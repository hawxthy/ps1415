package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.gcm.Message;
import ws1415.SkatenightBackend.gcm.MessageType;
import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.gcm.Sender;
import ws1415.SkatenightBackend.model.BoardEntry;
import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.GroupMetaData;
import ws1415.SkatenightBackend.model.Member;
import ws1415.SkatenightBackend.model.Picture;
import ws1415.SkatenightBackend.model.Right;
import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.transport.UserProfile;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Created by Richard on 01.05.2015.
 */
public class GroupEndpoint extends SkatenightServerEndpoint {

    /**
     * Erstellt eine Gruppe mit dem angegebenen Namen
     *
     * @param user      Der Benutzer, der die Gruppe anlegen möchte.
     * @param groupName Der Name der neuen Gruppe.
     */
    public void createUserGroup(User user, @Named("name") String groupName) throws OAuthRequestException {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        UserProfile userProfile = new UserEndpoint().getUserProfile(user, user.getEmail());
        if (userProfile == null) {
            throw new IllegalArgumentException("user is not registered");
        }
        if (getUserGroup(groupName) != null) {
            throw new IllegalArgumentException("group with submitted name already exists");
        }


        ArrayList<String> rights = new ArrayList<String>();
        rights.add(Right.FULLRIGHTS.name());


        // Die Daten der UserGroup setzen und diese dann abspeichern
        UserGroup ug = new UserGroup(userProfile.getEmail());
        ug.addGroupMember(userProfile.getEmail(), rights);
        ug.setName(groupName);

        // Die Metadaten für die UserGroup erstellen
        GroupMetaData metaData = new GroupMetaData();
        metaData.setCreator(ug.getCreator());
        metaData.setName(ug.getName());
        metaData.setMembers(new HashSet<String>());
        metaData.getMembers().add(ug.getCreator());

        ug.setMetaData(metaData);
        ofy().save().entity(ug).now();
        ofy().save().entity(metaData).now();

        // Die Daten beim EndUser setzen und diesen dann abspeichern
        // und gleichzeitig eine Notification senden
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            //userProfile.addUserGroup(ug);
            pm.makePersistent(userProfile);
            RegistrationManager rm = getRegistrationManager(pm);
            Message m = new Message.Builder()
                    .collapseKey("createUserGroup")
                    .timeToLive(6000)
                    .delayWhileIdle(false)
                    .addData("type", MessageType.GROUP_CREATED_NOTIFICATION_MESSAGE.name())
                    .build();
            Sender s = new Sender(Constants.GCM_API_KEY);
            s.send(m, rm.getRegisteredUser(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pm.close();
        }
    }

    /**
     * Ruft die Gruppe mit dem angegebenen Namen ab.
     *
     * @param groupName Der Name der abzurufenden Gruppe.
     * @return Die UserGroup-Entity.
     */
    public UserGroup getUserGroup(@Named("groupName") String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("can't get the usergroup, no group name submitted");
        }
        return ofy().load().type(UserGroup.class).id(groupName).now();
    }

    /**
     * Löscht die Gruppe, falls der aufrufende Benutzer der Ersteller der Gruppe ist.
     *
     * @param user Der Benutzer, der den Löschvorgang durchführen möchte.
     * @param name Der Name, der zu löschenden Gruppe.
     */
    public void deleteUserGroup(User user, @Named("name") String name) throws OAuthRequestException {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        UserEndpoint userEndpoint = new UserEndpoint();
        UserProfile userProfile = userEndpoint.getUserProfile(user, user.getEmail());
        if (userProfile == null) {
            throw new IllegalArgumentException("user is not registered");
        }
        UserGroup ug = getUserGroup(name);
        if (ug != null) {
            if (!ug.getCreator().equals(user.getEmail())) {
                throw new IllegalArgumentException("user is not creator of group");
            }

            // Benutzer abrufen, die in der Gruppe sind und Löschen der Nutzergruppe
            UserProfile[] members = new UserProfile[ug.getMembers().size()];
            int index = 0;
            for (String member : ug.getMembers()) {
                members[index++] = userEndpoint.getUserProfile(user, member);
            }
            ofy().delete().type(GroupMetaData.class).id(name);
            ofy().delete().type(UserGroup.class).id(name);
            if(ug.getBlackBoard() != null){
                ofy().delete().entities(ug.getBlackBoard()).now();
            }

            // Die UserGroup aus den Benutzern entfernen und eine Notification
            // an die EndUser senden.
            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            try {
                Set<String> regids = new HashSet<>();
                RegistrationManager rm = getRegistrationManager(pm);
                for (UserProfile e : members) {
                    //e.removeUserGroup(ug);
                    pm.makePersistent(e);
                    regids.add(rm.getUserIdByMail(e.getEmail()));
                }

                // Notification senden
                Sender sender = new Sender(Constants.GCM_API_KEY);
                Message.Builder mb = new Message.Builder()
                        // Nachricht erst anzeigen, wenn der Benutzer sein Handy benutzt
                        .delayWhileIdle(false)
                        .collapseKey("group_" + ug.getName() + "_deleted")
                                // Nachricht verfallen lassen, wenn Benutzer erst nach Event online geht
                        .addData("type", MessageType.GROUP_DELETED_NOTIFICATION_MESSAGE.name())
                        .addData("content", ug.getName())
                        .addData("title", "Eine Gruppe wurde geloescht");
                Message m = mb.build();
                try {
                    sender.send(m, new LinkedList<>(regids), 1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } finally {
                pm.close();
            }
        }
    }

    /**
     * Gibt eine Liste aller auf dem Server gespeicherter Benutzergruppen zurück.
     *
     * @return Eine Liste aller Benutzergruppen.
     */
    public List<UserGroup> getAllUserGroups() {
        Query<UserGroup> query = ofy().load().type(UserGroup.class);
        return query.list();
    }

    /**
     * Gibt eine Liste aller Benutzergruppen des angegebenen Benutzers zurück.
     *
     * @return Eine Liste aller Benutzergruppen.
     */
    public List<UserGroup> fetchMyUserGroups(User user) throws OAuthRequestException {
        EndUser endUser;
        if (user == null || (endUser = new UserEndpoint().getFullUser(user.getEmail())) == null) {
            // Falls kein Benutzer angegeben, dann leere Liste zurückgeben.
            return new ArrayList<>();
        }

        // Falls im EndUser noch UserGroups existieren die
        // nicht mehr da sein sollten, diese löschen
        UserGroup ug;
        List<UserGroup> result = new LinkedList<>();
        List<String> missingGroups = new LinkedList<>();
        for (String g : endUser.getMyUserGroups()) {
            ug = getUserGroup(g);
            if (ug != null) {
                result.add(ug);
            } else {
                missingGroups.add(g);
            }
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            if (missingGroups.size() > 0) {
                for (String g : missingGroups) {
                    endUser.getMyUserGroups().remove(g);
                }
                pm.makePersistent(endUser);
            }
            return result;
        } finally {
            pm.close();
        }
    }

    /**
     * Fügt den aufrufenden Benutzer zu der angegebenen Gruppe hinzu.
     *
     * @param user      Der aufrufende Benutzer.
     * @param groupName Der Name der beizutretenden Gruppe
     */
    public void joinUserGroup(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        EndUser endUser = new UserEndpoint().getFullUser(user.getEmail());
        if (endUser == null) {
            throw new IllegalArgumentException("user is not registered");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserGroup ug = getUserGroup(groupName);
            if (ug == null) {
                throw new IllegalArgumentException("a group with the submitted group name does not exist");
            }
            endUser.addUserGroup(ug);
            pm.makePersistent(endUser);
            ArrayList<String> rights = new ArrayList<>();
            rights.add(Right.NEWMEMBERRIGHTS.name());
            ug.addGroupMember(endUser.getEmail(), rights);
            ofy().save().entity(ug).now();
        } finally {
            pm.close();
        }
    }

    /**
     * Entfernt den aufrufenden Benutzer aus der angegebenen Gruppe.
     *
     * @param user      Der aufrufende Benutzer.
     * @param groupName Der Name der zu verlassenden Gruppe.
     */
    public void leaveUserGroup(User user, @Named("groupName") String groupName) {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        EndUser endUser = new UserEndpoint().getFullUser(user.getEmail());
        if (endUser == null) {
            throw new IllegalArgumentException("user is not registered");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserGroup ug = getUserGroup(groupName);
            if (ug == null) {
                throw new IllegalArgumentException("a group with the submitted group name does not exist");
            }
            if (user.getEmail().equals(ug.getCreator())) {
                throw new IllegalArgumentException("you can not leave your own group");
            }
            endUser.removeUserGroup(ug);
            ug.removeGroupMember(endUser.getEmail());
            pm.makePersistent(endUser);
            ofy().save().entity(ug).now();
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt eine Liste der Mitglieder der übergebenen Gruppe zurück.
     *
     * @param userGroup
     * @return
     */
    public ArrayList<Member> fetchGroupMembers(@Named("userGroup") String userGroup) {
        ArrayList<Member> members = new ArrayList<>();
        UserGroup tmpGroup = getUserGroup(userGroup);
        UserEndpoint userEndpoint = new UserEndpoint();
        for (String member : tmpGroup.getMembers()) {
            members.add(userEndpoint.getMember(member));
        }
        return members;
    }

    /**
     * Löscht den EndUser aus der übergebenen UserGroup.
     *
     * @param groupName die UserGroup aus der gelöscht werden soll
     * @param user  der EndUser, der gelöscht werden soll
     * @return BooleanWrapper, eigene Klasse um boolean Werte zurück zu geben
     */
    public void removeMember(User user, @Named("groupName") String groupName, @Named("userName") String userName) {
        // TODO User prüfen
        if (groupName == null) {
            throw new NullPointerException("no group submitted");
        }
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("no user to remove submitted");
        }
        EndUser endUser = new UserEndpoint().getFullUser(user.getEmail());
        if (endUser == null) {
            throw new IllegalArgumentException("user is not registered");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserGroup userGroup = getUserGroup(groupName);
            if (userGroup == null) {
                throw new IllegalArgumentException("a usergroup with this name doesn't exits");
            }
            endUser.removeUserGroup(userGroup);
            pm.makePersistent(endUser);
            userGroup.removeGroupMember(endUser.getEmail());
            ofy().save().entity(userGroup).now();
        } finally {
            pm.close();
        }
    }

    /**
     * Methode zum posten von BlackBoard Nachrichten in einer UserGroup. Momentan wird die Nachricht und
     * der Schreiber in das BoardEntry eingetragen und die Liste in der UserGroup aktualisiert.
     *
     * @param groupName   UserGroup, dessen BlackBoard eine neue Nachricht erhalten soll
     * @param message Die Nachricht
     * @param writer  Der Ersteller der Nachricht
     */
    public void postBlackBoard(User user, @Named("groupName") String groupName, @Named("boardMessage") String message, @Named("writer") String writer) throws  OAuthRequestException{
        if(user == null){
            throw new OAuthRequestException("no user submitted");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new NullPointerException("no groupName submitted");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("no message submitted");
        }
        UserGroup userGroup = getUserGroup(groupName);
        if (userGroup == null) {
            throw new IllegalArgumentException("this group doesn't exist");
        }

        BoardEntry be = new BoardEntry(null, message, writer);
        ofy().save().entity(be).now();
        if(userGroup.getBlackBoard() == null){
            ArrayList<BoardEntry> blackBoard = new ArrayList<>();
            blackBoard.add(be);
            userGroup.setBlackBoard(blackBoard);
        }else{
            userGroup.addBlackBoardMessage(be);
        }
        ofy().save().entity(userGroup).now();
    }

    /**
     * Methode um BlackBoard Nachrichten zu löschen. Dabei wird der BoardEntry übergeben und in
     * der Liste von BoardEntries dieser gesucht und dann gelöscht.
     *
     * @param boardEntry der zu löschende BoardEntry
     * @param groupName  die UserGroup in der ein BoardEntry vom BlackBoard gelöscht werden soll
     */
    public void deleteBoardMessage(BoardEntry boardEntry, @Named("groupName") String groupName) {
        if (boardEntry == null) {
            throw new NullPointerException("no entry to delete submitted");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        UserGroup userGroup = getUserGroup(groupName);
        if (userGroup == null) {
            throw new IllegalArgumentException("this group doesn't exist");
        }
        if(userGroup.getBlackBoard() != null){
            for(BoardEntry be : userGroup.getBlackBoard()){
                if(be.getId() == boardEntry.getId()){
                    ArrayList<BoardEntry> blackBoard = userGroup.getBlackBoard();
                    blackBoard.remove(be);
                    userGroup.setBlackBoard(blackBoard);
                    ofy().delete().entity(be).now();
                    ofy().save().entity(userGroup).now();
                }
            }
        }
    }

//======================================================================================

    public void setVisibility(@Named("groupName") String groupName, BooleanWrapper visibility) {
        //TODO Lösung für den PrefManager finden.
    }

    /**
     * Diese Methode sendet eine Einladung(Notification) zu der übergebene UserGroup and den
     * übergebenen EndUser mit der Möglichkeit diese anzunehmen oder abzulehnen.
     *
     * @param group   die UserGroup in die der EndUser eingeladen werden soll.
     * @param user    der EndUser, der die Einladung(Notification) erhalten soll
     * @param message ein Text beigefügt zu der Einladung.
     */
    public void sendInvitation(UserGroup group, @Named("userName") String user, @Named("invitationMessage") String message) {
        //TODO Diese Methode kann so noch nicht funktionieren.
        if (group == null) {
            throw new NullPointerException("no group submitted");
        }
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("no user to send message to");
        }
        EndUser endUser = new UserEndpoint().getFullUser(user);
        if (endUser == null) {
            throw new IllegalArgumentException("user is not registered");
        }
        if (getUserGroup(group.getName()) == null) {
            throw new IllegalArgumentException("group doesn't exist");
        }
        // send a message to the endUser asking if he want's to join the group
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            RegistrationManager rm = getRegistrationManager(pm);
            Message m = new Message.Builder()
                    .collapseKey("send invitation")
                    .timeToLive(6000)
                    .delayWhileIdle(false)
                    .addData("type", MessageType.INVITATION_TO_GROUP_MESSAGE.name())
                    .build();
            Sender s = new Sender(Constants.GCM_API_KEY);
            s.send(m, rm.getRegisteredUser(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pm.close();
        }

    }

    /**
     * Sendet eine Nachricht an alle Mitglieder der übergebenen UserGroup. Dabei wird
     * der MessageController benutzt und einfache Nachrichten an alle Mitglieder gesendet.
     *
     * @param group   die UserGroup deren Mitglieder eine Nachricht erhalten sollen
     * @param message die Nachricht die gesendet werden soll
     */
    public void sendGlobalMessage(UserGroup group, @Named("globalMessage") String message) {
        //TODO Diese Methode kann so noch nicht funktionieren.
        if (group == null) {
            throw new NullPointerException("no group submitted");
        }
        if (getUserGroup(group.getName()) == null) {
            throw new IllegalArgumentException("group doesn't exist");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("no message submitted");
        }

        // send a message to the endUser asking if he want's to join the group
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            RegistrationManager rm = getRegistrationManager(pm);
            Message m = new Message.Builder()
                    .collapseKey("send global message")
                    .timeToLive(6000)
                    .delayWhileIdle(false)
                    .addData("type", MessageType.GLOBAL_GROUP_MESSAGE.name())
                    .build();
            Sender s = new Sender(Constants.GCM_API_KEY);
            s.send(m, rm.getRegisteredUser(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pm.close();
        }
    }

    /**
     * Nuzlos
     *
     * @param message
     * @param user
     * @return
     */
    public BooleanWrapper sendMessage(@Named("normalMessage") String message, @Named("userName") String user) {
        //TODO auf Martin warten
        new MessageEndoint().sendMessage(user, message);
        return new BooleanWrapper(true);
    }

    /**
     * Methode zum ändern des Bildes einer Nutzergruppe
     *
     * @param p         Da Bild, welches geändert werden soll.
     * @param groupName Die UserGroup in der das Bild geändert werden soll.
     * @return Das Ergebnis des Änderns
     * true = das Bild wurde erfolgreich geändert
     * false = fas Bild wurde nicht geändert
     */
    public BooleanWrapper changePicture(Picture p, @Named("groupName") String groupName) {
        if (p == null) {
            throw new NullPointerException("no picture submitted");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserGroup userGroup = getUserGroup(groupName);
            if (userGroup == null) {
                throw new IllegalArgumentException("user group doesn't exist");
            }
            userGroup.setPicture(p);
            pm.makePersistent(userGroup);
            return new BooleanWrapper(true);
        } finally {
            pm.close();
        }
    }
}
