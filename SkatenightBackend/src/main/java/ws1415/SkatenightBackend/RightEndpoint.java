package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import ws1415.SkatenightBackend.model.Right;
import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.transport.ListWrapper;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Bernd Eissing on 03.05.2015.
 */
public class RightEndpoint extends SkatenightServerEndpoint {

    /**
     * Gibt einem Mitglied einer Nutzergruppe ein neues Recht, falls der übergebene
     * userName ein Mitglied dieser ist und der user die nötigen Rechte hat. Falls
     * der userName das Recht bereits hat geschieht nichts.
     *
     * @param user      Der EndUser, der das Recht verteilt
     * @param groupName Die Nutzergruppe
     * @param rightName Der Name des Rechts
     * @param userName  Der EndUser der ein neues Recht erhalten soll
     * @throws OAuthRequestException
     * @throws IllegalArgumentException
     */
    public void distributeRightToUser(User user, @Named("groupName") String groupName, @Named("rightName") String rightName, @Named("userName") String userName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = EndpointUtil.throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            EndpointUtil.throwIfUserNotInGroup(group, userName);
            if (!group.getMemberRights().get(userName).contains(rightName) && isRight(rightName)) {
                group.getMemberRights().get(userName).add(rightName);
                ofy().save().entity(group).now();
            }
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Gibt einem Mitglied einer Nutzergruppe neue Rechte, falls der übergebene
     * userName ein Mitglied dieser ist und der user die nötigen Rechte hat.
     * Dabei werden nur die Rechte hinzugefügt, die der userName noch nicht hat.
     *
     * @param user       Der EndUser, der das Recht verteilt
     * @param groupName  Die Nutzergruppe
     * @param rightNames Die Namen der Rechte
     * @param userName   Der EndUser der neue Rechte erhalten soll
     * @throws OAuthRequestException
     * @throws IllegalArgumentException
     */
    public void distributeRightsToUser(User user, @Named("groupName") String groupName, @Named("rightNames") ArrayList<String> rightNames, @Named("userName") String userName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = EndpointUtil.throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            EndpointUtil.throwIfUserNotInGroup(group, userName);
            for (int i = 0; i < rightNames.size(); i++) {
                if (!group.getMemberRights().get(userName).contains(rightNames.get(i)) && isRight(rightNames.get(i))) {
                    group.getMemberRights().get(userName).add(rightNames.get(i));
                    ofy().save().entity(group).now();
                }
            }
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Gibt mehreren Mitgliedern einer Nutzergruppe ein neues Recht, falls die übergebenen
     * userNames Mitglieder dieser sind und der user die nötigen Rechte hat. Falls
     * ein userName schon das Recht hat, wird für diesen nichts getan.
     *
     * @param user      Der EndUser, der das Recht verteilt
     * @param groupName Die Nutzergruppe
     * @param rightName Der Name des Rechtes
     * @param userNames Die EndUser die ein neues Recht erhalten sollen
     * @throws OAuthRequestException
     * @throws IllegalArgumentException
     */
    public void distributeRightToUsers(User user, @Named("groupName") String groupName, @Named("rightName") String rightName, ListWrapper userNames) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = EndpointUtil.throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            for (int i = 0; i < userNames.stringList.size(); i++) {
                EndpointUtil.throwIfUserNotInGroup(group, userNames.stringList.get(i));
                if (!group.getMemberRights().get(userNames.stringList.get(i)).contains(rightName) && isRight(rightName)) {
                    group.getMemberRights().get(userNames.stringList.get(i)).add(rightName);
                }
            }
            ofy().save().entity(group).now();
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Gibt mehreren Mitgliedern einer Nutzergruppe neue Rechte, falls die übergebenen
     * userNames Mitglieder dieser sind und der user die nötigen Rechte hat. Falls
     * ein userName schon ein Recht hat, wird für diesen userName und diese
     * Recht nichts getan.
     *
     * @param user       Der EndUser, der die Rechte verteilt
     * @param groupName  Die Nutzergruppe
     * @param rightNames Die Namen der Rechte
     * @param userNames  Die EndUser die ein neue Rechte erhalten sollen
     * @throws OAuthRequestException
     * @throws IllegalArgumentException
     */
    public void distributeRightsToUsers(User user, @Named("groupName") String groupName, @Named("rightNames")  ArrayList<String> rightNames, @Named("userNames") ArrayList<String> userNames) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = EndpointUtil.throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            for (int i = 0; i < userNames.size(); i++) {
                EndpointUtil.throwIfUserNotInGroup(group, userNames.get(i));
                for (int j = 0; j < rightNames.size(); j++) {
                    if (!group.getMemberRights().get(userNames.get(i)).contains(rightNames.get(j)) && isRight(rightNames.get(j))) {
                        group.getMemberRights().get(userNames.get(i)).add(rightNames.get(j));
                    }
                }
            }
            ofy().save().entity(group).now();
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Entfernt ein Recht von einem Mitglied in der übergebenen Nutzergruppe.
     * Falls das Mitglied dieses Recht nicht hat geschieht nichts.
     * Dabei dürfen FULLRIGHTS und NEWMEMBERRIGHTS nicht entzogen werden.
     *
     * @param user      Der EndUser, der das Recht entzieht
     * @param groupName Die Nutzergruppe
     * @param rightName Der Name des Rechts
     * @param userName  Der EndUser dem ein Recht weggenommen werden soll
     * @throws OAuthRequestException
     */
    public void takeRightFromUser(User user, @Named("groupName") String groupName, @Named("rightName") String rightName, @Named("userName") String userName) throws OAuthRequestException {
        // TODO die ganzen Abfragen generell zu Ranks versuchen zu reduzieren
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = EndpointUtil.throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            EndpointUtil.throwIfUserNotInGroup(group, userName);
            if (!rightName.equals(Right.FULLRIGHTS.name()) && !rightName.equals(Right.NEWMEMBERRIGHTS.name()) && group.getMemberRights().get(userName).contains(rightName)) {
                group.getMemberRights().get(userName).remove(rightName);
                ofy().save().entity(group).now();
            }
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Entfernt Rechte von einem Mitglied in der übergebenen Nutzergruppe.
     * Falls das Mitglied eines dieser Rechet nicht hat geschieht für
     * dieses Recht nichts.Dabei dürfen FULLRIGHTS und NEWMEMBERRIGHTS
     * nicht entzogen werden.
     *
     * @param user       Der EndUser, der Rechte entzieht
     * @param groupName  Die Nutzergruppe
     * @param rightNames Die Namen der Rechte
     * @param userName   Der EndUser dem Rechtw weggenommen werden sollen
     * @throws OAuthRequestException
     */
    public void takeRightsFromUser(User user, @Named("groupName") String groupName, @Named("rightNames") List<String> rightNames, @Named("userName") String userName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = EndpointUtil.throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            EndpointUtil.throwIfUserNotInGroup(group, userName);
            for (String right : rightNames) {
                if (!right.equals(Right.FULLRIGHTS.name()) && !right.equals(Right.NEWMEMBERRIGHTS.name()) && group.getMemberRights().get(userName).contains(right)) {
                    group.getMemberRights().get(userName).remove(right);
                }
            }
            ofy().save().entity(group).now();
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Entfernt ein Recht von Mitgliedern in der übergebenen Nutzergruppe.
     * Falls ein Mitglied das Recht nicht hat passiert für deses Mitglied
     * nichts.Dabei dürfen FULLRIGHTS und NEWMEMBERRIGHTS
     * nicht entzogen werden.
     *
     * @param user      Der EndUser, der das Recht entzieht
     * @param groupName Die Nutzergruppe
     * @param rightName Der Name des Rechtes
     * @param userNames Die EndUser denen Rechte entzogen werden sollen
     * @throws OAuthRequestException
     * @throws IllegalArgumentException
     */
    public void takeRightFromUsers(User user, @Named("groupName") String groupName, @Named("rightName") String rightName, @Named("userNames") List<String> userNames) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = EndpointUtil.throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            for (String member : userNames) {
                EndpointUtil.throwIfUserNotInGroup(group, member);
                if (!rightName.equals(Right.FULLRIGHTS.name()) && !rightName.equals(Right.NEWMEMBERRIGHTS.name()) && group.getMemberRights().get(member).contains(rightName)) {
                    group.getMemberRights().get(member).remove(rightName);
                }
            }
            ofy().save().entity(group).now();
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Entfernt Rechte von Mitgliedern in der übergebenen Nutzergruppe.
     * Falls ein Mitglied ein Recht nicht hat passiert für deses Mitglied
     * und diese Recht nichts. Dabei dürfen FULLRIGHTS und NEWMEMBERRIGHTS
     * nicht entzogen werden.
     *
     * @param user       Der EndUser, der die Rechte entzieht
     * @param groupName  Die Nutzergruppe
     * @param rightNames Die Namen der Rechte
     * @param userNames  Die EndUser denen Rechte entzogen werden sollen
     * @throws OAuthRequestException
     * @throws IllegalArgumentException
     */
    public void takeRightsFromUsers(User user, @Named("groupName") String groupName, @Named("rightNames") List<String> rightNames, @Named("userNames") List<String> userNames) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = EndpointUtil.throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            for (String member : userNames) {
                EndpointUtil.throwIfUserNotInGroup(group, member);
                for (String right : rightNames) {
                    if (!right.equals(Right.FULLRIGHTS.name()) && !right.equals(Right.NEWMEMBERRIGHTS.name()) && group.getMemberRights().get(member).contains(right)) {
                        group.getMemberRights().get(member).remove(right);
                    }
                }
            }
            ofy().save().entity(group).now();
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /**
     * Hilfsmethoden damit Code nicht doppelt vorkommt.
     */

    /**
     * Hilfsmethode um zu testen, ob ein EndUser die nötigen Rechte hat eine
     * Aktion durchzuführen.
     *
     * @param group         Nutzergruppe in der die Rechte überprüft werden sollen
     * @param email         Die E-Mail des EndUsers
     * @param requiredRight Das Recht, das benötigt wird
     * @return true, falls der EndUser die nötigen Rechte hat
     * false, falls nicht
     */
    private boolean hasRights(UserGroup group, @Named("email") String email, @Named("requiredRight") String requiredRight) {
        ArrayList<String> rights = (ArrayList<String>) group.getMemberRights().get(email);
        if (rights != null && (rights.contains(requiredRight) || rights.contains(Right.FULLRIGHTS.name()))) {
            return true;
        }
        return false;
    }

    /**
     * Hilfsmethode zum testen, ob ein übergebener String ein in der enum-
     * Klasse Right enthaltenes Recht ist.
     *
     * @param right Der String der gestestet werden soll
     * @return true, wenn der String ein Recht ist
     *          false, wenn der String kein Recht ist
     */
    private boolean isRight(String right) {
        if (Right.DISTRIBUTERIGHTS.name().equals(right) ||
                Right.POSTBLACKBOARD.name().equals(right) ||
                Right.INVITEGROUP.name().equals(right) ||
                Right.CHANGEGROUPPICTURE.name().equals(right) ||
                Right.COMMENTBOARDMESSAGE.name().equals(right) ||
                Right.DELETEGROUP.name().equals(right) ||
                Right.GLOBALMESSAGE.name().equals(right) ||
                Right.DELETEMEMBER.name().equals(right)) {
            return true;
        } else {
            return false;
        }
    }
}
