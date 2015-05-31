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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.Member;
import ws1415.SkatenightBackend.model.UserInfo;
import ws1415.SkatenightBackend.model.UserInfo.InfoPair;
import ws1415.SkatenightBackend.model.UserLocation;
import ws1415.SkatenightBackend.model.Visibility;
import ws1415.SkatenightBackend.transport.ListWrapper;
import ws1415.SkatenightBackend.transport.StringWrapper;
import ws1415.SkatenightBackend.transport.UserGroupMetaData;
import ws1415.SkatenightBackend.transport.UserListData;
import ws1415.SkatenightBackend.transport.UserLocationInfo;
import ws1415.SkatenightBackend.transport.UserPrimaryData;
import ws1415.SkatenightBackend.transport.UserProfile;

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
            UserLocation userLocation = new UserLocation(userMail);
            UserInfo userInfo = new UserInfo(userMail);
            userInfo.setFirstName(firstName);
            userInfo.setLastName(new UserInfo.InfoPair(lastName, Visibility.PUBLIC.getId()));
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
     * Gibt den Benutzer mit allen Informationen zu der angegebenen E-Mail Adresse aus.
     * TODO: Wirklich nötig?
     *
     * @param userMail E-Mail Adresse des zu auszugebenen Benutzers
     * @return Benutzer mit der angegebenen E-Mail Adresse, falls nicht gefunden: null
     */
    @ApiMethod(path = "enduser")
    public EndUser getFullUser(@Named("userMail") String userMail) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            endUser.getUserInfo();
            endUser.getUserLocation();
            return endUser;
        } catch (Exception e) {
            return null;
        } finally {
            pm.close();
        }
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
                return new UserPrimaryData(userMail, endUser.getPictureBlobKey(),
                        endUser.getUserInfo().getFirstName(), endUser.getUserInfo().getLastName().getValue());
            } else {
                List<String> friends = endUser.getMyFriends();
                String lastName = "";
                Integer lastNameVis = endUser.getUserInfo().getLastName().getVisibility();
                if (isVisible(user.getEmail(), userMail, lastNameVis, friends)) {
                    lastName = endUser.getUserInfo().getLastName().getValue();
                }
                return new UserPrimaryData(userMail, endUser.getPictureBlobKey(),
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
            Integer lastNameVis = lastNamePair.getVisibility();
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
     * Gibt die Standortinformationen eines Benutzers aus. Wird für die Feldberechnung verwendet.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @return Standortinformationen des Benutzers
     */
    public UserLocation getUserLocation(@Named("userMail") String userMail) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            return pm.getObjectById(UserLocation.class, userMail);
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
     * @return Allgemeine Informationen, beigretene Gruppen, teilgenommene Veranstaltungen und
     * Einstellungen des Benutzers
     */
    @ApiMethod(path = "user_profile")
    public UserProfile getUserProfile(User user, @Named("userMail") String userMail) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            List<String> friends = endUser.getMyFriends();

            UserInfo detachedUserInfo = pm.detachCopy(endUser.getUserInfo());
            setUpVisibility(detachedUserInfo, user.getEmail(), userMail, friends);

            // TODO: Events abrufen und setzen

            List<UserGroupMetaData> userGroups = listUserGroupMetaData(user.getEmail(), endUser);

            UserProfile result = new UserProfile();
            result.setUserInfo(detachedUserInfo);
            result.setUserPicture(endUser.getPictureBlobKey());
            result.setEmail(endUser.getEmail());
            result.setMyUserGroups(userGroups);
            result.setOptOutSearch(endUser.isOptOutSearch());
            result.setShowPrivateGroups(endUser.getShowPrivateGroups());
            return result;
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt eine Liste von allen Veranstaltungen aus, an denen der Benutzer teilgenommen hat bzw.
     * teilnimmt.
     *
     * @param endUser Benutzer dessen Veranstaltungen abgerufen werden
     * @return Liste von Veranstaltungen des Benutzers
     */
    private List<Event> listEvents(EndUser endUser) {
        List<Long> eventIds = endUser.getMyEvents();
        Event event;
        List<Event> result = new ArrayList<>();
        for (long key : eventIds) {
            // TODO Methode ggf. in den EventEndpoint verschieben
//            event = new EventEndpoint().getEvent(key);
//            if (event != null) result.add(event);
        }
        return result;
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

            UserInfo detachedUserInfo = pm.detachCopy(endUser.getUserInfo());
            setUpVisibility(detachedUserInfo, user.getEmail(), userMail, friends);

            return new UserListData(endUser.getEmail(), detachedUserInfo,
                    endUser.getPictureBlobKey());
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
    public ListWrapper searchUsers(@Named("input") String input) throws OAuthRequestException {
        List<String> cache;
        Set<String> resultMails = new HashSet<>();
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Query q = pm.newQuery(UserInfo.class);
            q.setFilter("email.startsWith(inputParam)");
            q.declareParameters("String inputParam");
            q.setResult("this.email");
            cache = (List<String>) q.execute(input);
            resultMails.addAll(cache);

            q = pm.newQuery(UserInfo.class);
            q.setFilter("firstName.startsWith(inputParam)");
            q.declareParameters("String inputParam");
            q.setResult("this.email");
            cache = (List<String>) q.execute(input);
            resultMails.addAll(cache);

            q = pm.newQuery(UserInfo.class);
            q.setFilter("lastName.value.startsWith(inputParam)");
            q.declareParameters("String inputParam");
            q.setResult("this.email");
            cache = (List<String>) q.execute(input);
            resultMails.addAll(cache);

            Set<String> cacheMails = new HashSet<>();
            for (String userMail : resultMails) {
                EndUser endUser = pm.getObjectById(EndUser.class, userMail);
                if (!endUser.isOptOutSearch()) {
                    cacheMails.add(userMail);
                }
            }
            resultMails = cacheMails;

//            List<UserListData> result = new ArrayList<>();
//            for (String userMail : resultMails) {
//                result.add(getUserInfo(user, userMail, false));
//            }
//            return result;
            return new ListWrapper(new ArrayList<>(resultMails));
        } finally {
            pm.close();
        }
    }

    /**
     * Aktualisiert die Profilinformationen eines Benutzers.
     *
     * @param user              User-Objekt für die Authentifikation
     * @param newUserInfo       Neue allgemeine Informationen zu dem Benutzer
     * @param optOutSearch      True, falls der Benutzer aus der Suche ausgetragen werden soll, false
     *                          andernfalls
     * @param showPrivateGroups True, falls private Gruppen des Benutzers auf dem Profil angezeigt
     *                          werden sollen, false andernfalls
     * @throws UnauthorizedException Wird geworfen, falls Benutzer nicht authorisiert ist, die Aktion auszuführen
     * @throws OAuthRequestException Wird geworfen, falls kein User-Objekt übergeben wird
     */
    @ApiMethod(path = "user_profile")
    public UserInfo updateUserProfile(User user, UserInfo newUserInfo, @Named("optOutSearch") Boolean optOutSearch, @Named("showPrivateGroups") Integer showPrivateGroups)
            throws UnauthorizedException, OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(user.getEmail());
        EndpointUtil.throwIfUserNotSame(user.getEmail(), newUserInfo.getEmail());
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, newUserInfo.getEmail());
            endUser.setOptOutSearch(optOutSearch);
            endUser.setShowPrivateGroups(showPrivateGroups);
            UserInfo userInfo = endUser.getUserInfo();
            userInfo.setFirstName(newUserInfo.getFirstName());
            userInfo.setLastName(newUserInfo.getLastName());
            userInfo.setCity(newUserInfo.getCity());
            userInfo.setDateOfBirth(newUserInfo.getDateOfBirth());
            userInfo.setDescription(newUserInfo.getDescription());
            userInfo.setGender(newUserInfo.getGender());
            userInfo.setPostalCode(newUserInfo.getPostalCode());
            return userInfo;
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
            BlobKey picture = endUser.getPictureBlobKey();
            if (picture != null) blobstoreService.delete(picture);
            endUser.setPictureBlobKey(null);
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
            UserLocation userLocation = pm.getObjectById(UserLocation.class, userMail);
            userLocation.setUpdatedAt(new Date());
            userLocation.setLatitude(latitude);
            userLocation.setLongitude(longitude);

            if (userLocation.getCurrentEventId() == null || userLocation.getCurrentEventId() != currentEventId) {
                userLocation.setCurrentEventId(currentEventId);
                userLocation.setCurrentWaypoint(0);
            }

            RouteEndpoint routeEndpoint = new RouteEndpoint();
            routeEndpoint.calculateCurrentWaypoint(userLocation);
            if (System.currentTimeMillis() - lastFieldUpdateTime >= routeEndpoint.FIELD_UPDATE_INTERVAL) {
                routeEndpoint.calculateField(userLocation.getCurrentEventId());
                lastFieldUpdateTime = System.currentTimeMillis();
            }
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

            if (endUser.getPictureBlobKey() != null)
                blobstoreService.delete(endUser.getPictureBlobKey());
            try {
                pm.deletePersistent(endUser.getUserInfo());
                pm.deletePersistent(endUser.getUserLocation());
                pm.deletePersistent(endUser);
            } finally {
                pm.close();
            }

            // Lösche aus Teilnehmerlisten
            //List<Key> eventIds = endUser.getMyEvents();
            //Event event;
            //for (Key eventId : eventIds) {
            //   event = new EventEndpoint().getEvent(eventId.getId());
            // event.removeMember(userMail)
            // pm.makePersistent(event);
            //}

            //List<String> groupIds = endUser.getMyUserGroups();
            //UserGroup userGroup;
            //for (String groupId : groupIds) {
            //   userGroup = new GroupEndpoint().getUserGroup(groupId);
            // userGroup.removeMember(userMail);
            // pm.makePersistent(userGroup);
            //}

        }
    }

    /**
     * Hilfsmethoden
     */

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
     * Hilfsmethode, um die Sichtbarkeit abzufragen.
     *
     * @param mailOfCaller Benutzer, der Informationen abrufen will
     * @param mailOfCalled Benutzer, deren Informationen abgerufen werden
     * @param visibility   Sichtbarkeit der Information
     * @param friends      E-Mail Adressen der Freunde des Benutzers, dessen Informationen abgerufen werden
     * @return true, falls Information sichtbar, false andernfalls
     */
    protected Boolean isVisible(String mailOfCaller, String mailOfCalled, Integer visibility, List<String> friends) {
        if (visibility.equals(Visibility.PUBLIC.getId())) return true;
        if (visibility.equals(Visibility.ONLY_ME.getId()))
            return mailOfCaller.equals(mailOfCalled);
        if (visibility.equals(Visibility.FRIENDS.getId())) {
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
     * @param mailOfCaller E-Mail Adresse des Benutzers, der die Gruppen abfragt
     * @param endUser      Benutzer dessen Gruppen abgerufen werden
     * @return Liste von beigetretenen oder erstellten Nutzergruppen
     */
    private List<UserGroupMetaData> listUserGroupMetaData(String mailOfCaller, EndUser endUser) {
        Integer showPrivateGroupsVisibility = endUser.getShowPrivateGroups();
        List<String> friends = endUser.getMyFriends();
        Boolean showPrivateGroups = isVisible(mailOfCaller, endUser.getEmail(),
                showPrivateGroupsVisibility, friends);
        List<String> userGroupIds = endUser.getMyUserGroups();
        UserGroupMetaData userGroup;
        List<UserGroupMetaData> result = new ArrayList<>();
        for (String userGroupId : userGroupIds) {
            userGroup = new GroupEndpoint().getUserGroupMetaData(userGroupId);
            if (userGroup != null) {
                if (!userGroup.isOpen() && showPrivateGroups) {
                    result.add(userGroup);
                } else if (userGroup.isOpen()) {
                    result.add(userGroup);
                }
            }
        }
        return result;
    }


    /**
     * ALTE METHODEN
     */
    /**
     * Erstellt ein Member-Objekt für die angegebene E-Mail.
     *
     * @param mail
     */
    public void createMember(@Named("mail") String mail) {
        Member m = getMember(mail);
        if (m == null) {
            m = new Member();
            m.setEmail(mail);
            m.setName(mail);

            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            try {
                pm.makePersistent(m);
            } finally {
                pm.close();
            }
        }
    }

    /**
     * Liefert das auf dem Server hinterlegte Member-Objekt mit der angegebenen Mail.
     *
     * @param email Die Mail-Adresse des Member-Objekts, das abgerufen werden soll.
     * @return Das aktuelle Member-Objekt.
     */
    public Member getMember(@Named("email") String email) {
        Member member = null;
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Query q = pm.newQuery(Member.class);
            q.setFilter("email == emailParam");
            q.declareParameters("String emailParam");
            List<Member> results = (List<Member>) q.execute(email);
            if (!results.isEmpty()) {
                member = results.get(0);
            }
        } catch (JDOObjectNotFoundException e) {
            // Wird geworfen, wenn kein Objekt mit dem angegebenen Schlüssel existiert
            // In diesem Fall null zurückgeben
            return null;
        } finally {
            pm.close();
        }
        return member;
    }

    protected Boolean isFriendWith(String userMail, String mailToCheck){
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            for(String friend : endUser.getMyFriends()){
                if(friend.equals(mailToCheck)) return true;
            }
        } catch (Exception e){
            return false;
        } finally {
            pm.close();
        }
        return false;
    }
}
