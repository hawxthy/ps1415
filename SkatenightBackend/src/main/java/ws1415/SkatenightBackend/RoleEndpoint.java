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
     * Gibt die Rolle eines Benutzers innerhalb einer Umgebung aus.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @param dom      Umgebung
     * @return Rolle des Benutzers
     */
    public RoleWrapper getUserRole(@Named("userMail") String userMail, Domain dom) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Domain domain = pm.getObjectById(Domain.class, dom.getKey());
            if (!dom.existsUser(userMail)) {
                throw new JDOObjectNotFoundException("Benutzer wurde nicht gefunden");
            } else {
                Integer role = domain.getRole(userMail);
                return new RoleWrapper(role);
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Setzt die Rolle für einen Benutzer in einer Umgebung.
     *
     * @param user     User-Objekt zur Authentifizierung
     * @param userMail E-Mail Adresse des Benutzers
     * @param dom      Umgebung
     * @param role     Rolle
     */
    public Domain assignUserRole(User user, @Named("userMail") String userMail, Domain dom, @Named("role") int role) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            dom = pm.getObjectById(Domain.class, dom.getKey());
            if (!(dom.getRole(user.getEmail()).equals(dom.getAdminRole()))) {
                throw new OAuthRequestException("Rechte um Rollen zu ändern fehlen");
            } else if (!dom.existsUser(userMail)) {
                throw new JDOObjectNotFoundException("Benutzer wurde nicht gefunden");
            } else if (!dom.getPossibleRoles().contains(role)) {
                throw new IllegalArgumentException("Rolle ist für die Umgebung nicht gültig");
            } else {
                dom.setRole(userMail, role);
                pm.makePersistent(dom);
                return dom;
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Löscht einen Benutzer aus einer Umgebung, sodass dieser keine Rolle mehr in dieser Umgebung
     * besitzt.
     *
     * @param userMail E-Mail Adresse des Benutzers
     * @param dom      Umgebung
     */
    public Domain deleteUserRole(User user, @Named("userMail") String userMail, Domain dom) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            dom = pm.getObjectById(Domain.class, dom.getKey());
            if (!(dom.getRole(user.getEmail()).equals(dom.getAdminRole()))) {
                throw new OAuthRequestException("Rechte um Rolle zu löschen fehlen");
            } else if (!dom.existsUser(userMail)) {
                throw new JDOObjectNotFoundException("Benutzer wurde nicht gefunden");
            } else {
                dom.removeRole(userMail);
                pm.makePersistent(dom);
                return dom;
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Liefert ein Objekt, dass die globale Umgebung repräsentiert. Wird zur Verwaltung der Rollen
     * der User verwendet.
     *
     * @return Globale Umgebung
     */
    public GlobalDomain getGlobalDomain() {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        Query q = pm.newQuery(GlobalDomain.class);
        List<GlobalDomain> listGlobalDomain = (List<GlobalDomain>) q.execute();
        try {
            if (listGlobalDomain.size() != 0) {
                listGlobalDomain.get(0).getUserRoles();
                listGlobalDomain.get(0).getPossibleRoles();
                return listGlobalDomain.get(0);
            } else {
                List<Integer> possibleRoles = new ArrayList<>();
                possibleRoles.add(Role.ADMIN.getId());
                possibleRoles.add(Role.USER.getId());
                Integer adminRole = Role.ADMIN.getId();
                GlobalDomain globalDomain = new GlobalDomain(possibleRoles, adminRole);
                return pm.makePersistent(globalDomain);
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Liefert die globale Rolle eines Benutzers.
     *
     * @param userMail
     * @return
     */
    @ApiMethod(path = "get_global_role")
    public RoleWrapper getGlobalRole(@Named("userMail") String userMail) {
        GlobalDomain globalDomain = getGlobalDomain();
        return new RoleWrapper(globalDomain.getRole(userMail));
    }

    /**
     * Setzt die globale Rolle eines Benutzers.
     *
     * @param user     User-Objekt zur Authentifizierung
     * @param userMail E-Mail Adresse des Benutzers
     * @param role     neue Rolle
     * @throws OAuthRequestException
     */
    public void assignGlobalRole(User user, @Named("userMail") String userMail, @Named("role") int role) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        GlobalDomain globalDomain = getGlobalDomain();
        if (!globalDomain.getPossibleRoles().contains(role)) {
            throw new IllegalArgumentException("Rolle ist für die Umgebung nicht gültig");
        } else if (!(globalDomain.getRole(user.getEmail()).equals(globalDomain.getAdminRole()))) {
            throw new OAuthRequestException("Rechte um Rollen zu ändern fehlen");
        } else if (!globalDomain.existsUser(userMail)) {
            throw new JDOObjectNotFoundException("Benutzer wurde nicht gefunden");
        } else {
            globalDomain.setRole(userMail, role);
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            try {
                pm.makePersistent(globalDomain);
            } finally {
                pm.close();
            }
        }
    }
}
