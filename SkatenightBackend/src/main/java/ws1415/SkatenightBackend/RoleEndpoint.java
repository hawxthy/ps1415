package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import jdk.nashorn.internal.objects.Global;
import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.GlobalRole;
import ws1415.SkatenightBackend.transport.ListWrapper;

/**
 * Der RoleEndpoint stellt Hilfsmethoden bereit, die genutzt werden um die Rollen der Benutzer
 * zu verwalten.
 *
 * @author Martin Wrodarczyk
 */
public class RoleEndpoint extends SkatenightServerEndpoint {

    /**
     * Prüft, ob ein Benutzer mit der angegebenen E-Mail Adresse ein Administrator ist.
     *
     * @param email E-Mail Adresse des Benutzers
     * @return true, falls Benutzer ein Administrator ist, false andernfalls
     */
    @ApiMethod(path = "global_admin")
    public BooleanWrapper isAdmin(@Named("email") String email) {
        if (!new UserEndpoint().existsUser(email).value) {
            return new BooleanWrapper(false);
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, email);
            if (endUser.getGlobalRole().equals(GlobalRole.ADMIN)) {
                return new BooleanWrapper(true);
            } else {
                return new BooleanWrapper(false);
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt eine Liste von Benutzern aus, die Administratoren sind.
     *
     * @return Liste von Administratoren
     */
    @ApiMethod(path = "global_admin")
    public ListWrapper listGlobalAdmins(User user) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        List<String> result;
        try {
            Query q = pm.newQuery(EndUser.class);
            q.setFilter("globalRole == roleParam");
            q.declareParameters("GlobalRole roleParam");
            q.setResult("this.email");

            result = (List<String>) q.execute(GlobalRole.ADMIN);
            return new ListWrapper(result);
        } finally {
            pm.close();
        }
    }

    /**
     * Setzt die globale Rolle eines Benutzers.
     *
     * @param user     User-Objekt zur Authentifizierung
     * @param userMail E-Mail Adresse des Benutzers
     * @param role     Id neuer Rolle
     * @throws OAuthRequestException
     */
    @ApiMethod(path = "global_role")
    public BooleanWrapper assignGlobalRole(User user, @Named("userMail") String userMail, @Named("role") int role) throws OAuthRequestException, UnauthorizedException {
        EndpointUtil.throwIfNoUser(user);
        if (!isAdmin(user.getEmail()).value) {
            throw new UnauthorizedException("user is not an admin");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            endUser.setGlobalRole(GlobalRole.getValue(role));
            return new BooleanWrapper(true);
        } catch (Exception e) {
            return new BooleanWrapper(false);
        } finally {
            pm.close();
        }
    }
}
