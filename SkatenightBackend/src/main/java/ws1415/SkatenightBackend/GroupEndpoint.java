package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.ArrayList;
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
import ws1415.SkatenightBackend.model.BoardEntry;
import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.Right;
import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.model.UserGroupPreviewPictures;
import ws1415.SkatenightBackend.model.UserGroupType;
import ws1415.SkatenightBackend.transport.ListWrapper;
import ws1415.SkatenightBackend.transport.StringWrapper;
import ws1415.SkatenightBackend.transport.UserGroupBlackBoardTransport;
import ws1415.SkatenightBackend.transport.UserGroupFilter;
import ws1415.SkatenightBackend.transport.UserGroupMetaData;
import ws1415.SkatenightBackend.transport.UserGroupMetaDataList;
import ws1415.SkatenightBackend.transport.UserGroupNewsBoardTransport;
import ws1415.SkatenightBackend.transport.UserGroupVisibleMembers;
import ws1415.SkatenightBackend.transport.UserLocationInfo;
import ws1415.SkatenightBackend.transport.UserPrimaryData;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Stellt Methoden zur Verarbeitung von Nutzergruppen auf dem Backend bereit.
 *
 * @author Bernd Eissing
 */
public class GroupEndpoint extends SkatenightServerEndpoint {
    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Prüft ob der angegebene Name schon vergeben ist.
     *
     * @param user
     * @param groupName Der Name der Gruppe, der geprüft werden soll
     * @return
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public BooleanWrapper checkGroupName(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        if (ofy().load().type(UserGroup.class).id(groupName).now() != null) {
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(true);
    }

    /**
     * Erstellt eine private Nutzergruppe vom angegebenen Typ mit dem angegebenen
     * Namen. Dabei muss ein Passwort angegeben sein, denn privaten Nutzergruppen
     * kann nur mit Angabe eines Passwortes beigetreten werden.
     *
     * @param user      Der Benutzer, der die Gruppe anlegen möchte
     * @param groupName Der Name der Nutzergrupppe
     * @param groupType Der Typ der Nutzergruppe entweder NORMALGROUP oder SECURITYGROUP
     * @param password  Das Passwort, kann nicht null oder leer sein
     * @param blobKeyValue   Das Bild
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    @ApiMethod(httpMethod = "POST")
    public void createUserGroup(User user,
                                @Named("groupName") String groupName,
                                @Named("groupType") UserGroupType groupType,
                                @Nullable @Named("groupPassword") String password,
                                @Named("groupPrivacy") boolean groupPrivacyValue,
                                @Nullable @Named("groupDescription") String groupDescription,
                                @Nullable @Named("blobKeyValue") String blobKeyValue) throws OAuthRequestException {
        // Bedingungen prüfen
        EndpointUtil.throwIfNoUser(user);
        throwIfUserGroupAlreadyExists(groupName);
        EndpointUtil.throwIfEndUserNotExists(user.getEmail());
        if (groupPrivacyValue && (password.isEmpty() || password == null)) {
            throw new IllegalArgumentException("password for private user groups has to be submitted");
        }

        // Den BlobKey aus den PreviewPictures löschen, falls einer vorhanden ist.
        BlobKey blobKey = null;
        if (blobKeyValue != null && !blobKeyValue.isEmpty()) {
            blobKey = new BlobKey(blobKeyValue);
            UserGroupPreviewPictures preview = getUserGroupPreviewPictures();
            preview.removeBlobKeyValue(blobKey);
            ofy().save().entity(preview);
        }


        UserGroup group = new UserGroup(groupName, user.getEmail(), groupType, groupPrivacyValue, hashPassword(password), groupDescription, blobKey);
        group.addGroupMember(user.getEmail(), createFullRightsList());

        // Der Ersteller der Nutzergruppe ist zu Anfang auch sichtbar auf der Karte
        UserGroupVisibleMembers visibleMembers = new UserGroupVisibleMembers(groupName, user.getEmail());
        ofy().save().entity(visibleMembers).now();
        group.setVisibleMembers(visibleMembers);

        UserEndpoint userEndpoint = new UserEndpoint();
        // Die Newsboard Message erstellen
        postNewsBoard(group, "Die Gruppe wurde erstellt von " + userEndpoint.getPrimaryData(user, user.getEmail()).getFirstName());

        // Die Gruppe  muss NICHT gespeichert werden, dies passiert in postNewsBoard
        userEndpoint.addGroupToUser(user.getEmail(), group);
    }

    /**
     * Gibt eine Liste aller sichtbaren Mitglieder einer Nutzergruppe zurück.
     *
     * @param user
     * @param groupName Der Name der Nutzergruppe
     * @return Eine Klasse, die die sichtbaren Mitglieder einer Gruppe enthält
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public UserGroupVisibleMembers getUserGroupVisibleMembers(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupVisibleMembers.class).type(UserGroup.class).id(groupName).safe();
        return new UserGroupVisibleMembers(groupName, group.getVisibleMembers().getVisibleMembers());
    }

    /**
     * Ruft das BlackBoard zu dem übergebenen Nutzergruppennamen ab und verschickt
     * dies mit einer Tansportklasse UserGroupBlackBoardTransport.
     *
     * @param user      Der Benutzer, der das BlackBoard abrufen möchte
     * @param groupName Der Name der Nutzergruppe
     * @return Die Tansportklasse, die die BoardEntries enthält
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public UserGroupBlackBoardTransport getUserGroupBlackBoard(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupBlackBoardTransport.class).type(UserGroup.class).id(groupName).safe();
        if (group.getBlackBoard() != null) {
            return new UserGroupBlackBoardTransport(group.getName(), group.getBlackBoard().getBoardEntries());
        }
        return new UserGroupBlackBoardTransport(group.getName(), null);
    }

    /**
     * Ruft das BlackBoard zu dem übergebenen Nutzergruppennamen ab und verschickt
     * dies mit einer Tansportklasse UserGroupBlackBoardTransport.
     *
     * @param user      Der Benutzer, der das BlackBoard abrufen möchte
     * @param groupName Der Name der Nutzergruppe
     * @return Die Tansportklasse, die die BoardEntries enthält
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public UserGroupNewsBoardTransport getUserGroupNewsBoard(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupNewsBoardTransport.class).type(UserGroup.class).id(groupName).safe();
        if (group.getNewsBoard() != null) {
            return new UserGroupNewsBoardTransport(group.getName(), group.getNewsBoard().getBoardEntries());
        }
        return new UserGroupNewsBoardTransport(group.getName(), null);
    }

    /**
     * Ruft die Gruppen Metadaten zu dem angegebenen Namen ab.
     *
     * @param groupName Der Name der Nutzergruppe, deren Metadaten agerufen werden sollen
     * @return UserGroupMetaData oder null falls keine Nutzergruppe gefunden wurde
     */
    public UserGroupMetaData getUserGroupMetaData(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupMetaData.class).type(UserGroup.class).id(groupName).safe();
        return new UserGroupMetaData(group.getName(), group.getCreator(), group.isPrivat(), group.getMemberCount(), group.getBlobKey());

    }

    /**
     * Ruft die Nutzergruppe zu dem angegebenen Namen ab. Dabei wird das Passwort auf einen
     * leeren String gesetzt, egal ob eins angegeben ist oder nicht. Dies soll es erschweren
     * das verschlüsselte Passwort einer Nutzergruppe herauszufinden
     *
     * @param groupName Der Name der Nutzergruppe
     * @return Die Nutzergruppe, oder null falls keine gefunden wurde
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public UserGroup getUserGroup(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).now();
        if (group != null) {
            group.setPassword("");
            return group;
        }
        return null;
    }

    /**
     * Löscht die Gruppe, falls der aufrufende Benutzer der Ersteller der Gruppe ist.
     *
     * @param user      Der Benutzer, der den Löschvorgang durchführen möchte.
     * @param groupName Der Name, der zu löschenden Gruppe.
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public void deleteUserGroup(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup ug = ofy().load().group(UserGroupBlackBoardTransport.class, UserGroupNewsBoardTransport.class, UserGroupVisibleMembers.class).type(UserGroup.class).id(groupName).safe();
        if (hasRights(ug, user.getEmail(), Right.FULLRIGHTS.name())) {
            if (ug.getBlackBoard() != null) {
                if (ug.getBlackBoard().getBoardEntries() != null) {
                    ofy().delete().entities(ug.getBlackBoard().getBoardEntries());
                }
                ofy().delete().entity(ug.getBlackBoard()).now();
            }
            if (ug.getNewsBoard() != null) {
                if (ug.getNewsBoard().getBoardEntries() != null) {
                    ofy().delete().entities(ug.getNewsBoard().getBoardEntries());
                }
                ofy().delete().entity(ug.getNewsBoard()).now();
            }
            if (ug.getBlobKey() != null) {
                blobstoreService.delete(ug.getBlobKey());
            }
            if (ug.getVisibleMembers() != null) {
                ofy().delete().entity(ug.getVisibleMembers()).now();
            }
            // Die UserGroup aus den Benutzern entfernen und eine Notification
            // an die EndUser senden.
            removeGroupFromEndUsersAndSendNotification(ug);
            ofy().delete().entity(ug).now();
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Gibt eine Liste aller auf dem Server gespeicherter Benutzergruppen zurück.
     *
     * @return Eine Liste aller Benutzergruppen.
     */
    public List<UserGroup> getAllUserGroups(User user) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        Query<UserGroup> query = ofy().load().type(UserGroup.class);
        return query.list();
    }

    /**
     * Gibt eine Liste aller auf dem Server gespeicherten Metadaten zu Nutzergruppen
     * zurück.
     *
     * @return Einer Liste der Metadaten aller Nutzergruppen
     */
    public List<UserGroupMetaData> getAllUserGroupMetaDatas() {
        Query<UserGroup> query = ofy().load().type(UserGroup.class);
        if (query.list() != null) {
            ArrayList<UserGroupMetaData> metaDatas = new ArrayList<>();
            for (UserGroup group : query.list()) {
                metaDatas.add(new UserGroupMetaData(group.getName(), group.getCreator(), group.isPrivat(), group.getMemberCount(), group.getBlobKey()));
            }
            return metaDatas;
        }
        return new ArrayList<>();
    }

    /**
     * Ruft die Metadaten von Nutzergruppen ab. Dabei wird ein Cursor erstellt der maximal
     * viele Einträge läd, wie im Filter angegeben. Die Einträge werden nach Grppennamen
     * sortiert zurück gegeben und der Cursor wird gespeichert und mit zurück gesendet.
     * Dies ermöglicht, dass beim nächsten Aufruf der Cursor wieder erlangt werden kann
     * und der Query an der richtigen Stelle beginnt.
     *
     * @param user
     * @param groupFilter Gibt an, wie viele Einträge geladen werden sollen und an welcher
     *                    Stelle angefangen werden soll zu laden
     * @return List mit Metadaten zu Nutzergruppen und dem cursortstring
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    @ApiMethod(httpMethod = "POST")
    public UserGroupMetaDataList listUserGroups(User user, UserGroupFilter groupFilter) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        if (groupFilter == null) {
            throw new IllegalArgumentException("no filter submitted");
        }
        if (groupFilter.getLimit() <= 0) {
            throw new IllegalArgumentException("limit of the filter has to be greater than zero");
        }

        Query<UserGroup> query = ofy().load().group(UserGroupMetaData.class).type(UserGroup.class).limit(groupFilter.getLimit());
        if (groupFilter.getCursorString() != null) {
            query = query.startAt(Cursor.fromWebSafeString(groupFilter.getCursorString()));
        }
        query = query.order("__key__");
        QueryResultIterator<UserGroup> iterator = query.iterator();

        UserGroupMetaDataList list = new UserGroupMetaDataList();
        list.setMetaDatas(new ArrayList<UserGroupMetaData>());
        while (iterator.hasNext()) {
            UserGroup group = iterator.next();
            list.getMetaDatas().add(new UserGroupMetaData(group.getName(), group.getCreator(), group.isPrivat(), group.getMemberCount(), group.getBlobKey()));
        }
        list.setWebCursorString(iterator.getCursor().toWebSafeString());
        return list;
    }

    /**
     * Gibt eine Liste aller Benutzergruppen des angegebenen Benutzers zurück.Dabei wird
     * ein Cursor erstellt der maxima viele Einträge läd, wie im Filter angegeben. Die
     * Einträge werden nach Grppennamen sortiert zurück gegeben und der Cursor wird
     * gespeichert und mit zurück gesendet. Dies ermöglicht, dass beim nächsten Aufruf
     * der Cursor wieder erlangt werden kann und der Query an der richtigen Stelle beginnt.
     *
     * @return Eine Liste aller Benutzergruppen.
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    @ApiMethod(httpMethod = "POST")
    public UserGroupMetaDataList fetchMyUserGroups(User user, UserGroupFilter groupFilter) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        if (groupFilter == null) {
            throw new IllegalArgumentException("no filter submitted");
        }
        if (groupFilter.getLimit() <= 0) {
            throw new IllegalArgumentException("limit of the filter has to be greater than zero");
        }


        String key = "memberRights."+user.getEmail();
        Query<UserGroup> query = ofy().load().type(UserGroup.class).filter(key + " =", Right.NEWMEMBERRIGHTS.name()).limit(groupFilter.getLimit());
        if (groupFilter.getCursorString() != null) {
            query = query.startAt(Cursor.fromWebSafeString(groupFilter.getCursorString()));
        }
        query = query.order(key);
        QueryResultIterator<UserGroup> iterator = query.iterator();

        UserGroupMetaDataList list = new UserGroupMetaDataList();
        list.setMetaDatas(new ArrayList<UserGroupMetaData>());
        while (iterator.hasNext()) {
            UserGroup group = iterator.next();
            list.getMetaDatas().add(new UserGroupMetaData(group.getName(), group.getCreator(), group.isPrivat(), group.getMemberCount(), group.getBlobKey()));

        }
        list.setWebCursorString(iterator.getCursor().toWebSafeString());
        return list;
    }

    /**
     * Durchsucht alle auf dem Server existierenden Gruppen nacht dem angegebenen Schlüssel. Dabei werden
     * alle Gruppen der Liste hizugefügt, dessen Name mindestens den angegebenen Schlüssel enthalten. Dabei wird
     * nicht auf Groß und Kleinschreibung geachtet, also würde ein Schlüssel "test" eine Gruppe mit dem Namen
     * "Test" finden.
     *
     * @param user
     * @param groupFilter Der Filter, der die Referenz auf den Cursor hält und die Maximalt Anzahl an Entitäten die
     *                    geladen werden sollen bereit stellt
     * @param groupName Der Schlüssel nach dem gesucht werden soll
     * @return Eine Liste mit den Metadaten der Gefundene Gruppe, sowie dem String der den Link zum Cursor enthält
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    @ApiMethod(httpMethod = "POST")
    public UserGroupMetaDataList searchGroups(User user, UserGroupFilter groupFilter, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        if (groupFilter == null) {
            throw new IllegalArgumentException("no filter submitted");
        }
        if (groupFilter.getLimit() <= 0) {
            throw new IllegalArgumentException("limit of the filter has to be greater than zero");
        }

        Query<UserGroup> query = ofy().load().type(UserGroup.class).filter("searchName >=", groupName);
        query = query.filter("searchName <", groupName+"\ufffd");
        query = query.limit(groupFilter.getLimit());
        if (groupFilter.getCursorString() != null) {
            query = query.startAt(Cursor.fromWebSafeString(groupFilter.getCursorString()));
        }
        query = query.order("searchName");
        QueryResultIterator<UserGroup> iterator = query.iterator();

        UserGroupMetaDataList list = new UserGroupMetaDataList();
        list.setMetaDatas(new ArrayList<UserGroupMetaData>());
        while (iterator.hasNext()) {
            UserGroup group = iterator.next();
            list.getMetaDatas().add(new UserGroupMetaData(group.getName(), group.getCreator(), group.isPrivat(), group.getMemberCount(), group.getBlobKey()));

        }
        list.setWebCursorString(iterator.getCursor().toWebSafeString());
        return list;
    }

    /**
     * Ruft eine Bestimmte Liste von Gruppen vom Server ab. Hier wird kein Cursor verwendet, da der IN Befehl
     * mehrere Cursor verwendet und dies laut Doku nicht möglich ist.
     *
     * @param user
     * @param groupNames Die Gruppennamen, die abgerufen werden sollen
     * @return Eine Liste von Gruppenmetadaten zu den angegebenen Namen, falls welche gefunden wurden
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public List<UserGroupMetaData> fetchSpecificGroupDatas(User user, ListWrapper groupNames) throws  OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);

        Query<UserGroup> query = ofy().load().type(UserGroup.class).filter("searchName IN", groupNames.stringList);
        query = query.order("searchName");
        QueryResultIterator<UserGroup> iterator = query.iterator();

        List<UserGroupMetaData> list = new ArrayList<>();
        while (iterator.hasNext()) {
            UserGroup group = iterator.next();
            list.add(new UserGroupMetaData(group.getName(), group.getCreator(), group.isPrivat(), group.getMemberCount(), group.getBlobKey()));

        }
        return list;
    }

    /**
     * Fügt den aufrufenden Benutzer zu der angegebenen Gruppe hinzu.
     *
     * @param user      Der aufrufende Benutzer.
     * @param groupName Der Name der beizutretenden Gruppe
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public BooleanWrapper joinUserGroup(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        EndpointUtil.throwIfUserAlreadyInGroup(group, user.getEmail());
        EndUser endUser = throwIfNoEndUserFound(user.getEmail());

        group.addGroupMember(endUser.getEmail(), createNewMemberRightsList());
        group.getVisibleMembers().addVisibleMember(endUser.getEmail());
        ofy().save().entity(group.getVisibleMembers()).now();

        // News Message erstellen
        BoardEntry be = new BoardEntry(user.getEmail() + " ist der Gruppe beigetreten", user.getEmail());
        ofy().save().entity(be).now();
        group.getNewsBoard().addBoardMessage(be);
        ofy().save().entity(group.getNewsBoard()).now();
        ofy().save().entity(group).now();

        // Dem EndUser die Nutzergruppe hinzufügen
        new UserEndpoint().addGroupToUser(endUser.getEmail(), group);
        return new BooleanWrapper(true);
    }

    /**
     * Fügt den aufrufenden Benutzer zu der angegebenen Gruppe hinzu. Dabei wird das angegebene
     * Passwort überprüft und bei einem eine Misserfolg, ein Fehler ausgegeben und die Aufrufer
     * nicht der Gruppe hinzu gefügt.
     *
     * @param user          Der aufrufende Benutzer.
     * @param groupName     Der Name der beizutretenden Gruppe
     * @param groupPassword Das Password der Nutzergruppe, falls diese privat ist
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public BooleanWrapper joinPrivateUserGroup(User user, @Named("groupName") String groupName, @Named("groupPassword") String groupPassword) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        if (group.isPrivat() && !checkPassword(groupPassword, group.getPassword())) {
            return new BooleanWrapper(false);
        }
        EndpointUtil.throwIfUserAlreadyInGroup(group, user.getEmail());
        EndUser endUser = throwIfNoEndUserFound(user.getEmail());

        group.addGroupMember(endUser.getEmail(), createNewMemberRightsList());
        group.getVisibleMembers().addVisibleMember(endUser.getEmail());
        ofy().save().entity(group.getVisibleMembers()).now();

        // News Message erstellen
        BoardEntry be = new BoardEntry(user.getEmail() + " ist der Gruppe beigetreten", user.getEmail());
        ofy().save().entity(be).now();
        group.getNewsBoard().addBoardMessage(be);
        ofy().save().entity(group.getNewsBoard()).now();
        ofy().save().entity(group).now();

        // Dem EndUser die Nutzergruppe hinzufügen
        new UserEndpoint().addGroupToUser(endUser.getEmail(), group);
        return new BooleanWrapper(true);
    }

    /**
     * Entfernt den aufrufenden Benutzer aus der angegebenen Gruppe.
     *
     * @param user      Der aufrufende Benutzer.
     * @param groupName Der Name der zu verlassenden Gruppe.
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public void leaveUserGroup(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        EndpointUtil.throwIfLeaderTriesToLeaveGroup(group, user.getEmail());

        // Mitglied von den sichtbaren Mitgliedern entfernen
        group.removeGroupMember(user.getEmail());
        group.getVisibleMembers().removeVisibleMember(user.getEmail());
        ofy().save().entity(group.getVisibleMembers()).now();

        // News Message erstellen
        BoardEntry be = new BoardEntry(user.getEmail() + " hat die Gruppe verlassen", user.getEmail());
        ofy().save().entity(be).now();
        group.getNewsBoard().addBoardMessage(be);
        ofy().save().entity(group.getNewsBoard()).now();

        ofy().save().entity(group).now();

        new UserEndpoint().removeGroupFromUser(user.getEmail(), group);
    }

    /**
     * Löscht den EndUser aus der übergebenen Nutzergruppe. Der Aufrufer muss ein
     * Mitglieder der Nutzergruppe zu dem angegebenen Namen sein und die nötigen
     * Rechte haben ein Mitglied aus der Gruppe zu entfernen. Wenn nicht wird
     * ein Fehler ausgegeben
     *
     * @param groupName Die UserGroup aus der gelöscht werden soll
     * @param user      Der EndUser, der gelöscht werden soll
     * @param userName  Der Name des Mitglieds das entfernt werden soll
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public void removeMember(User user, @Named("groupName") String groupName, @Named("userName") String userName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.FULLRIGHTS.name()) && group.getMemberRights().keySet().contains(userName)) {

            group.removeGroupMember(userName);

            // News Message erstellen
            BoardEntry be = new BoardEntry(user.getEmail() + " ist aus der Gruppe geworfen worden", user.getEmail());
            ofy().save().entity(be).now();
            group.getNewsBoard().addBoardMessage(be);
            ofy().save().entity(group.getNewsBoard()).now();

            ofy().save().entity(group).now();

            new UserEndpoint().removeGroupFromUser(userName, group);
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Methode zum posten von BlackBoard Nachrichten in einer UserGroup. Es wird die
     * angegebene Nachricht und der Schreiber gespeichert und gleichzeitig eine
     * upload url in den Blobstore erstellt für den Fall, dass der Benutzer ein
     * Bild mit angegeben hat. Nach Erstellung wird der BoardEntry wieder zurück
     * and den Clienten, also den GroupController gesendet und dort verarbeitet.
     *
     * @param groupName Nutzergruppe, dessen BlackBoard eine neue Nachricht erhalten soll
     * @param message   Der Inhalt der Nachricht
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public BoardEntry postBlackBoard(User user, @Named("groupName") String groupName, @Named("boardMessage") String message) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        EndpointUtil.throwIfNotBoardMessageSubmitted(message);
        UserGroup group = ofy().load().group(UserGroupBlackBoardTransport.class).type(UserGroup.class).id(groupName).safe();
        if (hasRights(group, user.getEmail(), Right.NEWMEMBERRIGHTS.name())) {
            UserPrimaryData primaryData = new UserEndpoint().getPrimaryData(user, user.getEmail());
            if (primaryData == null) {
                throw new IllegalArgumentException("the submitted use is not registered properly");
            }
            BoardEntry be = new BoardEntry(message, primaryData.getFirstName());
            be.setUploadUrl(blobstoreService.createUploadUrl("/images/upload"));
            ofy().save().entity(be).now();
            if (group.getBlackBoard() == null) {
                Board board = new Board(groupName, be);
                ofy().save().entity(board).now();
                group.setBlackBoard(board);
            } else {
                group.getBlackBoard().addBoardMessage(be);
                ofy().save().entity(group.getBlackBoard()).now();
            }
            ofy().save().entity(group).now();
            return be;
        } else {
            EndpointUtil.throwIfNoRights();
        }
        return null;
    }

    /**
     * Methode um BlackBoard Nachrichten zu löschen. Dabei wird die BoardEntry id übergeben und in
     * der Liste von BoardEntries dieser gesucht und dann gelöscht.
     *
     * @param boardEntryId Die id des zu löschenden BoardEntry
     * @param groupName    Die UserGroup in der ein BoardEntry vom BlackBoard gelöscht werden soll
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public void deleteBlackBoardMessage(User user, @Named("boardEntryId") Long boardEntryId, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        EndpointUtil.throwIfNoBoardEntryIdISubmitted(boardEntryId);
        UserGroup group = ofy().load().group(UserGroupBlackBoardTransport.class).type(UserGroup.class).id(groupName).safe();

        if (hasRights(group, user.getEmail(), Right.EDITBLACKBOARD.name())) {
            group.getBlackBoard().removeBoardMessage(boardEntryId);
            ofy().delete().type(BoardEntry.class).id(boardEntryId).now();
            ofy().save().entity(group.getBlackBoard()).now();
            ofy().save().entity(group).now();
        } else {
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Kommentiert eine Nachricht des Blackboards und speichert diese.
     *
     * @param id      Id der Nachricht des Blackboards
     * @param comment Die Message
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public BoardEntry commentBlackBoard(User user, @Named("groupName") String groupName, @Named("boardId") Long id, @Named("comment") String comment) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.EDITBLACKBOARD.name())) {
            BoardEntry entry = ofy().load().type(BoardEntry.class).id(id).safe().addComment(comment, new UserEndpoint().getPrimaryData(user, user.getEmail()).getFirstName());
            ofy().save().entity(entry).now();
            return entry;
        }else{
            EndpointUtil.throwIfNoRights();
        }
        return null;
    }

    /**
     * Entfernt einen Kommentar aus einer Blackboard Nachricht, falls dieser gefunden wird.
     *
     * @param user
     * @param groupName Der Name der Nutzergruppe, damit auf Rechte überprüft werden kann
     * @param boardId Die ist der BoardEntrys und des
     * @param commentId  Die id des Kommentars
     * @throws OAuthRequestException
     */
    public void removeComment(User user, @Named("groupName") String groupName, @Named("boardId") Long boardId, @Named("commentId") long commentId) throws  OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.EDITBLACKBOARD.name())) {
            ofy().save().entity(ofy().load().type(BoardEntry.class).id(boardId).safe().removeComment(commentId)).now();
        }else{
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Editiert einen BlackBoard Eintrag mit der angegebenen Nachricht, insofern ein
     * Eintrag zu der angegebenen id existiert
     *
     * @param user
     * @param id Die is des BoardEntry
     * @param newMessage Die neue Nachricht
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public void editBoardEntry(User user, @Named("groupName") String groupName, @Named("boardId") Long id, @Named("newMessage") String newMessage) throws OAuthRequestException{
        EndpointUtil.throwIfNoUser(user);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.EDITBLACKBOARD.name())) {
            BoardEntry be = ofy().load().type(BoardEntry.class).id(id).safe();
            be.setMessage(newMessage);
            ofy().save().entity(be).now();
        }else{
            EndpointUtil.throwIfNoRights();
        }
    }

    /**
     * Gibt einen einzelnen BoardEntry Eintrag zurück, falls dieser existiert.
     *
     * @param id Die id des BoardEntry
     * @return Den BoardEntry zu der id, oder es wird ein Fehler ausgegeben
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public BoardEntry getBoardEntry(User user, @Named("boardId") Long id) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        return ofy().load().type(BoardEntry.class).id(id).safe();
    }

    /**
     * Methode zum posten von NewsBoard Nachrichten in einer UserGroup. Momentan wird die Nachricht und
     * der Schreiber in das BoardEntry eingetragen und die Liste in der UserGroup aktualisiert. Wenn 50
     * Einträge erricht sind, werden die ältesten Einträge gelöscht, damit immer maximal 50 Einträge existieren.
     *
     * @param group   UserGroup, dessen BlackBoard eine neue Nachricht erhalten soll
     * @param message Die Nachricht
     */
    private void postNewsBoard(UserGroup group, @Named("boardMessage") String message) throws OAuthRequestException {
        EndpointUtil.throwIfNotBoardMessageSubmitted(message);
        if (group == null) {
            throw new IllegalArgumentException("no group submitted");
        }

        BoardEntry be = new BoardEntry(message, group.getName());
        ofy().save().entity(be).now();
        if (group.getNewsBoard() == null) {
            Board board = new Board(group.getName(), be);
            ofy().save().entity(board).now();
            group.setNewsBoard(board);
        } else {
            group.getNewsBoard().addBoardMessage(be);
            ofy().save().entity(group.getNewsBoard()).now();
        }
        ofy().save().entity(group).now();
    }

//    /**
//     * Methode um BlackBoard Nachrichten zu löschen. Dabei wird der BoardEntry übergeben und in
//     * der Liste von BoardEntries dieser gesucht und dann gelöscht.
//     *
//     * @param boardEntryId der zu löschende BoardEntry
//     * @param groupName    die UserGroup in der ein BoardEntry vom BlackBoard gelöscht werden soll
//     */
//    private void deleteNewsBoardMessage(User user, @Named("boardEntryId") Long boardEntryId, @Named("groupName") String groupName) throws OAuthRequestException {
//        //TODO nutzlos, wieso sollte man Nachrichten vom Newsboard löschen wollen. Vielleicht git es noch einen Nutzen
//        EndpointUtil.throwIfNoUser(user);
//        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
//        EndpointUtil.throwIfNoBoardEntryIdISubmitted(boardEntryId);
//        UserGroup group = ofy().load().group(UserGroupNewsBoardTransport.class).type(UserGroup.class).id(groupName).safe();
//
//        if (hasRights(group, user.getEmail(), Right.FULLRIGHTS.name())) {
//            group.getNewsBoard().removeBoardMessage(boardEntryId);
//            ofy().delete().type(BoardEntry.class).id(boardEntryId).now();
//            ofy().save().entity(group.getNewsBoard()).now();
//            ofy().save().entity(group).now();
//        } else {
//            EndpointUtil.throwIfNoRights();
//        }
//    }

    /**
     * Diese Methode ändert für den Aufrufer die Sichtbarkeit in der übergebenen Nutzergruppe
     * auf der Streckenkarte während eines aktiven Events. Dazu muss der Aufrufer ein
     * Mitglied der Nutzergruppe sein.
     *
     * @param user
     * @param groupName Der Name Der Nutzergruppe
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public UserGroupVisibleMembers changeMyVisibility(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = ofy().load().group(UserGroupVisibleMembers.class).type(UserGroup.class).id(groupName).safe();
        if (group.getGroupType().equals(UserGroupType.SECURITYGROUP)) {
            throw new IllegalArgumentException("You canno't change your visibility in a security user group");
        }
        if (group.getVisibleMembers().getVisibleMembers() == null && group.getMemberRights().containsKey(user.getEmail())) {
            group.getVisibleMembers().addVisibleMember(user.getEmail());
        } else if (!group.getVisibleMembers().getVisibleMembers().contains(user.getEmail()) && group.getMemberRights().containsKey(user.getEmail())) {
            group.getVisibleMembers().addVisibleMember(user.getEmail());
        } else {
            group.getVisibleMembers().removeVisibleMember(user.getEmail());
            ofy().save().entity(group.getVisibleMembers()).now();
        }
        ofy().save().entity(group.getVisibleMembers()).now();
        return group.getVisibleMembers();
    }

    /**
     * Diese Methode ändert das Passwort einer Nutergruppe, falls das übergebene derzeitige Password korrekt und
     * der Aufrufer die nötigen Rechte hat dieses zu ändern. Der Aufrufer muss der Leader einer Nutzergruppe sein,
     * es gibt kein anderes Recht, welches einem ermöglicht das Passwort einer Nutzergruppe zu ändern.
     *
     * @param user
     * @param groupName Der Name der Nutzergruppe
     * @param currentPw Das momentane Passwort
     * @param newPw     Das neue Passwort
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public BooleanWrapper changeUserGroupPassword(User user, @Named("groupName") String groupName, @Named("currentPassword") String currentPw, @Named("newPassword") String newPw) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        if (newPw == null || newPw.isEmpty()) {
            throw new IllegalArgumentException("no new password submitted");
        }
        UserGroup group = ofy().load().type(UserGroup.class).id(groupName).safe();
        if (hasRights(group, user.getEmail(), Right.FULLRIGHTS.name())) {
            if (checkPassword(currentPw, group.getPassword())) {
                group.setPassword(hashPassword(newPw));

                // News Message erstellen
                BoardEntry be = new BoardEntry(user.getEmail() + " hat das Passwort geändert", user.getEmail());
                ofy().save().entity(be).now();
                group.getNewsBoard().addBoardMessage(be);
                ofy().save().entity(group.getNewsBoard()).now();

                ofy().save().entity(group).now();
                return new BooleanWrapper(true);
            } else {
                throw new IllegalArgumentException("the submitted current password is wrong");
            }
        } else {
            EndpointUtil.throwIfNoRights();
        }
        return new BooleanWrapper(false);
    }

    /**
     * Methode zum ändern der Öffentlichkeit einer Nutzergruppe. EndUser können öffentlichen Nutzergruppen
     * ohne weiteres beitreten. Nicht öffentliche Nutzergruppen sind durch Passwörter geschützt.
     *
     * @param user
     * @param groupName Der Name der Nutzergruppe
     * @param groupPassword Das Passwort wird benötigt um eine Gruppe privat zu machen
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public BooleanWrapper makeUserGroupPrivat(User user, @Named("groupName") String groupName, @Named("groupPassword") String groupPassword) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        if (groupPassword == null || groupPassword.isEmpty()) {
            throw new IllegalArgumentException("making a user group private means you have to submit a password");
        }
        UserGroup group = throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.FULLRIGHTS.name())) {
            group.setPrivat(true);
            group.setPassword(hashPassword(groupPassword));
            ofy().save().entity(group).now();
            return new BooleanWrapper(true);
        } else {
            EndpointUtil.throwIfNoRights();
        }
        return new BooleanWrapper(false);
    }

    /**
     * Methode zum ändern der Öffentlichkeit einer Nutzergruppe. EndUser können öffentlichen Nutzergruppen
     * ohne weiteres beitreten. Nicht öffentliche Nutzergruppen sind durch Passwörter geschützt.
     *
     * @param user
     * @param groupName Der Name der Nutzergruppe
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public BooleanWrapper makeUserGroupOpen(User user, @Named("groupname") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = throwIfNoUserGroupExists(groupName);
        if (hasRights(group, user.getEmail(), Right.FULLRIGHTS.name())) {
            group.setPrivat(false);
            group.setPassword("");
            ofy().save().entity(group).now();
            return new BooleanWrapper(true);
        } else {
            EndpointUtil.throwIfNoRights();
        }
        return new BooleanWrapper(false);
    }

    /**
     * Diese Methode gibt das Bild einer Nutzergruppe zurück, oder null wenn keins existiert.
     *
     * @param user
     * @param groupName Der Name der Nutzergruppe
     * @return Der BlobKey, ein Objekt welches einen String mit dem Schlüssel des Blobs im
     * Blobstore hat. Diesen kann man auslesen und so an den Blob im Blobstore gelangen.
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public BlobKey getUserGroupPicture(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        return ofy().load().type(UserGroup.class).id(groupName).safe().getBlobKey();
    }

    /**
     * Gibt die UploadUrl des Blobstores zurück. Dient zum Uploaden von Blobs in den Blobstore.
     * Dient hier hauptsächlich für Bilder
     *
     * @return UploadUrl Der Link zu dem Blob, welcher hochgeladen werden soll, aber noch nicht
     * hochgeladen ist!
     */
    public StringWrapper previewImageUploadUrl(User user) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        return new StringWrapper(blobstoreService.createUploadUrl("/Bernd/images/upload"));
    }

    /**
     * Diese Methode existiert nur, damit gespeichert wird welche Bilder von
     * Nutzern beim erstellen von Gruppen hochgeladen wurden.
     */
    public UserGroupPreviewPictures getUserGroupPreviewPictures() {
        UserGroupPreviewPictures preview = ofy().load().type(UserGroupPreviewPictures.class).id("007").now();
        if (preview == null) {
            preview = new UserGroupPreviewPictures("007");
            ofy().save().entity(preview).now();
        }
        return preview;
    }

    /**
     * Diese Methode existiert nur, damit hochgeladene aber nicht verwendete blobkeys gelöscht werden
     * können.
     */
    public void cleanPreviewPictures() {
        if (getUserGroupPreviewPictures().getBlobKeysValues() != null) {
            for (String key : getUserGroupPreviewPictures().getBlobKeysValues()) {
                blobstoreService.delete(new BlobKey(key));
            }
        }
    }

    /**
     * Sendet eine Nachricht an alle Mitglieder der übergebenen UserGroup. Dabei wird
     * der RegistrationManager benutzt um eine Notification and die Handys der
     * Mitglieder der angegebenen Gruppe zu senden.
     *
     * @param groupName     Der Name der Nutzergruppe, dessen Mitglieder eine Nachricht erhalten sollen
     * @param globalMessage die Nachricht die gesendet werden soll
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public void sendGlobalMessage(User user, @Named("groupName") String groupName, @Named("globalMessage") String globalMessage) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        UserGroup group = throwIfNoUserGroupExists(groupName);

        // Benutzer abrufen, die in der Gruppe sind und Sendern der Nachricht
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
                regids.add(rm.getUserIdByMail(enduser.getEmail()));
            }

            // Notification senden
            Sender sender = new Sender(Constants.GCM_API_KEY);
            Message.Builder mb = new Message.Builder()
                    // Nachricht erst anzeigen, wenn der Benutzer sein Handy benutzt
                    .delayWhileIdle(false)
                    .collapseKey("group_" + group.getName() + "_deleted")
                            // Nachricht verfallen lassen, wenn Benutzer erst nach Event online geht
                    .addData("type", MessageType.GLOBAL_GROUP_MESSAGE.name())
                    .addData("content", globalMessage)
                    .addData("title", group.getName());
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

    /**
     * Diese Methode sendet eine Einladung(Notification) zu der übergebene UserGroup and den
     * übergebenen EndUser mit der Möglichkeit diese anzunehmen oder abzulehnen.Dabei wird
     * der RegistrationManager benutzt um eine Notification and die Handys der in der Liste
     * angegebenen Mitglieder zu senden.
     *
     * @param groupName Der Name der Nutzergruppe, zu der die Benutzer eingeladen werden sollen
     * @param users     Die Benutzer, die in die Gruppe eingeladen werden sollen.
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public void sendInvitation(User user, @Named("groupName") String groupName, ListWrapper users) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        if (users == null) {
            throw new IllegalArgumentException("no list of users submitted");
        }
        if (users.stringList == null || users.stringList.isEmpty()) {
            throw new IllegalArgumentException("no list inside the wrapper");
        }
        throwIfNoUserGroupExists(groupName);
        // send a message to the endUser asking if he wants to join the group
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Set<String> regids = new HashSet<>();
            RegistrationManager rm = getRegistrationManager(pm);
            for (String userToInvite : users.stringList) {
                regids.add(rm.getUserIdByMail(userToInvite));
            }

            // Notification senden
            Sender sender = new Sender(Constants.GCM_API_KEY);
            Message.Builder mb = new Message.Builder()
                    // Nachricht erst anzeigen, wenn der Benutzer sein Handy benutzt
                    .delayWhileIdle(false)
                    .timeToLive(6000)
                    .collapseKey("group_" + groupName + "_deleted")
                            // Nachricht verfallen lassen, wenn Benutzer erst nach Event online geht
                    .addData("type", MessageType.INVITATION_TO_GROUP_MESSAGE.name())
                    .addData("content", "Sie haben eine Einladung von " + user.getEmail() + " in die Nutzergruppe " + groupName + " erhalten.")
                    .addData("title", groupName);
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

    /**
     * Methode zum abrufen der Positionsdaten der Mitglieder der übergebenen Gruppe. Dabei
     * werden nur die Daten der sichtbaren Mitglieder abgerufen, in ein dafür vorgesehenes
     * Objekt UserLocationInfo geschrieben und der Liste der Positionsdaten hinzugefügt.
     * Die fertige Liste wird dann zurück gegeben.
     *
     * @param user
     * @param groupName Der Name der Gruppe, dessen Positionsdaten der Mitglieder ausgelesen
     *                  werden sollen
     * @return Eine Liste der Positionsdaten der sichtbaren Mitglieder der angegebenen Gruppe
     * @throws OAuthRequestException Wird geschmissen, falls der Aufrufer nicht registriert ist
     */
    public List<UserLocationInfo> listUserGroupVisibleMembersLocationInfo(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        EndpointUtil.throwIfNoUser(user);
        EndpointUtil.throwIfUserGroupNameWasntSubmitted(groupName);
        return new UserEndpoint().listUserLocationInfo(user, getUserGroupVisibleMembers(user, groupName).getVisibleMembers());
    }
//======================================================================================
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
            user.getUserInfo();
            user.getUserLocation();
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
            endUser.getUserLocation();
            endUser.getUserInfo();
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

    private void removeGroupFromEndUsersAndSendNotification(UserGroup group) {
        // Benutzer abrufen, die in der Gruppe sind und Löschen der Nutzergruppe
        EndUser[] members = new EndUser[group.getMemberCount()];
        int index = 0;
        for (String member : group.getMemberRights().keySet()) {
            members[index] = throwIfNoEndUserFound(member);
            index++;
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Set<String> regids = new HashSet<>();
            RegistrationManager rm = getRegistrationManager(pm);
            for (EndUser enduser : members) {
                if (rm.getUserIdByMail(enduser.getEmail()) != null) {
                    regids.add(rm.getUserIdByMail(enduser.getEmail()));
                }
                new UserEndpoint().removeGroupFromUser(enduser.getEmail(), group);
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
        tmpList.add(Right.NEWMEMBERRIGHTS.name());
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
     */
    private static UserGroup throwIfNoUserGroupExists(String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        UserGroup group = new GroupEndpoint().getUserGroup(groupName);
        if (group == null) {
            throw new IllegalArgumentException("user group with submitted name " + groupName + " could not be found");
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
    private static void throwIfUserGroupAlreadyExists(String groupName) {
        if (new GroupEndpoint().getUserGroup(groupName) != null) {
            throw new IllegalArgumentException("user group with submitted name " + groupName + " already exists");
        }
    }


// Definieren der BCrypt worload, welche beim generieren von Passwörtern  benutzt wird.
// Man kann hier 10-31 angeben
private static int workload = 12;

    /**
     * Diese Methode generiert einen String, der 60 Zeichen lang ist der
     * ein Passwort repräsentiert. Hier wird auch automatisch ein gewisser
     * Salt Wert generiert, der gleichzeitig im String gespeichert ist.
     *
     * @param passwordPlain Das Password als Plaintext
     * @return Einen String mit 60 Zeichen der dem Passwort entspricht
     */
    private static String hashPassword(String passwordPlain) {
        String salt = BCrypt.gensalt(workload);
        String hashed_password = BCrypt.hashpw(passwordPlain, salt);

        return hashed_password;
    }

    private static boolean checkPassword(String passwordPlain, String passwordHash) {
        boolean passwordVerified = false;

        if (null == passwordHash || !passwordHash.startsWith("$2a$"))
            throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison");

        passwordVerified = BCrypt.checkpw(passwordPlain, passwordHash);

        return passwordVerified;
    }
}