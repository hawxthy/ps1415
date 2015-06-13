package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.model.UserInfo;
import ws1415.SkatenightBackend.model.UserInfo.InfoPair;
import ws1415.SkatenightBackend.model.UserLocation;
import ws1415.SkatenightBackend.model.Visibility;
import ws1415.SkatenightBackend.transport.ListWrapper;
import ws1415.SkatenightBackend.transport.StringWrapper;
import ws1415.SkatenightBackend.transport.UserListData;
import ws1415.SkatenightBackend.transport.UserLocationInfo;
import ws1415.SkatenightBackend.transport.UserPrimaryData;
import ws1415.SkatenightBackend.transport.UserProfile;
import ws1415.SkatenightBackend.transport.UserProfileEdit;

/**
 * Der UserEndpoint stellt Methoden bereit, die genutzt werden um die Benutzer auf dem Server zu
 * verwalten.
 *
 * @author Martin Wrodarczyk
 */
public class UserEndpoint extends SkatenightServerEndpoint {
    private long lastFieldUpdateTime = 0;
    private static final BlobstoreService blobstoreService =
            BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Gibt die UploadUrl des Blobstores zurück. Dient zum Uploaden von Bildern in den Blobstore.
     *
     * @return UploadUrl
     */
    public StringWrapper getUploadUrl() {
        return new StringWrapper(blobstoreService.createUploadUrl("/userImages/upload"));
    }

    /**
     * Registriert den Benutzer mit seiner Registration Id für Google Cloud Messaging.
     *
     * @param user  User-Objekt zur Authentifizierung
     * @param regid GCM-ID
     */
    public void registerForGCM(User user, @Named("regid") String regid) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        RegistrationManager registrationManager;
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            registrationManager = getRegistrationManager(pm);
            registrationManager.addRegistrationId(user.getEmail(), regid);
            pm.makePersistent(registrationManager);
        } finally {
            pm.close();
        }
    }

    /**
     * Erstellt einen Benutzer auf dem Server, falls noch keiner zu der angegebenen E-Mail Adresse
     * existiert.
     *
     * @param userMail  E-Mail Adresse des zu erstellenden Benutzers
     * @param firstName Vorname des zu erstellenden Benutzers
     * @param lastName  Nachname des zu erstellenden Benutzers
     * @return true, wenn der Benutzer erstellt wurde, false andernfalls
     */
    public BooleanWrapper createUser(@Named("userMail") String userMail, @Nullable @Named("firstName") String firstName,
                                     @Nullable @Named("lastName") String lastName) {
        if (!existsUser(userMail).value) {
            UserLocation userLocation = new UserLocation();
            UserInfo userInfo = new UserInfo(firstName, lastName);
            EndUser user = new EndUser(userMail, userLocation, userInfo);

            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            try {
                pm.makePersistent(user);
                return new BooleanWrapper(true);
            } finally {
                pm.close();
            }
        }
        return new BooleanWrapper(false);
    }

    /**
     * Prüft, ob ein Benutzer mit der angegebenen E-Mail Adresse existiert.
     *
     * @param userMail E-Mail Adresse des zu prüfenden Benutzers
     * @return true, falls der Benutzer existiert, sonst false
     */
    public BooleanWrapper existsUser(@Named("userMail") String userMail) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        EndUser user;
        try {
            user = pm.getObjectById(EndUser.class, userMail);
        } catch (Exception e) {
            user = null;
        } finally {
            pm.close();

        }
        if (user == null) {
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(true);
    }

    /**
     * Gibt die primären Informationen Vorname, Nachname und Key des Profilbildes des Benutzers aus.
     *
     * @param user User-Objekt zur Authentifizierung
     * @return Vorname, Nachname und Key des Profilbildes des Benutzers
     */
    public UserPrimaryData getPrimaryData(User user, @Named("userMail") String userMail) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            if (user.getEmail().equals(userMail)) {
                return new UserPrimaryData(userMail, endUser.getUserPicture(),
                        endUser.getUserInfo().getFirstName(), endUser.getUserInfo().getLastName().getValue());
            } else {
                List<String> friends = endUser.getMyFriends();
                String lastName = "";
                Visibility lastNameVis = endUser.getUserInfo().getLastName().getVisibility();
                if (isVisible(user.getEmail(), userMail, lastNameVis, friends)) {
                    lastName = endUser.getUserInfo().getLastName().getValue();
                }
                return new UserPrimaryData(userMail, endUser.getUserPicture(),
                        endUser.getUserInfo().getFirstName(), lastName);
            }
        } finally {
            pm.close();

        }
    }

    /**
     * Gibt die E-Mail, den Namen und die Standortinformationen des Benutzers aus. Wird für
     * die Anzeige von Benutzern auf der Karte verwendet.
     *
     * @param user     User-Objekt zur Authentifizierung
     * @param userMail E-Mail Adresse des Benutzers
     * @return E-Mail, Name und Standortinformationen vom Benutzer
     */
    @ApiMethod(path = "user_location")
    public UserLocationInfo getUserLocationInfo(User user, @Named("userMail") String userMail) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            List<String> friends = endUser.getMyFriends();
            UserLocation userLocation = endUser.getUserLocation();
            String firstName = endUser.getUserInfo().getFirstName();
            InfoPair lastNamePair = endUser.getUserInfo().getLastName();
            String lastName = "";
            Visibility lastNameVis = lastNamePair.getVisibility();
            if (isVisible(user.getEmail(), userMail, lastNameVis, friends)) {
                lastName = lastNamePair.getValue();
            }
            return new UserLocationInfo(userMail, firstName, lastName, userLocation.getLatitude(),
                    userLocation.getLongitude());
        } catch (Exception e) {
            return null;
        } finally {
            pm.close();

        }
    }

    /**
     * Liefert eine Liste von Standortinformationen von Benutzern.
     *
     * @param user      User-Objekt zur Authentifizierung
     * @param userMails E-Mail Adressen der Benutzer
     * @return Standortinformationen von Benutzern
     * @throws OAuthRequestException
     */
    @ApiMethod(path = "user_location_info")
    public List<UserLocationInfo> listUserLocationInfo(User user, @Named("userMails") List<String> userMails) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            List<UserLocationInfo> locationInfos = new ArrayList<>();
            for (String userMail : userMails) {
                locationInfos.add(getUserLocationInfo(user, userMail));
            }
            return locationInfos;
        } finally {
            pm.close();

        }
    }

    /**
     * Gibt die Standortinformationen eines Benutzers aus. Wird für die Feldberechnung verwendet.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @return Standortinformationen des Benutzers
     */
    public UserLocation getUserLocation(@Named("userMail") String userMail) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            return endUser.getUserLocation();
        } catch (Exception e) {
            return null;
        } finally {
            pm.close();

        }
    }

    /**
     * Gibt die Profilinformationen eines Benutzers mit der angegebenen E-Mail Adresse aus.
     *
     * @param user     User-Objekt zur Authentifizierung
     * @param userMail E-Mail Adresse des Benutzers
     * @return Allgemeine Informationen und beigretene Gruppen
     * @throws OAuthRequestException
     */
    @ApiMethod(path = "user_profile")
    public UserProfile getUserProfile(User user, @Named("userMail") String userMail) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            List<String> friends = endUser.getMyFriends();

            UserProfile userProfile = new UserProfile();
            setUpVisibility(userProfile, endUser.getUserInfo(), user.getEmail(), userMail, friends);
            List<String> userGroups = listUserGroupMetaData(user, endUser);

            userProfile.setEmail(userMail);
            userProfile.setUserPicture(endUser.getUserPicture());
            userProfile.setFirstName(endUser.getUserInfo().getFirstName());
            userProfile.setGender(endUser.getUserInfo().getGender());
            userProfile.setMyUserGroups(userGroups);
            userProfile.setEventCount(endUser.getMyEvents().size());
            return userProfile;
        } finally {
            pm.close();

        }
    }

    /**
     * Gibt die Profilinformationen eines Benutzers mit der angegebenen E-Mail Adresse aus.
     *
     * @param user     User-Objekt zur Authentifizierung
     * @param userMail E-Mail Adresse des Benutzers
     * @return Allgemeine Informationen, beigretene Gruppen, teilgenommene Veranstaltungen und
     * Einstellungen des Benutzers
     */
    @ApiMethod(path = "user_profile_edit")
    public UserProfileEdit getUserProfileEdit(User user, @Named("userMail") String userMail) throws OAuthRequestException, UnauthorizedException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        EndpointUtil.throwIfUserNotSame(user.getEmail(), userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);

            return new UserProfileEdit(endUser.getEmail(),
                    endUser.isOptOutSearch(),
                    endUser.getShowPrivateGroups(),
                    endUser.getUserPicture(),
                    endUser.getUserInfo());
        } finally {
            pm.close();

        }
    }

    /**
     * Gibt die allgemeinen Informationen zu einem Benutzer mit der angegebenen E-Mail Adresse
     * aus.
     *
     * @param user     User-Objekt zur Authentifizierung
     * @param userMail E-Mail Adresse des Benutzers
     * @return Allgemeine Informationen und Profilbild des Benutzers, falls nicht gefunden: null
     */
    @ApiMethod(path = "user_info")
    public UserListData getUserInfo(User user, @Named("userMail") String userMail) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            List<String> friends = endUser.getMyFriends();

            UserListData userListData = new UserListData();
            setUpVisibility(userListData, endUser.getUserInfo(), user.getEmail(), userMail, friends);

            userListData.setEmail(userMail);
            userListData.setUserPicture(endUser.getUserPicture());
            userListData.setFirstName(endUser.getUserInfo().getFirstName());
            return userListData;
        } catch (Exception e) {
            return null;
        } finally {
            pm.close();

        }
    }

    /**
     * Listet die allgemeinen Informationen und die Profilbilder der Benutzer auf, dessen E-Mail
     * Adressen übergeben wurden.
     *
     * @param userMails Liste von E-Mail Adressen der Benutzer
     * @return Liste von allgemeinen Informationen und Profilbildern der Benutzer
     */
    @ApiMethod(path = "user_info_list")
    public List<UserListData> listUserInfo(User user, @Named("userMails") List<String> userMails) {
        List<UserListData> result = new ArrayList<>();
        UserListData userInfo;
        for (String userMail : userMails) {
            userInfo = getUserInfo(user, userMail);
            if (userInfo != null) result.add(userInfo);
        }
        return result;
    }

    /**
     * Erstellt eine Liste mit den E-Mail Adressen der Freunde eines Benutzers.
     *
     * @param user     User-Objekt für die Authentifikation
     * @param userMail E-Mail Adresse des Benutzers
     * @return Liste von E-Mail Adressen der Freunde
     * @throws OAuthRequestException
     * @throws UnauthorizedException
     */
    @ApiMethod(path = "friends_list")
    public ListWrapper listFriends(User user, @Named("userMail") String userMail) throws OAuthRequestException, UnauthorizedException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        EndpointUtil.throwIfUserNotSame(user.getEmail(), userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            return new ListWrapper(pm.getObjectById(EndUser.class, userMail).getMyFriends());
        } finally {
            pm.close();

        }
    }

    /**
     * Durchsucht die Benutzer nach der Eingabe und gibt die allgemeinen Informationen und
     * Profilbilder zu den Ergebnissen in einer Liste aus.
     *
     * @param input Eingabe nach der gesucht werden soll, wobei nach der E-Mail, dem Vornamen und
     *              dem Nachnamen gesucht wird
     * @return E-Mail Adressen der Ergebnisbenutzer
     */
    public ListWrapper searchUsers(StringWrapper input) throws OAuthRequestException, UnsupportedEncodingException {
        String query = input.string.trim();
        List<String> cache;
        Set<String> resultMails = new HashSet<>();
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Query q = pm.newQuery(EndUser.class);
            q.setFilter("email.startsWith(inputParam)");
            q.declareParameters("String inputParam");
            q.setResult("this.email");
            cache = (List<String>) q.execute(query);
            resultMails.addAll(cache);
            q.closeAll();

            q = pm.newQuery(EndUser.class);
            q.setFilter("fullNameLc.startsWith(inputParam)");
            q.declareParameters("String inputParam");
            q.setResult("this.email");
            cache = (List<String>) q.execute(query.toLowerCase());
            resultMails.addAll(cache);
            q.closeAll();

            Set<String> cacheMails = new HashSet<>();
            for (String userMail : resultMails) {
                EndUser endUser = pm.getObjectById(EndUser.class, userMail);
                if (!endUser.isOptOutSearch()) {
                    cacheMails.add(userMail);
                }
            }
            resultMails = cacheMails;

            return new ListWrapper(new ArrayList<>(resultMails));
        } finally {
            pm.close();

        }
    }

    /**
     * Aktualisiert die Profilinformationen eines Benutzers.
     *
     * @param user        User-Objekt für die Authentifikation
     * @param userProfile Neue Nutzerprofilinformationen
     * @throws UnauthorizedException Wird geworfen, falls Benutzer nicht authorisiert ist, die Aktion auszuführen
     * @throws OAuthRequestException Wird geworfen, falls kein User-Objekt übergeben wird
     */
    @ApiMethod(path = "user_profile")
    public BooleanWrapper updateUserProfile(User user, UserProfileEdit userProfile)
            throws UnauthorizedException, OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(user.getEmail());
        EndpointUtil.throwIfUserNotSame(user.getEmail(), userProfile.getEmail());
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userProfile.getEmail());
            UserInfo userInfo = endUser.getUserInfo();
            endUser.setOptOutSearch(userProfile.isOptOutSearch());
            endUser.setShowPrivateGroups(userProfile.getShowPrivateGroups());

            // Workaround für das Aktualisieren der embedded fields, da direktes Aktualisieren nicht übernommen wird
            UserInfo userInfoDet = pm.detachCopy(userInfo);
            UserInfo newUserInfo = userProfile.getUserInfo();
            userInfoDet.setFirstName(newUserInfo.getFirstName());
            userInfoDet.setLastName(newUserInfo.getLastName());
            userInfoDet.setCity(newUserInfo.getCity());
            userInfoDet.setDateOfBirth(newUserInfo.getDateOfBirth());
            userInfoDet.setDescription(newUserInfo.getDescription());
            userInfoDet.setGender(newUserInfo.getGender());
            userInfoDet.setPostalCode(newUserInfo.getPostalCode());
            endUser.setUserInfo(userInfoDet);
            pm.makePersistent(endUser);

            return new BooleanWrapper(true);
        } catch (Exception e) {
            return new BooleanWrapper(false);
        } finally {
            pm.close();

        }
    }

    /**
     * Löscht das Profilbild eines Benutzers.
     *
     * @param user     User-Objekt zur Authentifizierung
     * @param userMail Benutzer, dessen Profilbild gelöscht werden soll
     * @return true, falls Löschvorgang erfolgreich, false andernfalls
     * @throws OAuthRequestException
     * @throws UnauthorizedException
     */
    @ApiMethod(path = "user_picture")
    public BooleanWrapper removeUserPicture(User user, @Named("userMail") String userMail) throws OAuthRequestException, UnauthorizedException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        EndpointUtil.throwIfUserNotSame(user.getEmail(), userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            BlobKey picture = endUser.getUserPicture();
            if (picture != null) blobstoreService.delete(picture);
            endUser.setUserPicture(null);
            return new BooleanWrapper(true);
        } catch (Exception e) {
            return new BooleanWrapper(false);
        } finally {
            pm.close();

        }
    }

    /**
     * Aktualisiert die Standortinformationen eines Benutzers.
     *
     * @param user           User-Objekt für die Authentifikation
     * @param userMail       E-Mail Adresse des Benutzers
     * @param latitude       Neue Latitude
     * @param longitude      Neue Longitude
     * @param currentEventId Neue Event Id
     * @throws UnauthorizedException Wird geworfen, falls User nicht authorisiert ist, die Daten zu
     *                               ändern
     * @throws OAuthRequestException Wird geworfen, falls kein User-Objekt übergeben wird
     */
    @ApiMethod(path = "user_location")
    public void updateUserLocation(User user, @Named("userMail") String userMail,
                                   @Named("latitude") double latitude,
                                   @Named("longitude") double longitude,
                                   @Named("currentEventId") long currentEventId) throws Exception {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(user.getEmail());
        EndpointUtil.throwIfUserNotSame(user.getEmail(), userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            UserLocation userLocation = endUser.getUserLocation();
            UserLocation userLocationDet = pm.detachCopy(userLocation);

            userLocationDet.setUpdatedAt(new Date());
            userLocationDet.setLatitude(latitude);
            userLocationDet.setLongitude(longitude);

            if (userLocationDet.getCurrentEventId() == null || userLocationDet.getCurrentEventId() != currentEventId) {
                userLocationDet.setCurrentEventId(currentEventId);
                userLocationDet.setCurrentWaypoint(0);
            }

            RouteEndpoint routeEndpoint = new RouteEndpoint();
            routeEndpoint.calculateCurrentWaypoint(userLocationDet);
            if (System.currentTimeMillis() - lastFieldUpdateTime >= routeEndpoint.FIELD_UPDATE_INTERVAL) {
                routeEndpoint.calculateField(userLocationDet.getCurrentEventId());
                lastFieldUpdateTime = System.currentTimeMillis();
            }

            endUser.setUserLocation(userLocationDet);
            pm.makePersistent(endUser);
        } finally {
            pm.close();

        }
    }

    /**
     * Fügt einen Benutzer der Freundesliste hinzu.
     *
     * @param user       User-Objekt für die Authentifikation
     * @param userMail   E-Mail Adresse des Benutzers, dessen Freundesliste ergänzt wird
     * @param friendMail E-Mail Adresse des Freundes
     * @return true, falls Benutzer hinzugefügt wurde, false falls dieser nicht existiert
     * @throws OAuthRequestException
     */
    @ApiMethod(path = "friends_add")
    public BooleanWrapper addFriend(User user, @Named("userMail") String
            userMail, @Named("friendMail") String friendMail) throws
            OAuthRequestException, UnauthorizedException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserNotSame(user.getEmail(), userMail);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        EndpointUtil.throwIfEndUserNotExists(friendMail);
        if (user.getEmail().equals(friendMail)) {
            throw new IllegalArgumentException("friendMail may not be your own mail");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            List<String> myFriends = endUser.getMyFriends();
            for (String mail : myFriends) {
                if (mail.equals(friendMail)) return new BooleanWrapper(false);
            }
            myFriends.add(friendMail);
            return new BooleanWrapper(true);
        } finally {
            pm.close();

        }
    }

    /**
     * Löscht einen Benutzer aus der Freundeliste.
     *
     * @param user     User-Objekt für die Authentifikation
     * @param userMail E-Mail Adresse des Benutzers
     * @return true, falls Benutzer entfernt wurde, false falls dieser nicht exisiert
     * @throws OAuthRequestException
     */
    @ApiMethod(path = "friends_remove")
    public BooleanWrapper removeFriend(User user, @Named("userMail") String
            userMail, @Named("friendMail") String friendMail)
            throws OAuthRequestException, UnauthorizedException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserNotSame(user.getEmail(), userMail);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            return new BooleanWrapper(endUser.getMyFriends().remove(friendMail));
        } finally {
            pm.close();

        }
    }

    /**
     * Löscht einen Benutzer, falls dieser vorhanden ist. Wird zur Zeit nur für die Tests verwendet.
     *
     * @param userMail E-Mail Adresse des Benutzers
     */
    public void deleteUser(User user, @Named("userMail") String userMail) throws UnauthorizedException, OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfNoRights(user.getEmail(), userMail);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        if (existsUser(userMail).value) {
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);

            // Lösche aus Teilnehmerlisten
            for (Long eventId : endUser.getMyEvents())
                new EventEndpoint().removeUserFromEvent(userMail, eventId);

            // Lösche aus Gruppen
            for (String groupId : endUser.getMyUserGroups())
                new GroupEndpoint().removeMember(user, groupId, userMail);

            if (endUser.getUserPicture() != null)
                blobstoreService.delete(endUser.getUserPicture());
            try {
                pm.deletePersistent(endUser);
            } finally {
                pm.close();

            }
        }
    }

    /**
     * Hilfsmethoden
     */


    /**
     * Setzt die Informationen in UserProfile, falls diese sichtbar für den Aufrufer sind.
     *
     * @param userProfile  Informationen die aufgerufen werden
     * @param mailOfCaller User, der Informationen abrufen will
     * @param mailOfCalled E-Mail Adresse des Benutzers mit den Informationen
     * @param friends      E-Mail Adressen der Freunde des Benutzers mit den Informationen
     */
    private void setUpVisibility(UserProfile userProfile, UserInfo userInfo, String mailOfCaller, String mailOfCalled, List<String> friends) {
        if (isVisible(mailOfCaller, mailOfCalled, userInfo.getLastName().getVisibility(), friends)) {
            userProfile.setLastName(userInfo.getLastName().getValue());
        }
        if (isVisible(mailOfCaller, mailOfCalled, userInfo.getCity().getVisibility(), friends)) {
            userProfile.setCity(userInfo.getCity().getValue());
        }
        if (isVisible(mailOfCaller, mailOfCalled, userInfo.getDateOfBirth().getVisibility(), friends)) {
            userProfile.setDateOfBirth(userInfo.getDateOfBirth().getValue());
        }
        if (isVisible(mailOfCaller, mailOfCalled, userInfo.getDescription().getVisibility(), friends)) {
            userProfile.setDescription(userInfo.getDescription().getValue());
        }
        if (isVisible(mailOfCaller, mailOfCalled, userInfo.getPostalCode().getVisibility(), friends)) {
            userProfile.setPostalCode(userInfo.getPostalCode().getValue());
        }
    }

    /**
     * Setzt die Informationen der UserInfo auf null, die für den User nicht sichtbar sein sollen.
     *
     * @param userInfo     Informationen die aufgerufen werden
     * @param mailOfCaller User, der Informationen abrufen will
     * @param mailOfCalled E-Mail Adresse des Benutzers mit den Informationen
     * @param friends      E-Mail Adressen der Freunde des Benutzers mit den Informationen
     * @return Informationen mit Sichtbarkeitsanpassungen
     */
    private UserInfo setUpVisibility(UserInfo userInfo, String mailOfCaller, String mailOfCalled, List<String> friends) {
        if (!isVisible(mailOfCaller, mailOfCalled, userInfo.getLastName().getVisibility(), friends)) {
            userInfo.getLastName().setValue(null);
        }
        if (!isVisible(mailOfCaller, mailOfCalled, userInfo.getCity().getVisibility(), friends)) {
            userInfo.getCity().setValue(null);
        }
        if (!isVisible(mailOfCaller, mailOfCalled, userInfo.getDateOfBirth().getVisibility(), friends)) {
            userInfo.getDateOfBirth().setValue(null);
        }
        if (!isVisible(mailOfCaller, mailOfCalled, userInfo.getDescription().getVisibility(), friends)) {
            userInfo.getDescription().setValue(null);
        }
        if (!isVisible(mailOfCaller, mailOfCalled, userInfo.getPostalCode().getVisibility(), friends)) {
            userInfo.getPostalCode().setValue(null);
        }
        return userInfo;
    }

    /**
     * Setzt die Informationen in UserListData, falls diese sichtbar für den Aufrufer sind.
     *
     * @param userInfo     Informationen die aufgerufen werden
     * @param mailOfCaller User, der Informationen abrufen will
     * @param mailOfCalled E-Mail Adresse des Benutzers mit den Informationen
     * @param friends      E-Mail Adressen der Freunde des Benutzers mit den Informationen
     */
    private void setUpVisibility(UserListData userListData, UserInfo userInfo, String mailOfCaller, String mailOfCalled, List<String> friends) {
        if (isVisible(mailOfCaller, mailOfCalled, userInfo.getLastName().getVisibility(), friends)) {
            userListData.setLastName(userInfo.getLastName().getValue());
        }
        if (isVisible(mailOfCaller, mailOfCalled, userInfo.getCity().getVisibility(), friends)) {
            userListData.setCity(userInfo.getCity().getValue());
        }
        if (isVisible(mailOfCaller, mailOfCalled, userInfo.getDateOfBirth().getVisibility(), friends)) {
            userListData.setDateOfBirth(userInfo.getDateOfBirth().getValue());
        }
    }

    /**
     * Hilfsmethode, um die Sichtbarkeit abzufragen.
     *
     * @param mailOfCaller Benutzer, der Informationen abrufen will
     * @param mailOfCalled Benutzer, deren Informationen abgerufen werden
     * @param visibility   Sichtbarkeit der Information
     * @param friends      E-Mail Adressen der Freunde des Benutzers, dessen Informationen abgerufen werden
     * @return true, falls Information sichtbar, false andernfalls
     */
    protected Boolean isVisible(String mailOfCaller, String mailOfCalled, Visibility visibility, List<String> friends) {
        if (visibility.equals(Visibility.PUBLIC)) return true;
        if (visibility.equals(Visibility.ONLY_ME))
            return mailOfCaller.equals(mailOfCalled);
        if (visibility.equals(Visibility.FRIENDS)) {
            if (mailOfCaller.equals(mailOfCalled)) return true;
            if (friends == null) return false;
            for (String friend : friends) {
                if (friend.equals(mailOfCaller)) return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Gibt eine Liste von den Nutzergruppen aus, bei denen der Benutzer als Mitglied eingetragen
     * ist, wenn diese sichtbar sein soll für den aufgerufenen Benutzer.
     *
     * @param caller  E-Mail Adresse des Benutzers, der die Gruppen abfragt
     * @param endUser Benutzer dessen Gruppen abgerufen werden
     * @return Liste von beigetretenen oder erstellten Nutzergruppen
     */
    private List<String> listUserGroupMetaData(User caller, EndUser endUser) throws OAuthRequestException {
        Visibility showPrivateGroupsVisibility = endUser.getShowPrivateGroups();
        List<String> friends = endUser.getMyFriends();
        Boolean showPrivateGroups = isVisible(caller.getEmail(), endUser.getEmail(),
                showPrivateGroupsVisibility, friends);
        List<String> userGroupIds = endUser.getMyUserGroups();

        if (showPrivateGroups) {
            return userGroupIds;
        } else {
            return new GroupEndpoint().getPublicGroups(userGroupIds);
        }
    }

    /**
     * Prüft ob ein Benutzer mit einem anderen Benutzer befreundet ist.
     *
     * @param userMail    E-Mail Adresse des Benutzers mit der Freundeliste
     * @param mailToCheck E-Mail Adresse des Benutzers, dessen Freundschaft geprüft wird
     * @return true, falls Freundschaft besteht, false andernfalls
     */
    protected Boolean isFriendWith(String userMail, String mailToCheck) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            for (String friend : endUser.getMyFriends()) {
                if (friend.equals(mailToCheck)) return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            pm.close();

        }
        return false;
    }

    /**
     * In der Liste der beigetretenen Gruppen des Benutzers wird die übergebene Gruppe hinzugefügt.
     *
     * @param userMail  E-Mail des Benutzers
     * @param userGroup Nutzergruppe
     */
    protected void addGroupToUser(String userMail, UserGroup userGroup) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            endUser.addUserGroup(userGroup);
        } finally {
            pm.close();

        }
    }

    /**
     * In der Liste der beigetretenen Gruppen des Benutzers wird die übergebene Gruppe entfernt.
     *
     * @param userMail  E-Mail des Benutzers
     * @param userGroup Nutzergruppe
     */
    protected void removeGroupFromUser(String userMail, UserGroup userGroup) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            endUser.removeUserGroup(userGroup);
        } finally {
            pm.close();

        }
    }

    /**
     * Liefert alle Veranstaltungen an denen der Benutzer teilnimmt/teilgenommen hat.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @return Liste der Veranstaltungen
     */
    protected List<Long> listEventsFromUser(String userMail) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            return pm.getObjectById(EndUser.class, userMail).getMyEvents();
        } finally {
            pm.close();
        }
    }

    /**
     * In der Liste der teilgenommenen Veranstaltungen des Benutzers wird die übergebene
     * Veranstaltung hinzugefügt.
     *
     * @param userMail E-Mail des Benutzers
     */
    protected void addEventToUser(String userMail, Event event) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            endUser.addEvent(event);
        } finally {
            pm.close();

        }
    }

    /**
     * In der Liste der teilgenommenen Veranstaltungen des Benutzers wird die übergebene
     * Veranstaltung entfernt.
     *
     * @param userMail E-Mail des Benutzers
     */
    protected void removeEventFromUser(String userMail, Event event) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            endUser.removeEvent(event);
        } finally {
            pm.close();
        }
    }
}
