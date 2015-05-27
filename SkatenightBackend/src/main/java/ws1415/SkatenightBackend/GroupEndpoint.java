package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.gcm.Message;
import ws1415.SkatenightBackend.gcm.MessageType;
import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.gcm.Sender;
import ws1415.SkatenightBackend.model.Board;
import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.BoardEntry;
import ws1415.SkatenightBackend.model.UserGroupType;
import ws1415.SkatenightBackend.transport.UserGroupBlackBoardTransport;
import ws1415.SkatenightBackend.transport.UserGroupMetaData;
import ws1415.SkatenightBackend.model.Right;
import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.transport.UserGroupNewsBoardTransport;
import ws1415.SkatenightBackend.model.UserGroupPicture;
import ws1415.SkatenightBackend.transport.UserGroupVisibleMembers;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Created by Richard on 01.05.2015.
 */
public class GroupEndpoint extends SkatenightServerEndpoint {
    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Erstellt eine Gruppe mit dem angegebenen Namen und der angegebenen
     * Öffentlichkeit.
     *
     * @param user      Der Benutzer, der die Gruppe anlegen möchte
     * @param privat    Die Öffentlichkeitseinstellung der Gruppe
     * @param groupName Der Name der neuen Gruppe.
     */
    public void createUserGroup(User user, @Named("groupName") String groupName, @Named("groupPrivacy") boolean privat, @Named("groupType") UserGroupType groupType, @Named("groupPassword") String password) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        throwIfUserGroupAlreadyExists(groupName);
        EndUser endUser = throwIfNoEndUserFound(user.getEmail());

        if(groupType.equals(UserGroupType.SECURITYGROUP) && privat == false){
            throw new IllegalArgumentException("a security user group cannot be open to everyone");
        }
        if(privat == true && (password.isEmpty() || password == null || password.length() != 60)){
            throw new IllegalArgumentException("password for private user groups has to be specified");
        }
        UserGroup ug = new UserGroup(endUser.getEmail(), groupType.name(), password);
        ug.setName(groupName);
        ug.setMemberRights(new HashMap<String, ArrayList<String>>());
        ug.addGroupMember(endUser.getEmail(), createFullRightsList());
        ug.setMemberCount(1);
        ug.setPrivat(privat);

        // Der Ersteller der Nutzergruppe ist zu Anfang auch Sichtbar auf der Karte
        UserGroupVisibleMembers visibleMembers = new UserGroupVisibleMembers();
        visibleMembers.setGroupName(groupName);
        visibleMembers.addVisibleMember(endUser.getEmail());
        ofy().save().entity(visibleMembers).now();
        ug.setVisibleMembers(visibleMembers);
        ofy().save().entity(ug).now();
        endUser.addUserGroup(ug);
        saveEndUserAndSendNotification(endUser);
    }

    public UserGroupVisibleMembers getUserGroupVisibleMembers(User user, @Named("groupName") String groupName)throws OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupVisibleMembers.class).type(UserGroup.class).id(groupName).safe();
        return new UserGroupVisibleMembers(groupName, group.getVisibleMembers().getList());
    }

    /**
     * Ruft das BlackBoard zu dem übergebenen Nutzergruppennamen ab und verschickt
     * dies mit einer Tansportklasse UserGroupBlackBoardTransport.
     *
     * @param user      Der Benutzer, der das BlackBoard abrufen möchte
     * @param groupName Der Name der Nutzergruppe
     * @return Die Tansportklasse, die die BoardEntries enthält
     * @throws OAuthRequestException
     */
    public UserGroupBlackBoardTransport getUserGroupBlackBoard(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupBlackBoardTransport.class).type(UserGroup.class).id(groupName).safe();
        return new UserGroupBlackBoardTransport(group.getName(), group.getBlackBoard().getBoardEntries());
    }

    /**
     * Ruft das BlackBoard zu dem übergebenen Nutzergruppennamen ab und verschickt
     * dies mit einer Tansportklasse UserGroupBlackBoardTransport.
     *
     * @param user      Der Benutzer, der das BlackBoard abrufen möchte
     * @param groupName Der Name der Nutzergruppe
     * @return Die Tansportklasse, die die BoardEntries enthält
     * @throws OAuthRequestException
     */
    public UserGroupNewsBoardTransport getUserGroupNewsBoard(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupNewsBoardTransport.class).type(UserGroup.class).id(groupName).safe();
        return new UserGroupNewsBoardTransport(group.getName(), group.getBlackBoard().getBoardEntries());
    }

    /**
     * Ruft die Gruppen Metadaten zu dem angegebenen Namen ab.
     *
     * @param groupName Der Name der Nutzergruppe, deren Metadaten agerufen werden sollen
     * @return UserGroupMetaData oder null falls keine Nutzergruppe gefunden wurde
     */
    public UserGroupMetaData getUserGroupMetaData(User user, @Named("groupName") String groupName) throws OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupMetaData.class).type(UserGroup.class).id(groupName).safe();
        return new UserGroupMetaData(group.getName(), group.getCreator(), group.isPrivat(), group.getMemberCount());
    }

    /**
     * Ruft die Nutzergruppe zu dem angegebenen Namen ab.
     *
     * @param groupName Der Name der Nutzergruppe
     * @return Die Nutzergruppe, oder null falls keine gefunden wurde
     */
    public UserGroup getUserGroup(User user, @Named("groupName") String groupName) throws OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        return ofy().load().type(UserGroup.class).id(groupName).safe();
    }

    /**
     * Löscht die Gruppe, falls der aufrufende Benutzer der Ersteller der Gruppe ist.
     *
     * @param user      Der Benutzer, der den Löschvorgang durchführen möchte.
     * @param groupName Der Name, der zu löschenden Gruppe.
     */
    public void deleteUserGroup(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup ug = ofy().load().group(UserGroupBlackBoardTransport.class, UserGroupNewsBoardTransport.class, UserGroupPicture.class, UserGroupVisibleMembers.class).type(UserGroup.class).id(groupName).safe();
        if (hasRights(ug, user.getEmail(), Right.FULLRIGHTS.name())) {
            if(ug.getBlackBoard() != null){
                if(ug.getBlackBoard().getBoardEntries() != null){
                    ofy().delete().entities(ug.getBlackBoard().getBoardEntries());
                }
                ofy().delete().entity(ug.getBlackBoard()).now();
            }
            if(ug.getNewsBoard() != null){
                if(ug.getNewsBoard().getBoardEntries() != null){
                    ofy().delete().entities(ug.getNewsBoard().getBoardEntries());
                }
                ofy().delete().entity(ug.getNewsBoard()).now();
            }
            if(ug.getPicture() != null){
                blobstoreService.delete(ug.getPicture().getPictureBlobKey());
                ofy().delete().entity(ug.getPicture()).now();
            }
            if(ug.getVisibleMembers() != null){
                ofy().delete().entity(ug.getVisibleMembers()).now();
            }
            // Die UserGroup aus den Benutzern entfernen und eine Notification
            // an die EndUser senden.
            removeGroupFromEndUsersAndSendNotification(ug);
            ofy().delete().entity(ug).now();
        }else{
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Gibt eine Liste aller auf dem Server gespeicherter Benutzergruppen zurück.
     *
     * @return Eine Liste aller Benutzergruppen.
     */
    public List<UserGroup> getAllUserGroups(User user) throws OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
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
        if (user == null || (endUser = throwIfNoEndUserFound(user.getEmail())) == null) {
            // Falls kein Benutzer angegeben, dann leere Liste zurückgeben.
            return new ArrayList<>();
        }
        // Falls im EndUser noch UserGroups existieren die
        // nicht mehr da sein sollten, diese löschen
        return removeMissingUserGroups(endUser);
    }

    /**
     * Fügt den aufrufenden Benutzer zu der angegebenen Gruppe hinzu.
     *
     * @param user      Der aufrufende Benutzer.
     * @param groupName Der Name der beizutretenden Gruppe
     * @param groupPassword Das Password der Nutzergruppe, falls diese privat ist
     */
    public void joinUserGroup(User user, @Named("groupName") String groupName, @Named("groupPassword") String groupPassword) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        if(group.isPrivat() && !group.getPassword().equals(groupPassword)){
            throw new IllegalArgumentException("if you want to join this user group you need the correct password");
        }
        EndpointUtil.throwIfUserAlreadyInGroup(group, user.getEmail());
        EndUser endUser = throwIfNoEndUserFound(user.getEmail());

        group.addGroupMember(endUser.getEmail(), createNewMemberRightsList());
        group.getVisibleMembers().addVisibleMember(endUser.getEmail());
        ofy().save().entity(group.getVisibleMembers()).now();
        ofy().save().entity(group).now();

        // Dem EndUser die Nutzergruppe hinzufügen
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            endUser.addUserGroup(group);
            pm.makePersistent(endUser);
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
    public void leaveUserGroup(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        EndpointUtil.throwIfLeaderTriesToLeaveGroup(group, user.getEmail());

        group.removeGroupMember(user.getEmail());
        group.getVisibleMembers().removeVisibleMember(user.getEmail());
        ofy().save().entity(group.getVisibleMembers()).now();
        ofy().save().entity(group).now();

        removeGroupFromEndUser(group, user.getEmail());
    }

    /**
     * Löscht den EndUser aus der übergebenen Nutzergruppe.
     *
     * @param groupName die UserGroup aus der gelöscht werden soll
     * @param user      der EndUser, der gelöscht werden soll
     * @return BooleanWrapper, eigene Klasse um boolean Werte zurück zu geben
     */
    public void removeMember(User user, @Named("groupName") String groupName, @Named("userName") String userName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.DELETEMEMBER.name()) && group.getMemberRights().keySet().contains(userName)) {

            group.removeGroupMember(userName);
            ofy().save().entity(group).now();

            removeGroupFromEndUser(group, userName);
        }else{
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Methode zum posten von BlackBoard Nachrichten in einer UserGroup. Momentan wird die Nachricht und
     * der Schreiber in das BoardEntry eingetragen und die Liste in der UserGroup aktualisiert.
     *
     * @param groupName UserGroup, dessen BlackBoard eine neue Nachricht erhalten soll
     * @param message   Die Nachricht
     */
    public void postBlackBoard(User user, @Named("groupName") String groupName, @Named("boardMessage") String message) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        EndpointUtil.throwIfNotBoardMessageSubmitted(message);
        UserGroup group = ofy().load().group(UserGroupBlackBoardTransport.class).type(UserGroup.class).id(groupName).safe();
        if(hasRights(group, user.getEmail(), Right.POSTBLACKBOARD.name())){
            BoardEntry be = new BoardEntry(message, user.getEmail());
            ofy().save().entity(be).now();
            if(group.getBlackBoard() == null){
                Board board = new Board(groupName, be);
                ofy().save().entity(board).now();
                group.setBlackBoard(board);
            }else{
                group.getBlackBoard().addBoardMessage(be);
                ofy().save().entity(group.getBlackBoard()).now();
            }
            ofy().save().entity(group).now();
        }else{
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Methode um BlackBoard Nachrichten zu löschen. Dabei wird der BoardEntry übergeben und in
     * der Liste von BoardEntries dieser gesucht und dann gelöscht.
     *
     * @param boardEntryId der zu löschende BoardEntry
     * @param groupName    die UserGroup in der ein BoardEntry vom BlackBoard gelöscht werden soll
     */
    public void deleteBlackBoardMessage(User user, @Named("boardEntryId") Long boardEntryId, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        EndpointUtil.throwIfNoBoardEntryIdISubmitted(boardEntryId);
        UserGroup group = ofy().load().group(UserGroupBlackBoardTransport.class).type(UserGroup.class).id(groupName).safe();
        if(hasRights(group, user.getEmail(), Right.POSTBLACKBOARD.name())) {
            ofy().delete().type(BoardEntry.class).id(boardEntryId);
            group.getBlackBoard().removeBoardMessage(boardEntryId);
            ofy().save().entity(group.getBlackBoard()).now();
            ofy().save().entity(group).now();
        }else{
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Methode zum posten von NewsBoard Nachrichten in einer UserGroup. Momentan wird die Nachricht und
     * der Schreiber in das BoardEntry eingetragen und die Liste in der UserGroup aktualisiert.
     *
     * @param groupName UserGroup, dessen BlackBoard eine neue Nachricht erhalten soll
     * @param message   Die Nachricht
     */
    public void postNewsBoard(User user, @Named("groupName") String groupName, @Named("boardMessage") String message) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        EndpointUtil.throwIfNotBoardMessageSubmitted(message);
        UserGroup group = ofy().load().group(UserGroupBlackBoardTransport.class).type(UserGroup.class).id(groupName).safe();

        if(hasRights(group, user.getEmail(), Right.FULLRIGHTS.name())){
            BoardEntry be = new BoardEntry(message, groupName);
            ofy().save().entity(be).now();
            if(group.getNewsBoard() == null){
                Board board = new Board(groupName, be);
                ofy().save().entity(board).now();
                group.setNewsBoard(board);
            }else{
                group.getNewsBoard().addBoardMessage(be);
                ofy().save().entity(group.getNewsBoard()).now();
            }
            ofy().save().entity(group).now();
        }else{
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Methode um BlackBoard Nachrichten zu löschen. Dabei wird der BoardEntry übergeben und in
     * der Liste von BoardEntries dieser gesucht und dann gelöscht.
     *
     * @param boardEntryId der zu löschende BoardEntry
     * @param groupName    die UserGroup in der ein BoardEntry vom BlackBoard gelöscht werden soll
     */
    public void deleteNewsBoardMessage(User user, @Named("boardEntryId") Long boardEntryId, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        EndpointUtil.throwIfNoBoardEntryIdISubmitted(boardEntryId);
        UserGroup group = ofy().load().group(UserGroupBlackBoardTransport.class).type(UserGroup.class).id(groupName).safe();

        if(hasRights(group, user.getEmail(), Right.FULLRIGHTS.name())){
            group.getNewsBoard().removeBoardMessage(boardEntryId);
            ofy().delete().type(BoardEntry.class).id(boardEntryId);
            ofy().save().entity(group).now();
        }else{
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Methode zum ändern des Bildes einer Nutzergruppe
     * //TODO Kommentieren
     * @param groupName Die UserGroup in der das Bild geändert werden soll.
     * @return Das Ergebnis des Änderns
     * true = das Bild wurde erfolgreich geändert
     * false = fas Bild wurde nicht geändert
     */
    public UserGroupPicture changePicture(User user, @Named("groupName") String groupName) throws OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupPicture.class).type(UserGroup.class).id(groupName).safe();
        if(group.getPicture() != null){
            blobstoreService.delete(group.getPicture().getPictureBlobKey());
            ofy().delete().entity(group.getPicture()).now();
        }

        UserGroupPicture picture = new UserGroupPicture();
        picture.setPictureUrl(blobstoreService.createUploadUrl("/Bernd/images/upload"));
        ofy().save().entity(picture).now();
        group.setPicture(picture);
        ofy().save().entity(group).now();
        return picture;
    }

    /**
     * Diese Methode gibt das Bild einer Nutzergruppe zurück, oder null wenn keins existiert.
     *
     * @param user
     * @param groupName Der Name der Nutzergruppe
     * @return
     * @throws OAuthRequestException
     */
    public UserGroupPicture getUserGroupPicture(User user, @Named("groupName") String groupName)throws OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        return ofy().load().group(UserGroupPicture.class).type(UserGroup.class).id(groupName).safe().getPicture();
    }

    /**
     * Diese Methode ändert für den Aufrufer die Sichtbarkeit in der übergebenen Nutzergruppe
     * auf der Streckenkarte während eines aktiven Events. Dazu muss der Aufrufer ein
     * Mitglied der Nutzergruppe sein.
     *
     * @param user
     * @param groupName Der Name Der Nutzergruppe
     * @throws OAuthRequestException
     */
    public void changeMyVisibility(User user, @Named("groupName") String groupName) throws OAuthRequestException{
        //TODO Jeder setzt seine sichtbaren Nutzergruppen lokal und seine Sichtbarkeit gegenüber der Gruppe global
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupVisibleMembers.class).type(UserGroup.class).id(groupName).safe();
        if(group.getGroupType().equals(UserGroupType.SECURITYGROUP)){
            throw new IllegalArgumentException("You canno't change your visibility in a security user group");
        }
        if(!group.getVisibleMembers().getList().contains(user.getEmail()) && group.getMemberRights().containsKey(user.getEmail())){
            group.getVisibleMembers().addVisibleMember(user.getEmail());
        }else{
            group.getVisibleMembers().removeVisibleMember(user.getEmail());
        }
        ofy().save().entity(group.getVisibleMembers()).now();
    }

    public void changeUserGroupPassword(User user, @Named("groupName") String groupName, @Named("currentPassword") String currentPw, @Named("newPassword") String newPw)throws OAuthRequestException{
        //TODO Password in der app mit einem Service in einen Hash umwandeln
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        if(newPw == null || newPw.isEmpty() || newPw.length() != 60){
            throw new IllegalArgumentException("the submitted new password does not met the requrements");
        }
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
        if(hasRights(group, user.getEmail(), Right.FULLRIGHTS.name())){
            if(group.getPassword().equals(currentPw)){
                group.setPassword(newPw);
                ofy().save().entity(group).now();
            }else{
                throw new IllegalArgumentException("your submitted current password does not met the user groups current password");
            }
        }else{
            EndpointUtil.throwIfNoRights();
        }
    }

    public void changeUserGroupPrivacy(User user, @Named("groupName") String groupName, @Named("groupPrivacy") boolean isOpen) throws OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        group.setPrivat(isOpen);
    }
//======================================================================================

    /**
     * Diese Methode sendet eine Einladung(Notification) zu der übergebene UserGroup and den
     * übergebenen EndUser mit der Möglichkeit diese anzunehmen oder abzulehnen.
     *
     * @param groupName   die UserGroup in die der EndUser eingeladen werden soll.
     * @param userToInvite    der EndUser, der die Einladung(Notification) erhalten soll
     * @param message ein Text beigefügt zu der Einladung.
     */
    public void sendInvitation(User user, @Named("groupName") String groupName, @Named("userToInvite") String userToInvite, @Named("invitationMessage") String message) throws  OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        EndpointUtil.throwIfEndUserNotExists(userToInvite);
        EndpointUtil.throwIfNotBoardMessageSubmitted(message);
        throwIfNoUserGroupExists(groupName);
        // send a message to the endUser asking if he wants to join the group
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

        try {
            RegistrationManager rm = getRegistrationManager(pm);
            Message m = new Message.Builder()
                    .collapseKey("send invitation")
                    .timeToLive(6000)
                    .delayWhileIdle(false)
                    .addData("type", MessageType.INVITATION_TO_GROUP_MESSAGE.name())
                    .addData("content", message)
                    .addData("title", "Sie haben eine Einladung erhalten von "+user.getEmail()+ " in die Nutzergruppe "+groupName)
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
    public void sendGlobalMessage(User user, UserGroup group, @Named("globalMessage") String message) {
        //TODO Diese Methode kann so noch nicht funktionieren.

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
        return new BooleanWrapper(true);
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /**
     * Hilfsmethoden damit Code nicht doppelt vorkommt.
     */

    /**
     * Exception wird geworfen, falls kein EndUser für die gegebene E-Mail existiert.
     *
     * @param email E-Mail Adresse des zu prüfenden EndUsers
     */
    private EndUser throwIfNoEndUserFound(@Named("userMail") String email) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        EndUser user;
        try {
            user = pm.getObjectById(EndUser.class, email);
        } catch (Exception e) {
            user = null;
        } finally {
            pm.close();
        }
        if (user == null) {
            throw new JDOObjectNotFoundException("EndUser with submitted E-Mail doesn't exsit");
        }
        return user;
    }

    /**
     * Hilfsmethode zum speichern von EndUser objekten.
     *
     * @param endUser
     */
    private void saveEndUser(EndUser endUser) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            pm.makePersistent(endUser);
        } finally {
            pm.close();
        }
    }

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
     * Hilfsmethode, welche das EndUser- Objekt speichert und eine Notification
     * and das Smartphone sendet.
     *
     * @param endUser
     */
    private void saveEndUserAndSendNotification(EndUser endUser) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            pm.makePersistent(endUser);
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

    private void removeGroupFromEndUsersAndSendNotification(UserGroup group) {
        // Benutzer abrufen, die in der Gruppe sind und Löschen der Nutzergruppe
        EndUser[] members = new EndUser[group.getMemberCount()];
        int index = 0;
        for (String member : group.getMemberRights().keySet()) {
            members[index++] = throwIfNoEndUserFound(member);
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Set<String> regids = new HashSet<>();
            RegistrationManager rm = getRegistrationManager(pm);
            for (EndUser enduser : members) {
                enduser.removeUserGroup(group);
                pm.makePersistent(enduser);
                regids.add(rm.getUserIdByMail(enduser.getEmail()));
            }

            // Notification senden
            Sender sender = new Sender(Constants.GCM_API_KEY);
            Message.Builder mb = new Message.Builder()
                    // Nachricht erst anzeigen, wenn der Benutzer sein Handy benutzt
                    .delayWhileIdle(false)
                    .collapseKey("group_" + group.getName() + "_deleted")
                            // Nachricht verfallen lassen, wenn Benutzer erst nach Event online geht
                    .addData("type", MessageType.GROUP_DELETED_NOTIFICATION_MESSAGE.name())
                    .addData("content", group.getName())
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

    private void removeGroupFromEndUser(UserGroup group, @Named("userMail") String userMail) {
        EndUser user = throwIfNoEndUserFound(userMail);
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            user.removeUserGroup(group);
            pm.makePersistent(user);
        } finally {
            pm.close();
        }
    }

    /**
     * Hilfsmethode welche eine Liste mit dem Recht FULLRIGHTS erstellt.
     *
     * @return
     */
    private ArrayList<String> createFullRightsList() {
        ArrayList<String> tmpList = new ArrayList<>();
        tmpList.add(Right.FULLRIGHTS.name());
        return tmpList;
    }

    /**
     * Hilfsmethode, welche eine Liste mit dem Recht NEWMEMBERRIGHTS erstellt.
     *
     * @return
     */
    private ArrayList<String> createNewMemberRightsList() {
        ArrayList<String> tmpList = new ArrayList<>();
        tmpList.add(Right.NEWMEMBERRIGHTS.name());
        return tmpList;
    }

    /**
     * Hilfsmethode zum löschen aller Nutzergruppen in einem EndUser welche
     * nicht mehr existieren. Als Ergebnis werden alle Nutzergruppen zurück
     * gegeben in denen der EndUser Mitglied ist und die noch auf dem Server
     * existieren.
     *
     * @param endUser Der EndUser, dessen Nutzergruppen überprüft werden sollen
     * @return Liste mit allen Nutzergruppen in denen der EndUser Mitglied ist.
     */
    private List<UserGroup> removeMissingUserGroups(EndUser endUser) {
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
     * Ruft die Nutzergruppe zu dem angegebenen Namen ab. Hilfsmethode
     *
     * @param groupName Der Name der Nutzergruppe
     * @return Die Nutzergruppe, oder null falls keine gefunden wurde
     */
    private UserGroup getUserGroup(@Named("groupName") String groupName) {
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        return ofy().load().type(UserGroup.class).id(groupName).now();
    }

    /**
     * Exception wird geworfen, falls kein gültiges UserGroup-Objekt übergeben wird. Oder
     * keine UserGroup zu dem angegebenen Namen existiert.
     *
     * @param groupName
     * @throws OAuthRequestException
     */
    private static UserGroup throwIfNoUserGroupExists(String groupName) throws OAuthRequestException {
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
    private static void throwIfUserGroupAlreadyExists(String groupName){
        if(new GroupEndpoint().getUserGroup(groupName) != null){
            throw new IllegalArgumentException("user group with submitted name "+groupName+ " already exists");
        }
    }
}
