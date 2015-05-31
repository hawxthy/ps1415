package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiNamespace;

import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.UserGroup;

/**
 * Stellt Hilfsmethoden bereit, die von allen Endpoints der Skatenight-API benötigt werden.
 */
@Api(name = "skatenightAPI",
        version = "v1",
        clientIds = {Constants.ANDROID_USER_CLIENT_ID, Constants.ANDROID_HOST_CLIENT_ID,
                Constants.WEB_CLIENT_ID, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = "skatenight.com", ownerName = "skatenight"))
public abstract class SkatenightServerEndpoint {
    private PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(
            "transactions-optional");

    protected PersistenceManagerFactory getPersistenceManagerFactory() {
        return pmf;
    }

    /**
     * Ruft über den angegebenen PersistenceManager den RegistrationManager aus der Datenbank ab.
     *
     * @param pm Der zu verwendende PersistenceManager.
     * @return Gibt den RegistrationManager zurück, der in der Datenbank gespeichert ist.
     */
    protected RegistrationManager getRegistrationManager(PersistenceManager pm) {
        RegistrationManager registrationManager;
        Query q = pm.newQuery(RegistrationManager.class);
        List<RegistrationManager> result = (List<RegistrationManager>) q.execute();
        if (!result.isEmpty()) {
            registrationManager = result.get(0);
        } else {
            registrationManager = new RegistrationManager();
        }
        return registrationManager;
    }

    /**
     * Prüft ob ein Benutzer mit einem anderen Benutzer befreundet ist.
     *
     * @param userMail E-Mail Adresse des Benutzers mit der Freundeliste
     * @param mailToCheck E-Mail Adresse des Benutzers, dessen Freundschaft geprüft wird
     * @return true, falls Freundschaft besteht, false andernfalls
     */
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

    /**
     * In der Liste der beigetretenen Gruppen des Benutzers wird die übergebene Gruppe hinzugefügt.
     *
     * @param userMail E-Mail des Benutzers
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
     * @param userMail E-Mail des Benutzers
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
