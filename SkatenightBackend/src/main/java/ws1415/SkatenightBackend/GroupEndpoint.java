package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import ws1415.SkatenightBackend.gcm.Message;
import ws1415.SkatenightBackend.gcm.MessageType;
import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.gcm.Sender;
import ws1415.SkatenightBackend.model.BoardEntry;
import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.Member;
import ws1415.SkatenightBackend.model.Picture;
import ws1415.SkatenightBackend.model.UserGroup;

/**
 * Created by Richard on 01.05.2015.
 */
public class GroupEndpoint extends SkatenightServerEndpoint {

    /**
     * Ruft die Gruppe mit dem angegebenen Namen ab.
     * @param name Der Name der abzurufenden Gruppe.
     * @return Die UserGroup-Entity.
     */
    protected UserGroup getUserGroup(String name) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

        try {
            return getUserGroup(pm, name);
        } finally {
            pm.close();
        }
    }
    protected UserGroup getUserGroup(PersistenceManager pm, String name) {
        Query q = pm.newQuery(UserGroup.class);
        q.setFilter("name == nameParam");
        q.declareParameters("String nameParam");
        List<UserGroup> existingGroups = (List<UserGroup>) q.execute(name);
        if (existingGroups.isEmpty()) {
            return null;
        } else {
            return existingGroups.get(0);
        }
    }

    /**
     * Gibt eine Liste aller auf dem Server gespeicherter Benutzergruppen zurück.
     * @return Eine Liste aller Benutzergruppen.
     */
    public List<UserGroup> getAllUserGroups() {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

        try {
            List<UserGroup> result = (List<UserGroup>) pm.newQuery(UserGroup.class).execute();
            return result;
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt eine Liste aller Benutzergruppen des angegebenen Benutzers zurück.
     * @return Eine Liste aller Benutzergruppen.
     */
    public List<UserGroup> fetchMyUserGroups(User user) throws OAuthRequestException {
        Member member;
        if (user == null || (member = new UserEndpoint().getMember(user.getEmail())) == null) {
            // Falls kein Benutzer angegeben, dann leere Liste zurückgeben.
            return new ArrayList<>();
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        UserGroup ug;
        try {
            List<UserGroup> result = new LinkedList<>();
            List<String> missingGroups = new LinkedList<>();
            for (String g : member.getGroups()) {
                ug = getUserGroup(pm, g);
                if (ug != null) {
                    result.add(ug);
                } else {
                    missingGroups.add(g);
                }
            }
            if (missingGroups.size() > 0) {
                for (String g : missingGroups) {
                    member.getGroups().remove(g);
                }
                pm.makePersistent(member);
            }
            return result;
        } finally {
            pm.close();
        }
    }

    /**
     * Erstellt eine Gruppe mit dem angegebenen Namen
     * @param user Der Benutzer, der die Gruppe anlegen möchte.
     * @param name Der Name der neuen Gruppe.
     */
    public void createUserGroup(User user, @Named("name") String name) throws OAuthRequestException {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        Member member = new UserEndpoint().getMember(user.getEmail());
        if (member == null) {
            throw new IllegalArgumentException("user is not registered");
        }
        if (getUserGroup(name) != null) {
            throw new IllegalArgumentException("group with submitted name already exists");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserGroup ug = new UserGroup(member);
            ug.setName(name);
            member.addGroup(ug);
            pm.makePersistent(member);
            pm.makePersistent(ug);
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
     * Löscht die Gruppe, falls der aufrufende Benutzer der Ersteller der Gruppe ist.
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
        Member member = userEndpoint.getMember(user.getEmail());
        if (member == null) {
            throw new IllegalArgumentException("user is not registered");
        }
        UserGroup ug = getUserGroup(name);
        if (ug != null) {
            if (!ug.getCreator().getEmail().equals(user.getEmail())) {
                throw new IllegalArgumentException("user is not creator of group");
            }

            // Benutzer abrufen, die in der Gruppe sind
            Member[] members = new Member[ug.getMemberRanks().size()];
            int index = 0;
            for (String mail: ug.getMemberRanks().values()) {
                members[index++] = userEndpoint.getMember(mail);
            }

            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            try {
                Set<String> regids = new HashSet<>();
                RegistrationManager rm = getRegistrationManager(pm);
                for (Member m : members) {
                    m.removeGroup(ug);
                    pm.makePersistent(m);
                    regids.add(rm.getUserIdByMail(m.getEmail()));
                }
                pm.makePersistent(ug);
                pm.deletePersistent(ug);

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
     * Fügt den aufrufenden Benutzer zu der angegebenen Gruppe hinzu.
     * @param user Der aufrufende Benutzer.
     * @param groupName Der Name der beizutretenden Gruppe
     */
    public void joinUserGroup(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        Member member = new UserEndpoint().getMember(user.getEmail());
        if (member == null) {
            throw new IllegalArgumentException("user is not registered");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserGroup ug = getUserGroup(pm, groupName);
            if (ug == null) {
                throw new IllegalArgumentException("a group with the submitted group name does not exist");
            }
            member.addGroup(ug);
            pm.makePersistent(member);
            pm.makePersistent(ug);
        } finally {
            pm.close();
        }
    }

    /**
     * Entfernt den aufrufenden Benutzer aus der angegebenen Gruppe.
     * @param user Der aufrufende Benutzer.
     * @param groupName Der Name der zu verlassenden Gruppe.
     */
    public void leaveUserGroup(User user, @Named("groupName") String groupName) {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        Member member = new UserEndpoint().getMember(user.getEmail());
        if (member == null) {
            throw new IllegalArgumentException("user is not registered");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            UserGroup ug = getUserGroup(pm, groupName);
            if (ug == null) {
                throw new IllegalArgumentException("a group with the submitted group name does not exist");
            }
            if (user.getEmail().equals(ug.getCreator().getEmail())) {
                throw new IllegalArgumentException("you can not leave your own group");
            }
            member.removeGroup(ug);
            pm.makePersistent(member);
            pm.makePersistent(ug);
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
    public ArrayList<Member> fetchGroupMembers(@Named("userGroup") String userGroup){
        ArrayList<Member> members = new ArrayList<>();
        UserGroup tmpGroup = getUserGroup(userGroup);
        UserEndpoint userEndpoint = new UserEndpoint();
        for(String s : tmpGroup.getMemberRanks().values()){
            members.add(userEndpoint.getMember(s));
        }
        return members;
    }

    public void setVisibility(@Named("groupName") String groupName, BooleanWrapper visibility){
        //TODO Lösung für den PrefManager finden.
    }

    public void sendInvitation(UserGroup u, @Named("userName") String user, @Named("message") String message){
        //TODO Implementieren.
    }

    public BooleanWrapper removeMember(UserGroup u, @Named("userName") String user){
        //TODO Implementieren.
        return new BooleanWrapper(true);
    }

    public void sendGlobalMessage(UserGroup u, @Named("message") String message){
        //TODO Implementieren.
    }

    public BooleanWrapper sendMessage(UserGroup u, @Named("message") String message, @Named("userName") String user){
        //TODO Implementieren.
        return new BooleanWrapper(true);
    }

    public BooleanWrapper postBlackBoard(UserGroup u, @Named("message") String Message){
        //TODO Implementieren.
        return new BooleanWrapper(true);
    }

    public BooleanWrapper deleteBoardMessage(@Named("groupName") String groupName, BoardEntry be){
        //TODO Implementieren.
        return new BooleanWrapper(true);
    }

    public BooleanWrapper changePicture(@Named("gropuName") String gropuName, Picture p){
        //TODO Implementieren.
        return new BooleanWrapper(true);
    }

}
