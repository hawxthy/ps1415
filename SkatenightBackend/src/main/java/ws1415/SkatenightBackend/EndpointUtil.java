package ws1415.SkatenightBackend;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.NotFoundException;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.UserGroup;

/**
 * Created by Martin on 13.05.2015.
 */
public class EndpointUtil {

    /**
     * Exception wird geworfen, falls kein gültiges User-Objekt übergeben wird.
     *
     * @param user
     * @throws OAuthRequestException
     */
    public static void throwIfNoUser(User user) throws OAuthRequestException {
        if (user == null || user.getEmail() == null) {
            throw new OAuthRequestException("no user submitted");
        }
    }

    /**
     * Exception wird geworfen, falls kein EndUser für die gegebene E-Mail existiert.
     *
     * @param email E-Mail Adresse des zu prüfenden EndUsers
     */
    public static void throwIfEndUserNotExists(String email){
        if (!new UserEndpoint().existsUser(email).value) {
            throw new JDOObjectNotFoundException("end user could not be found: " + email);
        }
    }

    /**
     * Exception wird geworfen, falls kein gültiges UserGroup-Objekt übergeben wird. Oder
     * keine UserGroup zu dem angegebenen Namen existiert.
     *
     * @param groupName
     * @throws OAuthRequestException
     */
    public static UserGroup throwIfNoUserGroupExists(String groupName) throws OAuthRequestException {
        if (groupName == null || groupName.isEmpty()) {
            throw new OAuthRequestException("no group name submitted");
        }
        UserGroup group = new GroupEndpoint().getUserGroup(groupName);
        if(group == null){
            throw new IllegalArgumentException("user group with submitted name could not be found");
        }
        return group;
    }

    /**
     * Exception wird geworfen, falls Rechte fehlen um Benutzerdaten zu ändern/abzurufen.
     */
    public static void throwIfNoRights(String mailLoggedIn, String mailUserToChange) throws UnauthorizedException {
        if (!mailLoggedIn.equals(mailUserToChange) && !new RoleEndpoint().isAdmin(mailLoggedIn).value) {
            throw new UnauthorizedException("rights missing to perform the action");
        }
    }

    /**
     * Exception wird geworfen, fall es schon eine UserGroup mit dem übergebenen
     * Namen gibt.
     *
     * @param groupName
     * @throws IllegalArgumentException
     */
    public static void throwIfUserGroupAlreadyExists(String groupName){
        if(new GroupEndpoint().getUserGroupMetaData(groupName) != null){
           throw new IllegalArgumentException("user group with submitted name already exists");
        }
    }

    /**
     * Exception wird geworfen, falls kein UserGroup Name übergeben wurde.
     *
     * @param groupName
     */
    public static void throwIfUserGroupNameWasntSubmitted(String groupName){
        if(groupName == null || groupName.isEmpty()){
            throw new IllegalArgumentException("no group name submitted");
        }
    }

    /**
     * Exception wird geworfen, falls die E-Mail nicht als creator in der
     * Nutzergruppe eingetragen ist.
     *
     * @param group
     * @param email
     */
    public static void throwIfNotCreatorOfUserGroup(UserGroup group, String email){
        if (!group.getCreator().equals(email)) {
            throw new IllegalArgumentException("user is not creator of group");
        }
    }
}
