package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.GlobalRole;
import ws1415.SkatenightBackend.model.UserProfile;

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
    public BooleanWrapper isAdmin(@Named("email") String email){
        if(!new UserEndpoint().existsUser(email).value){
            return new BooleanWrapper(false);
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try{
            EndUser endUser = pm.getObjectById(EndUser.class, email);
            if(endUser.getGlobalRole().equals(GlobalRole.ADMIN.getId())){
                return new BooleanWrapper(true);
            } else{
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
    public List<UserProfile> listGlobalAdmins(User user) throws OAuthRequestException {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        List<EndUser> adminUsers;
        List<UserProfile> result = new ArrayList<>();
        try {
            Query q = pm.newQuery(EndUser.class);
            q.setFilter("globalRole == p1");

            adminUsers = (List<EndUser>) pm.newQuery(q).execute(GlobalRole.ADMIN.getId());
            for(EndUser adminUser : adminUsers){
                result.add(new UserEndpoint().getUserProfile(user, adminUser.getEmail()));
            }
            return result;
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
    public void assignGlobalRole(User user, @Named("userMail") String userMail, @Named("role") int role) throws OAuthRequestException, UnauthorizedException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        } else if(!isAdmin(user.getEmail()).value){
            throw new UnauthorizedException("user is not an admin");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            EndUser endUser = pm.getObjectById(EndUser.class, userMail);
            endUser.setGlobalRole(role);
        } finally {
            pm.close();
        }
    }
}
