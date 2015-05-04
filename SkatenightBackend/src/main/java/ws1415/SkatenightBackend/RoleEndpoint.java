package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import ws1415.SkatenightBackend.model.Domain;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.GlobalDomain;
import ws1415.SkatenightBackend.model.Role;
import ws1415.SkatenightBackend.model.RoleWrapper;

/**
 * Der RoleEndpoint stellt Hilfsmethoden bereit, die genutzt werden um die Rollen der Benutzer
 * zu verwalten.
 *
 * @author Martin Wrodarczyk
 */
public class RoleEndpoint extends SkatenightServerEndpoint {
    /**
     * Setzt die Rolle für einen Benutzer in einer Umgebung.
     *
     * @param user User-Objekt zur Authentifizierung
     * @param userMail E-Mail Adresse des Benutzers
     * @param dom Umgebung
     * @param role Rolle
     */
    public void setUserRole(User user, @Named("userMail") String userMail, Domain dom ,@Named("role") int role) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        else if(!new UserEndpoint().getFullUser(user.getEmail()).getRole(dom.getKey()).equals(dom.getAdminRole())){
            throw new OAuthRequestException("Rechte um Rollen zu ändern fehlen");
        }
        else if(!new UserEndpoint().existsUser(userMail).value){
            throw new JDOObjectNotFoundException("Benutzer wurde nicht gefunden");
        }
        else {
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
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
    public void deleteUserRole(User user, @Named("userMail") String userMail, Domain dom) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        else if(!new UserEndpoint().getFullUser(user.getEmail()).getRole(dom.getKey()).equals(dom.getAdminRole())){
            throw new OAuthRequestException("Rechte um User aus Umgebung zu löschen fehlen");
        }
        else if(!new UserEndpoint().existsUser(userMail).value){
            throw new JDOObjectNotFoundException("Benutzer wurde nicht gefunden");
        }
        else {
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            endUser.removeRole(dom.getKey());
        }
    }

    /**
     * Liefert ein Objekt, dass die globale Umgebung repräsentiert. Wird zur Verwaltung der Rollen
     * der User verwendet.
     *
     * @return Globale Umgebung
     */
    public GlobalDomain getGlobalDomain(){
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        Query q = pm.newQuery(GlobalDomain.class);
        List<GlobalDomain> listGlobalDomain = (List<GlobalDomain>) q.execute();

        if(listGlobalDomain.size() != 0){
            return listGlobalDomain.get(0);
        } else {
            List<Integer> possibleRoles = new ArrayList<>();
            possibleRoles.add(Role.ADMIN.getId());
            possibleRoles.add(Role.USER.getId());
            Integer adminRole = Role.ADMIN.getId();
            GlobalDomain globalDomain = new GlobalDomain(possibleRoles, adminRole);
            try {
                return pm.makePersistent(globalDomain);
            } finally {
                pm.close();
            }
        }
    }

    /**
     * Liefert die globale Rolle eines Benutzers.
     *
     * @param userMail
     * @return
     */
    @ApiMethod(path="get_global_role")
    public RoleWrapper getGlobalRole(@Named("userMail") String userMail){
        GlobalDomain globalDomain = getGlobalDomain();
        return getUserRole(userMail, globalDomain);
    }

    /**
     * Setzt die globale Rolle eines Benutzers.
     *
     * @param user User-Objekt zur Authentifizierung
     * @param userMail E-Mail Adresse des Benutzers
     * @param role neue Rolle
     * @throws OAuthRequestException
     */
    public void changeGlobalRole(User user, @Named("userMail") String userMail,@Named("role") int role) throws OAuthRequestException {
        GlobalDomain globalDomain = getGlobalDomain();
        if(!globalDomain.getPossibleRoles().contains(role)){
            throw new IllegalArgumentException("Rolle ist für die Umgebung nicht gültig");
        }
        setUserRole(user, userMail, globalDomain, role);
    }
}
