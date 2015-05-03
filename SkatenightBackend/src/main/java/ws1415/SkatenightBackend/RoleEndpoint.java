package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.model.Domain;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.RoleWrapper;

/**
 * Der RoleEndpoint stellt Hilfsmethoden bereit, die genutzt werden um die Rollen der Benutzer
 * zu verwalten.
 *
 * TODO: Authentifikation um Rollen zu setzen
 *
 * @author Martin Wrodarczyk
 */
public class RoleEndpoint extends SkatenightServerEndpoint {
    /**
     * Setzt die Rolle für einen Benutzer in einer Umgebung.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @param dom Umgebung
     * @param role Rolle
     */
    public void setUserRole(@Named("userMail") String userMail, Domain dom ,@Named("role") int role) {
        if(!new UserEndpoint().existsUser(userMail).value){
            throw new JDOObjectNotFoundException("Benutzer wurde nicht gefunden");
        }
        else {
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            //Testzwecke
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            endUser.setRole(dom.getKey(), role);
        }
    }

    /**
     * Gibt die Rolle eines Benutzers innerhalb einer Umgebung aus.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @param dom Umgebung
     * @return Rolle des Benutzers
     */
    public RoleWrapper getUserRole(@Named("userMail") String userMail, Domain dom){
        if(!new UserEndpoint().existsUser(userMail).value){
            throw new JDOObjectNotFoundException("Benutzer wurde nicht gefunden");
        }
        else {
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            Integer role = endUser.getRole(dom.getKey());
            return new RoleWrapper(role);
        }
    }

    /**
     * Löscht einen Benutzer aus einer Umgebung, sodass dieser keine Rolle mehr in dieser Umgebung
     * besitzt.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @param dom Umgebung
     */
    public void deleteUserRole(@Named("userMail") String userMail, Domain dom){
        if(!new UserEndpoint().existsUser(userMail).value){
            throw new JDOObjectNotFoundException("Benutzer wurde nicht gefunden");
        }
        else {
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            endUser.removeRole(dom.getKey());
        }
    }
}
