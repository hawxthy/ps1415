package ws1415.SkatenightBackend;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.model.Right;
import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.model.UserGroup.BoardEntry;

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

    /**
     * Exception wird geworfen, falls Benutzer mit dem eingeloggten Benutzer nicht übereinstimmt
     *
     * @param mailLoggedIn eingeloggter Benutzer
     * @param mailUserToChange Benutzer der abgerufen/geändert wird
     * @throws UnauthorizedException
     */
    public static void throwIfUserNotSame(String mailLoggedIn, String mailUserToChange) throws UnauthorizedException {
        if (!mailLoggedIn.equals(mailUserToChange)) {
            throw new UnauthorizedException("rights missing to perform the action");
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
            throw new IllegalArgumentException("user group with submitted name "+groupName+ " could not be found");
        }
        return group;
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
            throw new IllegalArgumentException("user group with submitted name "+groupName+ " already exists");
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
            throw new IllegalArgumentException("user "+email+ " is not creator of group");
        }
    }

    /**
     * Exception wird geworfen, falls die übergebene Message leer oder null ist.
     *
     * @param message
     */
    public static void throwIfNotBoardMessageSubmitted(String message){
        if(message == null || message.isEmpty()){
            throw new IllegalArgumentException("no message for BoardEntry submitted");
        }
    }

    /**
     * Exception wird geworfen, falls die übergebene BoardEntry id leer oder null ist.
     *
     * @param boardEntryId
     */
    public static void throwIfNoBoardEntryIdISubmitted(Long boardEntryId){
        if (boardEntryId == null || boardEntryId == 0) {
            throw new NullPointerException("no entry to delete submitted");
        }
    }

    /**
     * Exception wird geworfen, falls kein BoardEntry submitted wurde.
     *
     * @param be
     */
    public static void throwIfNoBoardEntrySubmitted(BoardEntry be){
        if(be == null){
            throw new IllegalArgumentException("no BoardEntry submitted");
        }
    }

    /**
     * Exception um zu informieren, dass der Aufrufer keine Rechte hat.
     *
     * @throws IllegalAccessException
     */
    public static void throwIfNoRights(){
        throw new IllegalArgumentException("the user doesn't have the needed rights to do this action");
    }

    /**
     * Exception wird geworfen, falls eine Mitglied einer Nutzergruppe versicht
     * diese erneut zu betreten.
     *
     * @param group Die Nutzergruppe
     * @param email Der EndUser
     */
    public static void throwIfUserAlreadyInGroup(UserGroup group, String email){
        if(group.getMemberRights().keySet().contains(email)){
            throw new IllegalArgumentException("the user "+email+ " is already a member of they group");
        }
    }

    /**
     * Exception wird geworfen, fall der Leader eine Nutzergruppe versucht diese
     * zu verlassen.
     *
     * @param group Die Nutzergruppe
     * @param email Der EndUser
     */
    public static void throwIfLeaderTriesToLeaveGroup(UserGroup group, String email){
        if(group.getMemberRights().values().contains(Right.FULLRIGHTS.name())){
            throw new IllegalArgumentException("the leader of a group can't leave the group");
        }
    }

    /**
     * Exception wird geworfen, falls der EndUser nicht in der Nutzergruppe als
     * Mitglieg eigetragen ist.
     *
     * @param group Die Nutzergruppe
     * @param email Der EndUser
     */
    public static void throwIfUserNotInGroup(UserGroup group, String email){
        if(!group.getMemberRights().keySet().contains(email)){
            throw new IllegalArgumentException("the submitted user "+email+ " is not part of this group "+group.getName());
        }
    }
}
