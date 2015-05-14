package ws1415.SkatenightBackend;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import javax.jdo.JDOObjectNotFoundException;

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
     * Exception wird geworfen, falls Rechte fehlen um Benutzerdaten zu ändern/abzurufen.
     */
    public static void throwIfNoRights(String mailLoggedIn, String mailUserToChange) throws UnauthorizedException {
        if (!mailLoggedIn.equals(mailUserToChange) && !new RoleEndpoint().isAdmin(mailLoggedIn).value) {
            throw new UnauthorizedException("rights missing to perform the action");
        }
    }
}
