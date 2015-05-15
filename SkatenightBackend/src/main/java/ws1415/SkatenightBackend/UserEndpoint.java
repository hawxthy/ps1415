package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.Domain;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.GlobalDomain;
import ws1415.SkatenightBackend.model.Member;
import ws1415.SkatenightBackend.model.Role;
import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.model.UserInfo;
import ws1415.SkatenightBackend.model.UserInfoPicture;
import ws1415.SkatenightBackend.model.UserLocation;
import ws1415.SkatenightBackend.model.UserPicture;

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
        if (user != null) {
            Member m = getMember(user.getEmail());
            if (m == null) {
                createMember(user.getEmail());
            }
            if (!existsUser(user.getEmail()).value) {
                createUser(user.getEmail());
            }

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
    }

    /**
     * Erstellt einen Benutzer auf dem Server, falls noch keiner zu der angegebenen E-Mail Adresse
     * existiert.
     *
     * @param email E-Mail Adresse des zu erstellenden Benutzers
     */
    public void createUser(@Named("email") String email) {
        if (existsUser(email).value) {
            throw new JDOException("Benutzer existiert bereits");
        }
        UserLocation userLocation = new UserLocation(email);
        UserPicture userPicture = new UserPicture(email);
        UserInfo userInfo = new UserInfo(email);
        UserInfoPicture userInfoPicture = new UserInfoPicture(email, userInfo, userPicture);
        EndUser user = new EndUser(email, userInfoPicture, userLocation);

        Domain globalDomain = new RoleEndpoint().getGlobalDomain();
        globalDomain.setRole(email, Role.USER.getId());

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            pm.makePersistent(globalDomain);
        } finally {
            pm.close();
        }

        pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            pm.makePersistent(user);
        } finally {
            pm.close();
        }
    }

    /**
     * Prüft ob ein Benutzer mit der angegebenen E-Mail Adresse existiert. Es wird ein
     * BooleanWrapper Objekt zurückgegeben da man auf dem Server keine primitive Datentypen
     * als Rückgabetypen verwenden kann.
     *
     * @param email E-Mail Adresse des zu prüfenden Benutzers
     * @return true, falls Benutzer existiert, sonst false
     */
    public BooleanWrapper existsUser(@Named("email") String email) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        EndUser user;
        try {
            user = pm.getObjectById(EndUser.class, email);
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
     * @param email E-Mail Adresse des zu auszugebenen Benutzers
     * @return Benutzer mit der angegebenen E-Mail Adresse, falls nicht gefunden: null
     */
    @ApiMethod(path = "get_full_user")
    public EndUser getFullUser(@Named("email") String email) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, email);
            // Stellt sicher das Objekte der Assoziation auch runtergeladen werden
            endUser.getUserInfoPicture();
            endUser.getUserInfoPicture().getUserPicture();
            endUser.getUserInfoPicture().getUserInfo();
            endUser.getUserLocation();
            endUser.getMyEvents();
            endUser.getMyUserGroups();
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
     * @param email E-Mail Adresse des Benutzers
     * @return Standortinformationen zum Benutzer, falls nicht gefunden: null
     */
    @ApiMethod(path = "get_user_location")
    public UserLocation getUserLocation(@Named("email") String email) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            return pm.getObjectById(UserLocation.class, email);
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
     * @param email E-Mail Adresse des Benutzers
     * @return Informationen und Profilbild vom Benutzer, falls nicht gefunden: null
     */
    @ApiMethod(path = "get_user_info_with_picture")
    public UserInfoPicture getUserInfoWithPicture(@Named("email") String email) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserInfoPicture userInfoPicture = pm.getObjectById(UserInfoPicture.class, email);
            userInfoPicture.getUserInfo();
            userInfoPicture.getUserPicture();
            return userInfoPicture;
        } catch (Exception e) {
            return null;
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt die allgemeinen Informationen zu einem Benutzer mit der angegebenen E-Mail Adresse
     * aus.
     *
     * @param email E-Mail Adresse des Benutzers
     * @return Allgemeine Informationen zum Benutzer, falls nicht gefunden: null
     */
    @ApiMethod(path = "get_user_info")
    public UserInfo getUserInfo(@Named("email") String email) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            return pm.getObjectById(UserInfo.class, email);
        } catch (Exception e) {
            return null;
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt das Profilbild des Benuters mit der angegebenen E-Mail Adresse aus.
     *
     * @param email E-Mail Adresse des Benutzers
     * @return Profilbild des Benutzers, falls nicht gefunden: null
     */
    @ApiMethod(path = "get_user_picture")
    public UserPicture getUserPicture(@Named("email") String email) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            return pm.getObjectById(UserPicture.class, email);
        } catch (Exception e) {
            return null;
        } finally {
            pm.close();
        }
    }

    /**
     * Aktualisiert die allgemeinen Informationen zu einem Benutzer.
     *
     * @param user        User-Objekt für die Authentifikation
     * @param newUserInfo Neue allgemeine Informationen zu dem Benutzer
     * @throws UnauthorizedException Wird geworfen, falls Benutzer nicht authorisiert ist die Daten
     *                               zu ändern
     * @throws OAuthRequestException Wird geworfen, falls kein User-Objekt übergeben wird
     */
    // TODO: Authentifizierung korrigieren(?)
    @ApiMethod(path = "update_user_info")
    public UserInfo updateUserInfo(User user, UserInfo newUserInfo) throws UnauthorizedException, OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        } else if (!existsUser(user.getEmail()).value) {
            throw new JDOObjectNotFoundException("Benutzer konnte nicht gefunden werden");
        } else if (!user.getEmail().equals(newUserInfo.getEmail())) {
            throw new UnauthorizedException("Keine Rechte um diesen Benutzer zu editieren");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserInfo userInfo = pm.getObjectById(UserInfo.class, newUserInfo.getEmail());
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
     * @param user           User-Objekt für die Authentifikation
     * @param newUserPicture Neues Profilbild eines Benutzers
     * @throws UnauthorizedException Wird geworfen, falls Benutzer nicht authorisiert ist die Daten
     *                               zu ändern
     * @throws OAuthRequestException Wird geworfen, falls kein User-Objekt übergeben wird
     */
    @ApiMethod(path = "update_user_picture")
    public UserPicture updateUserPicture(User user, UserPicture newUserPicture) throws
            UnauthorizedException, OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        } else if (!existsUser(user.getEmail()).value) {
            throw new JDOObjectNotFoundException("Benutzer konnte nicht gefunden werden");
        } else if (!user.getEmail().equals(newUserPicture.getEmail())) {
            throw new UnauthorizedException("Keine Rechte um diesen Benutzer zu editieren");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserPicture userPicture = pm.getObjectById(UserPicture.class, newUserPicture.getEmail());
            userPicture.setPicture(newUserPicture.getPicture());
            return userPicture;
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
    @ApiMethod(path = "update_user_location")
    public void updateUserLocation(User user, @Named("userMail") String userMail,
                                   @Named("latitude") double latitude,
                                   @Named("longitude") double longitude,
                                   @Named("currentEventId") long currentEventId) throws Exception {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        } else if (!existsUser(user.getEmail()).value) {
            createUser(userMail);
            //throw new JDOObjectNotFoundException("Benutzer konnte nicht gefunden werden");
        } else if (!user.getEmail().equals(userMail)) {
            throw new UnauthorizedException("Keine Rechte um diesen Benutzer zu editieren");
        }
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
            routeEndpoint.calculateCurrentWaypoint(pm, userLocation);
            if (System.currentTimeMillis()-lastFieldUpdateTime >= routeEndpoint.FIELD_UPDATE_INTERVAL) {
                routeEndpoint.calculateField(pm, userLocation.getCurrentEventId());
                lastFieldUpdateTime = System.currentTimeMillis();
            }
            pm.makePersistent(userLocation);
        } finally {
            pm.close();
        }
    }

    /**
     * Listet die allgemeinen Informationen zusammen mit den Profilbildern der Benutzer auf, dessen
     * E-Mail Adressen übergeben wurden.
     *
     * @param userMails
     * @return Liste von allgemeinen Informationen mit Profilbildern
     */
    @ApiMethod(path = "list_user_info_with_picture")
    public List<UserInfoPicture> listUserInfoWithPicture(@Named("userMails") List<String> userMails) {
        List<UserInfoPicture> result = new ArrayList<>();
        UserInfoPicture userInfoPicture;
        for (String userMail : userMails) {
            userInfoPicture = getUserInfoWithPicture(userMail);
            if (userInfoPicture != null) result.add(userInfoPicture);
        }
        return result;
    }

    /**
     * Listet die allgemeinen Informationen der Benutzer auf, dessen E-Mail Adressen übergeben
     * wurden.
     *
     * @param userMails Liste der E-Mail Adressen der Benutzer
     * @return Liste von allgemeinen Informationen
     */
    @ApiMethod(path = "list_user_infos")
    public List<UserInfo> listUserInfo(@Named("userMails") List<String> userMails) {
        List<UserInfo> result = new ArrayList<>();
        UserInfo userInfo;
        for (String userMail : userMails) {
            userInfo = getUserInfo(userMail);
            if (userInfo != null) result.add(userInfo);
        }
        return result;
    }

    /**
     * Gibt eine Liste von allen Nutzergruppen aus, bei denen der Benutzer als Mitglied eingetragen
     * ist.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @return Liste von beigetretenen oder erstellten Nutzergruppen
     * @throws Exception Wird geworfen, falls Benutzer mit der angegebenen E-Mail nicht existiert
     */
    @ApiMethod(path = "list_user_groups")
    public List<UserGroup> listUserGroups(@Named("userMail") String userMail) throws Exception {
        if (!existsUser(userMail).value) {
            throw new JDOObjectNotFoundException("Benutzer konnte nicht gefunden werden");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        List<String> userGroupIds = pm.getObjectById(EndUser.class, userMail).getMyUserGroups();
        pm.close();

        UserGroup userGroup;
        List<UserGroup> result = new ArrayList<>();
        for (String userGroupId : userGroupIds) {
            userGroup = new GroupEndpoint().getUserGroup(userGroupId);
            if (userGroup != null) result.add(userGroup);
        }
        return result;
    }


    /**
     * Gibt eine Liste von allen Veranstaltungen aus, an denen der Benutzer teilgenommen hat bzw.
     * teilnimmt.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @return Liste von Veranstaltungen des Benutzers
     * @throws Exception Wird geworfen, falls Benutzer mit der angegebenen E-Mail nicht existiert
     */
    @ApiMethod(path = "list_events")
    public List<Event> listEvents(@Named("userMail") String userMail) throws Exception {
        if (!existsUser(userMail).value) {
            throw new JDOObjectNotFoundException("Benutzer konnte nicht gefunden werden");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        List<Long> eventIds = pm.getObjectById(EndUser.class, userMail).getMyEvents();

        Event event;
        List<Event> result = new ArrayList<>();
        for (long key : eventIds) {
            event = new EventEndpoint().getEvent(key);
            if (event != null) result.add(event);
        }
        return result;
    }

    /**
     * Durchsucht die Benutzer nach der Eingabe und gibt die allgemeinen Nutzerinformationen
     * zu den Ergebnissen in einer Liste aus.
     *
     * @param input Eingabe
     * @return Liste von allgemeinen Informationen zu Benutzern
     */
    public List<UserInfo> searchUsers(@Named("input") String input){
        List<UserInfo> result;
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Query q = pm.newQuery(UserInfo.class);
            q.setFilter("email == :inputEmail");
            result = (List<UserInfo>) q.execute(Arrays.asList(input));
            return result;
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
    public void deleteUser(@Named("userMail") String userMail) {
        if (!existsUser(userMail).value) {
            throw new JDOObjectNotFoundException("Benutzer konnte nicht gefunden werden");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        EndUser endUser = pm.getObjectById(EndUser.class, userMail);
        try {
            pm.deletePersistent(endUser.getUserInfoPicture().getUserPicture());
            pm.deletePersistent(endUser.getUserInfoPicture().getUserInfo());
            pm.deletePersistent(endUser.getUserInfoPicture());
            pm.deletePersistent(endUser.getUserLocation());
            pm.deletePersistent(endUser);
        } finally {
            pm.close();
        }

        pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            GlobalDomain globalDomain = new RoleEndpoint().getGlobalDomain();
            globalDomain.removeRole(userMail);
            pm.makePersistent(globalDomain);
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
