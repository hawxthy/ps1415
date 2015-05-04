package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.Domain;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.Event;
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
        if (!existsUser(email).value) {
            UserLocation userLocation = new UserLocation(email);
            UserPicture userPicture = new UserPicture(email);
            UserInfo userInfo = new UserInfo(email);
            UserInfoPicture userInfoPicture = new UserInfoPicture(email, userInfo, userPicture);
            EndUser user = new EndUser(email, userInfoPicture, userLocation);

            Domain globalDomain = new RoleEndpoint().getGlobalDomain();
            user.setRole(globalDomain.getKey(), Role.USER.getId());

            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            try {
                pm.makePersistent(user);
            } finally {
                pm.close();
            }
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
        EndUser user = getFullUser(email);
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
        EndUser endUser;
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            try {
                endUser = pm.getObjectById(EndUser.class, email);
                // Stellt sicher das Objekte der Assoziation auch runtergeladen werden
                endUser.getUserInfoPicture();
                endUser.getUserInfoPicture().getUserPicture();
                endUser.getUserInfoPicture().getUserInfo();
                endUser.getUserLocation();
            } catch (Exception e) {
                // User mit der Email wurde nicht gefunden
                return null;
            }
        } finally {
            pm.close();
        }
        return endUser;
    }

    /**
     * Gibt die Standordinformationen eines Benutzers mit der angegebenen E-Mail Adresse aus.
     *
     * @param email E-Mail Adresse des Benutzers
     * @return Standortinformationen zum Benutzer
     */
    @ApiMethod(path = "get_user_location")
    public UserLocation getUserLocation(@Named("email") String email) {
        UserLocation userLocation;
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            try {
                userLocation = pm.getObjectById(UserLocation.class, email);
            } catch (Exception e) {
                // UserLocation mit der Email wurde nicht gefunden
                return null;
            }
        } finally {
            pm.close();
        }
        return userLocation;
    }

    /**
     * Gibt die allgemeinen Informationen zu einem Benutzer mit der angegebenen E-Mail Adresse
     * aus.
     *
     * @param email E-Mail Adresse des Benutzers
     * @return Allgemeine Informationen zum Benutzer
     */
    @ApiMethod(path = "get_user_info")
    public UserInfo getUserInfo(@Named("email") String email) {
        UserInfo userInfo;
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            try {
                userInfo = pm.getObjectById(UserInfo.class, email);
            } catch (Exception e) {
                return null;
            }
        } finally {
            pm.close();
        }
        return userInfo;
    }

    /**
     * Gibt die allgemeinen Informationen und das Profilbild zu einem Benutzer mit der angegebenen
     * E-Mail Adresse aus.
     *
     * @param email
     * @return Informationen und Profilbild vom Benutzer
     */
    @ApiMethod(path = "get_user_info_with_picture")
    public UserInfoPicture getUserInfoWithPicture(@Named("email") String email) {
        UserInfoPicture userInfoPicture;
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            try {
                userInfoPicture = pm.getObjectById(UserInfoPicture.class, email);
                userInfoPicture.getUserInfo();
                userInfoPicture.getUserPicture();
            } catch (Exception e) {
                return null;
            }
        } finally {
            pm.close();
        }
        return userInfoPicture;
    }

    /**
     * Gibt das Profilbild des Benuters mit der angegebenen E-Mail Adresse aus.
     *
     * @param email E-Mail Adresse des Benutzers
     * @return Profilbild des Benutzers
     */
    @ApiMethod(path = "get_user_picture")
    public UserPicture getUserPicture(@Named("email") String email) {
        UserPicture userPicture;
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            try {
                userPicture = pm.getObjectById(UserPicture.class, email);
            } catch (Exception e) {
                return null;
            }
        } finally {
            pm.close();
        }
        return userPicture;
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
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }

        UserInfo userInfo;
        //if (user.getEmail().equals(newUserInfo.getEmail())) {
        try {
            userInfo = pm.getObjectById(UserInfo.class, newUserInfo.getEmail());
            userInfo.setFirstName(newUserInfo.getFirstName());
            userInfo.setLastName(newUserInfo.getLastName());
            userInfo.setCity(newUserInfo.getCity());
            userInfo.setDateOfBirth(newUserInfo.getDateOfBirth());
            userInfo.setDescription(newUserInfo.getDescription());
            userInfo.setGender(newUserInfo.getGender());
            userInfo.setPostalCode(newUserInfo.getPostalCode());
        } finally {
            pm.close();
        }
        //} else {
        //    throw new UnauthorizedException("Keine Rechte um diesen Benutzer zu editieren");
        //}
        return userInfo;
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
        }
        UserPicture userPicture;
        if (user.getEmail().equals(newUserPicture.getEmail())) {
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            try {
                userPicture = pm.getObjectById(UserPicture.class, newUserPicture.getEmail());
                userPicture.setPicture(newUserPicture.getPicture());
            } finally {
                pm.close();
            }
        } else {
            throw new UnauthorizedException("Keine Rechte um diesen Benutzer zu editieren");
        }
        return userPicture;
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
        }
        if (user.getEmail().equals(userMail)) {
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            try {
                UserLocation userLocation = pm.getObjectById(UserLocation.class, userMail);
                userLocation.setUpdatedAt(new Date());
                userLocation.setLatitude(latitude);
                userLocation.setLongitude(longitude);
                userLocation.setCurrentEventId(currentEventId);
            } finally {
                pm.close();
            }
        } else {
            throw new UnauthorizedException("Keine Rechte um diesen Benutzer zu editieren");
        }
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
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        List<UserInfo> result = new ArrayList<>();
        try {
            UserInfo userInfo;
            for (String userMail : userMails) {
                userInfo = getUserInfo(userMail);
                if (userInfo != null) result.add(userInfo);
            }
        } finally {
            pm.close();
        }
        return result;
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
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        List<UserInfoPicture> result = new ArrayList<>();
        try {
            UserInfoPicture userInfoPicture;
            for (String userMail : userMails) {
                userInfoPicture = getUserInfoWithPicture(userMail);
                if (userInfoPicture != null) result.add(userInfoPicture);
            }
        } finally {
            pm.close();
        }
        return result;
    }

    /**
     * Listet die Profilbilder der Benutzer, dessen E-Mail Adressen übergeben wurden.
     *
     * @param userMails Liste der E-Mail Adressen der Benutzer
     * @return Liste von Profilbildern
     */
    @ApiMethod(path = "list_user_picture")
    public List<UserPicture> listUserPicture(@Named("userMails") List<String> userMails) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        List<UserPicture> result = new ArrayList<>();
        try {
            UserPicture userPicture;
            for (String userMail : userMails) {
                userPicture = getUserPicture(userMail);
                if (userPicture != null) result.add(userPicture);
            }
        } finally {
            pm.close();
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
        if (existsUser(userMail).value) {
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            List<UserGroup> result = new ArrayList<>();
            try {
                List<String> userGroupIds = getFullUser(userMail).getMyUserGroups();
                UserGroup userGroup;
                for (String userGroupId : userGroupIds) {
                    userGroup = new GroupEndpoint().getUserGroup(userGroupId);
                    if (userGroup != null) result.add(userGroup);
                }
            } finally {
                pm.close();
            }
            return result;
        } else {
            throw new Exception("Benutzer existiert nicht");
        }
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
        if (existsUser(userMail).value) {
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            List<Event> result = new ArrayList<>();
            try {
                List<Key> eventIds = getFullUser(userMail).getMyEvents();
                Event event;
                for (Key key : eventIds) {
                    event = new EventEndpoint().getEvent(key.getId());
                    if (event != null) result.add(event);
                }
            } finally {
                pm.close();
            }
            return result;
        } else {
            throw new Exception("Benutzer existiert nicht");
        }
    }

    /**
     * Löscht einen Benutzer auf dem Server, falls dieser vorhanden ist. Wird zur Zeit nur für
     * die Tests verwendet.
     *
     * @param email E-Mail Adresse des Benutzers
     */
    public void deleteUser(@Named("email") String email) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        if (existsUser(email).value) {
            EndUser endUser;
            try {
                endUser = pm.getObjectById(EndUser.class, email);
                pm.deletePersistent(endUser.getUserInfoPicture().getUserPicture());
                pm.deletePersistent(endUser.getUserInfoPicture().getUserInfo());
                pm.deletePersistent(endUser.getUserInfoPicture());
                pm.deletePersistent(endUser.getUserLocation());
                pm.deletePersistent(endUser);

                // Lösche aus Teilnehmerlisten
                //List<Key> eventIds = endUser.getMyEvents();
                //Event event;
                //for (Key eventId : eventIds) {
                //   event = new EventEndpoint().getEvent(eventId.getId());
                // event.removeMember(email)
                // pm.makePersistent(event);
                //}

                //List<String> groupIds = endUser.getMyUserGroups();
                //UserGroup userGroup;
                //for (String groupId : groupIds) {
                //   userGroup = new GroupEndpoint().getUserGroup(groupId);
                // userGroup.removeMember(email);
                // pm.makePersistent(userGroup);
                //}

            } catch (Exception e) {
                throw new JDOObjectNotFoundException("User wurde nicht gefunden");
            } finally {
                pm.close();
            }
        } else {
            throw new JDOObjectNotFoundException("User wurde nicht gefunden");
        }
    }
}
