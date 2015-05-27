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
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
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
    public void distributeRightsToUser(User user, @Named("groupName") String groupName, ListWrapper rightNames, @Named("userName") String userName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            EndpointUtil.throwIfUserNotInGroup(group, userName);
            for (String right : rightNames.stringList) {
                if (!group.getMemberRights().get(userName).contains(right) && isRight(right)) {
                    group.getMemberRights().get(userName).add(right);
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
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            for (String userName : userNames.stringList) {
                EndpointUtil.throwIfUserNotInGroup(group, userName);
                if (!group.getMemberRights().get(userName).contains(rightName) && isRight(rightName)) {
                    group.getMemberRights().get(userName).add(rightName);
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
    public void distributeRightsToUsers(User user, @Named("groupName") String groupName, ListWrapper rightNames, @Named("userNames") List<String> userNames) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            String[] users = userNames.get(0).split(",");
            for (String member : users) {
                EndpointUtil.throwIfUserNotInGroup(group, member);
                for (String right : rightNames.stringList) {
                    if (!group.getMemberRights().get(member).contains(right) && isRight(right)) {
                        group.getMemberRights().get(member).add(right);
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
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
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
    public void takeRightsFromUser(User user, @Named("groupName") String groupName, ListWrapper rightNames, @Named("userName") String userName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            EndpointUtil.throwIfUserNotInGroup(group, userName);
            for (String right : rightNames.stringList) {
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
    public void takeRightFromUsers(User user, @Named("groupName") String groupName, @Named("rightName") String rightName, ListWrapper userNames) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            for (String member : userNames.stringList) {
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
    public void takeRightsFromUsers(User user, @Named("groupName") String groupName, ListWrapper rightNames, @Named("userNames") List<String> userNames) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
        if (hasRights(group, user.getEmail(), Right.DISTRIBUTERIGHTS.name())) {
            String[] users = userNames.get(0).split(",");
            for (String member : users) {
                EndpointUtil.throwIfUserNotInGroup(group, member);
                for (String right : rightNames.stringList) {
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

    /**
     * Ändert den Leader der Nutzergruppe, das heißt die Person mit dem Recht
     * FULLRIGHTS ändert sich. Der neue Leader newLeader bekommt das Recht
     * FULLRIGHTS und der alte Leader bekommt statt dessen das Recht
     * NEWMEMBERRIGHTS.
     *
     * @param user Der Aufrufer, der den Leader ändern möchte
     * @param groupName Der Name der Nutzergruppe
     * @param newLeader Der neue Leader
     * @throws OAuthRequestException
     */
    public void changeLeader(User user, @Named("groupName") String groupName, @Named("newLeader") String newLeader) throws OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        EndpointUtil.throwIfEndUserNotExists(newLeader);
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
        if(hasRights(group, user.getEmail(), Right.FULLRIGHTS.name())){
            EndpointUtil.throwIfUserNotInGroup(group, newLeader);
            group.getMemberRights().get(user.getEmail()).remove(Right.FULLRIGHTS.name());
            group.getMemberRights().get(user.getEmail()).add(Right.NEWMEMBERRIGHTS.name());
            group.getMemberRights().get(newLeader).remove(Right.NEWMEMBERRIGHTS.name());
            group.getMemberRights().get(newLeader).add(Right.FULLRIGHTS.name());
        }else{
            EndpointUtil.throwIfNoRights();
        }
        ofy().save().entity(group).now();
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
