package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Text;
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
import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.model.UserInfo;
import ws1415.SkatenightBackend.model.UserLocation;
import ws1415.SkatenightBackend.model.UserPicture;
import ws1415.SkatenightBackend.model.Visibility;
import ws1415.SkatenightBackend.transport.ListWrapper;
import ws1415.SkatenightBackend.transport.UserListData;
import ws1415.SkatenightBackend.transport.UserProfile;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Der UserEndpoint stellt Hilfsmethoden bereit, die genutzt werden um die Benutzer zu verwalten.
 *
 * @author Martin Wrodarczyk
 */
public class UserEndpoint extends SkatenightServerEndpoint {
    public long lastFieldUpdateTime = 0;

    /**
     * Erstellt ein Member-Objekt für die angegebene
     * E-Mail.
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

    /**
     * Wird von den Benutzer-Apps aufgerufen, wenn der Benutzer noch kein zu verwendendes Konto an-
     * gegeben hat. Es wird außerdem ein Member-Objekt für den Benutzer erstellt, falls es noch nicht
     * existiert.
     *
     * @param user
     * @param regid
     */
    public void registerForGCM(User user, @Named("regid") String regid) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        Member m = getMember(user.getEmail());
        if (m == null) {
            createMember(user.getEmail());
        }
        //if (!existsUser(user.getEmail()).value) {
        //    createUser(user.getEmail());
        //}
        RegistrationManager registrationManager;
        // Den GCM-Manager abrufen, falls er existiert, sonst einen neuen Manager anlegen.
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
     * @return true, wenn Benutzer erstellt wurde, false andernfalls
     */
    public BooleanWrapper createUser(@Named("userMail") String userMail, @Nullable @Named("firstName") String firstName,
                                     @Nullable @Named("lastName") String lastName, Text picture) {
        if (!existsUser(userMail).value) {
            UserLocation userLocation = new UserLocation(userMail);
            UserPicture userPicture = new UserPicture(userMail);
            UserInfo userInfo = new UserInfo(userMail);
            if (picture.getValue() != null) userPicture.setPicture(picture);
            userInfo.setFirstName(firstName);
            userInfo.setLastName(new UserInfo.InfoPair(lastName, Visibility.PUBLIC.getId()));
            EndUser user = new EndUser(userMail, userLocation, userInfo, userPicture);

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
     * Prüft ob ein Benutzer mit der angegebenen E-Mail Adresse existiert. Es wird ein
     * BooleanWrapper Objekt zurückgegeben da man auf dem Server keine primitive Datentypen
     * als Rückgabetypen verwenden kann.
     *
     * @param userMail E-Mail Adresse des zu prüfenden Benutzers
     * @return true, falls Benutzer existiert, sonst false
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
     *
     * @param userMail E-Mail Adresse des zu auszugebenen Benutzers
     * @return Benutzer mit der angegebenen E-Mail Adresse, falls nicht gefunden: null
     * TODO: Wirklich nötig?
     */
    @ApiMethod(path = "enduser")
    public EndUser getFullUser(@Named("userMail") String userMail) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            // Stellt sicher das Objekte der Assoziation auch runtergeladen werden
            endUser.getUserPicture();
            endUser.getUserInfo();
            endUser.getUserLocation();
            return endUser;
        } catch (Exception e) {
            // User mit der Email wurde nicht gefunden
            return null;
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt die Standordinformationen eines Benutzers mit der angegebenen E-Mail Adresse aus.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @return Standortinformationen zum Benutzer, falls nicht gefunden: null
     */
    @ApiMethod(path = "user_location")
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
     * Gibt die allgemeinen Informationen und das Profilbild zu einem Benutzer mit der angegebenen
     * E-Mail Adresse aus.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @return Informationen und Profilbild vom Benutzer, falls nicht gefunden: null
     */
    @ApiMethod(path = "user_profile")
    public UserProfile getUserProfile(User user, @Named("userMail") String userMail) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            List<String> friends = endUser.getMyFriends();
            endUser.getUserPicture();
            endUser.getUserInfo();

            UserInfo detachedUserInfo = pm.detachCopy(endUser.getUserInfo());
            setUpVisibility(detachedUserInfo, user, userMail, friends);

            // TODO: Events abrufen und setzen

            // TODO: Benutzergruppen abrufen und setzen

            UserProfile result = new UserProfile();
            result.setUserInfo(detachedUserInfo);
            result.setUserPicture(endUser.getUserPicture());
            result.setEmail(endUser.getEmail());
            result.setOptOutSearch(endUser.isOptOutSearch());
            result.setShowPrivateGroups(endUser.getShowPrivateGroups());
            return result;
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt eine Liste von allen Nutzergruppen aus, bei denen der Benutzer als Mitglied eingetragen
     * ist.
     *
     * @param endUser Benutzer dessen Gruppen abgerufen werden
     * @return Liste von beigetretenen oder erstellten Nutzergruppen
     */
    private List<UserGroup> listUserGroups(EndUser endUser) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        List<String> userGroupIds = endUser.getMyUserGroups();
        UserGroup userGroup;
        List<UserGroup> result = new ArrayList<>();
        for (String userGroupId : userGroupIds) {
            userGroup = ofy().load().type(UserGroup.class).id(userGroupId).now();
            if (userGroup != null) result.add(userGroup);
        }
        return result;
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
            event = new EventEndpoint().getEvent(key);
            if (event != null) result.add(event);
        }
        return result;
    }


    /**
     * Gibt die allgemeinen Informationen zu einem Benutzer mit der angegebenen E-Mail Adresse
     * aus.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @return Allgemeine Informationen zum Benutzer, falls nicht gefunden: null
     */
    @ApiMethod(path = "user_info")
    public UserListData getUserInfo(User user, @Named("userMail") String userMail, @Named("withPicture") boolean withPicture) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            List<String> friends = endUser.getMyFriends();

            UserInfo detachedUserInfo = pm.detachCopy(endUser.getUserInfo());
            setUpVisibility(detachedUserInfo, user, userMail, friends);

            UserListData result = new UserListData(endUser.getEmail(), detachedUserInfo);
            if (withPicture) result.setUserPicture(endUser.getUserPicture());
            return result;
        } catch (Exception e) {
            return null;
        } finally {
            pm.close();
        }
    }

    /**
     * Setzt die Informationen der UserInfo auf null, die für den User nicht sichtbar sein sollen.
     *
     * @param userInfo    Informationen die aufgerufen werden
     * @param user        User, der Informationen abrufen will
     * @param mailToCheck E-Mail Adresse des Benutzers mit den Informationen
     * @param friends     E-Mail Adressen der Freunde des Benutzers mit den Informationen
     * @return Informationen mit Sichtbarkeitsanpassungen
     */
    private UserInfo setUpVisibility(UserInfo userInfo, User user, String mailToCheck, List<String> friends) {
        if (!isVisible(user, mailToCheck, userInfo.getLastName().getVisibility(), friends).value) {
            userInfo.getLastName().setValue(null);
        }
        if (!isVisible(user, mailToCheck, userInfo.getCity().getVisibility(), friends).value) {
            userInfo.getCity().setValue(null);
        }
        if (!isVisible(user, mailToCheck, userInfo.getDateOfBirth().getVisibility(), friends).value) {
            userInfo.getDateOfBirth().setValue(null);
        }
        if (!isVisible(user, mailToCheck, userInfo.getDescription().getVisibility(), friends).value) {
            userInfo.getDescription().setValue(null);
        }
        if (!isVisible(user, mailToCheck, userInfo.getPostalCode().getVisibility(), friends).value) {
            userInfo.getPostalCode().setValue(null);
        }
        return userInfo;
    }

    /**
     * Hilfsmethode, um die Sichtbarkeit abzufragen.
     *
     * @param user        Benutzer, der Informationen abrufen will
     * @param mailToCheck Benutzer, deren Informationen abgerufen werden
     * @param visibility  Sichtbarkeit der Information
     * @param friends     E-Mail Adressen der Freunde des Benutzers, dessen Informationen abgerufen werden
     * @return true, falls Information sichtbar, false andernfalls
     */
    private BooleanWrapper isVisible(User user, String mailToCheck, Integer visibility, List<String> friends) {
        if (visibility.equals(Visibility.PUBLIC.getId())) return new BooleanWrapper(true);
        if (visibility.equals(Visibility.ONLY_ME.getId()))
            return new BooleanWrapper(user.getEmail().equals(mailToCheck));
        if (visibility.equals(Visibility.FRIENDS.getId())) {
            if(user.getEmail().equals(mailToCheck)) return new BooleanWrapper(true);
            if (friends == null) return new BooleanWrapper(false);
            for (String friend : friends) {
                if (friend.equals(user.getEmail())) return new BooleanWrapper(true);
            }
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(false);
    }

    /**
     * Gibt das Profilbild des Benuters mit der angegebenen E-Mail Adresse aus.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @return Profilbild des Benutzers, falls nicht gefunden: null
     */
    @ApiMethod(path = "user_picture")
    public UserPicture getUserPicture(@Named("userMail") String userMail) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            return pm.getObjectById(UserPicture.class, userMail);
        } catch (Exception e) {
            return null;
        } finally {
            pm.close();
        }
    }


    /**
     * Listet die allgemeinen Informationen der Benutzer auf, dessen E-Mail Adressen übergeben
     * wurden.
     *
     * @param userMails Liste der E-Mail Adressen der Benutzer
     * @return Liste von allgemeinen Informationen
     */
    @ApiMethod(path = "user_info_list")
    public List<UserListData> listUserInfo(User user, @Named("userMails") List<String> userMails,
                                           @Named("withPicture") boolean withPicture) {
        List<UserListData> result = new ArrayList<>();
        UserListData userInfo;
        for (String userMail : userMails) {
            userInfo = getUserInfo(user, userMail, withPicture);
            if (userInfo != null) result.add(userInfo);
        }
        return result;
    }

    /**
     * Listet die Profilbilder der Benutzer auf, dessen E-Mail Adressen übergeben wurden.
     *
     * @param userMails Liste der E-Mail Adressen der Benutzer
     * @return Liste von Profilbildern
     */
    @ApiMethod(path = "user_picture_list")
    public List<UserPicture> listUserPicture(@Named("userMails") List<String> userMails) {
        List<UserPicture> result = new ArrayList<>();
        UserPicture userPicture;
        for (String userMail : userMails) {
            userPicture = getUserPicture(userMail);
            if (userPicture != null) result.add(userPicture);
        }
        return result;
    }


    /**
     * Erstellt eine Liste mit allgemeinen Informationen zu den Freunden eines Benutzers.
     *
     * @param user     User-Objekt für die Authentifikation
     * @param userMail E-Mail Adresse des Benutzers
     * @return Liste von Informationen zu Freunden
     * @throws OAuthRequestException
     * @throws UnauthorizedException
     */
    @ApiMethod(path = "friends_list")
    public List<UserListData> listFriends(User user, @Named("userMail") String userMail) throws OAuthRequestException, UnauthorizedException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(userMail);
        EndpointUtil.throwIfUserNotSame(user.getEmail(), userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        List<String> friendMails;
        try {
            friendMails = pm.getObjectById(EndUser.class, userMail).getMyFriends();
        } finally {
            pm.close();
        }
        return listUserInfo(user, friendMails, true);
    }

    /**
     * Durchsucht die Benutzer nach der Eingabe und gibt die allgemeinen Nutzerinformationen
     * zu den Ergebnissen in einer Liste aus.
     *
     * @param input Eingabe
     * @return Liste von allgemeinen Informationen zu Benutzern
     */
    public ListWrapper searchUsers(User user, @Named("input") String input) throws OAuthRequestException {
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
            for(String userMail : resultMails){
                EndUser endUser = pm.getObjectById(EndUser.class, userMail);
                if(!endUser.isOptOutSearch()){
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
     * Aktualisiert die allgemeinen Informationen zu einem Benutzer.
     *
     * @param user        User-Objekt für die Authentifikation
     * @param newUserInfo Neue allgemeine Informationen zu dem Benutzer
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
     * Aktualisiert das Profilbild eines Benutzers.
     *
     * @param user     User-Objekt für die Authentifikation
     * @param userMail E-Mail Adresse des Benutzers
     * @throws UnauthorizedException Wird geworfen, falls Benutzer nicht authorisiert ist die Daten
     *                               zu ändern
     * @throws OAuthRequestException Wird geworfen, falls kein User-Objekt übergeben wird
     */
    @ApiMethod(path = "user_picture")
    public void updateUserPicture(User user, @Named("userMail") String userMail, Text encodedImage) throws
            UnauthorizedException, OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfEndUserNotExists(user.getEmail());
        EndpointUtil.throwIfUserNotSame(user.getEmail(), userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserPicture userPicture = pm.getObjectById(UserPicture.class, userMail);
            if(encodedImage.getValue() != null)userPicture.setPicture(encodedImage);
            else userPicture.setPicture(null);
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
            pm.makePersistent(userLocation);
        } finally {
            pm.close();
        }
    }

    /**
     * Fügt einen Benutzer der Freundeliste hinzu.
     *
     * @param user     User-Objekt für die Authentifikation
     * @param userMail E-Mail Adresse des Benutzers
     * @return true, falls Benutzer hinzugefügt wurde, false falls dieser nicht existiert
     * @throws OAuthRequestException
     */
    public BooleanWrapper addFriend(User user, @Named("userMail") String userMail, @Named("friendMail") String friendMail) throws OAuthRequestException, UnauthorizedException {
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
    public BooleanWrapper removeFriend(User user, @Named("userMail") String userMail, @Named("friendMail") String friendMail)
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
     * Löscht einen Benutzer auf dem Server, falls dieser vorhanden ist. Wird zur Zeit nur für
     * die Tests verwendet.
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
            try {
                pm.deletePersistent(endUser.getUserPicture());
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


            // Aus Domänen löschen
        }

    }
}
