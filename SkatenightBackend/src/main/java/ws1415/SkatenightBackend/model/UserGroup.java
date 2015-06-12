package ws1415.SkatenightBackend.model;

import com.google.appengine.api.blobstore.BlobKey;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.HashMap;

import ws1415.SkatenightBackend.transport.UserGroupBlackBoardTransport;
import ws1415.SkatenightBackend.transport.UserGroupMetaData;
import ws1415.SkatenightBackend.transport.UserGroupNewsBoardTransport;
import ws1415.SkatenightBackend.transport.UserGroupVisibleMembers;

/**
 * Repräsentiert eine Benutzergruppe.
 *
 * @author Bernd Eissing
 */
@Entity
public class UserGroup {
    @Id
    private String name;            // Eindeutiger Name der Gruppe
    @Index
    @Load
    private String searchName;
    private String password;
    private String creator;
    private String description;
    private boolean privat;
    private int memberCount;
    private UserGroupType groupType;
    private BlobKey blobKey;
    @Index
    @Load//(unless = {UserGroupBlackBoardTransport.class, UserGroupNewsBoardTransport.class})
    private HashMap<String, ArrayList<String>> memberRights;
    @Load(unless = {UserGroupMetaData.class, UserGroupNewsBoardTransport.class, UserGroupVisibleMembers.class})
    private Ref<Board> blackBoard;
    @Load(unless = {UserGroupMetaData.class, UserGroupBlackBoardTransport.class, UserGroupVisibleMembers.class})
    private Ref<Board> newsBoard;
    @Load(unless = {UserGroupMetaData.class, UserGroupBlackBoardTransport.class, UserGroupNewsBoardTransport.class})
    private Ref<UserGroupVisibleMembers> visibleMembers;


    public UserGroup() {
        // Konstruktor für GAE
    }


    /**
     * Konstruktor für Nutzergruppen
     * angegeben werden.
     *
     * @param groupName Der Name der Gruppe
     * @param creator Der Ersteller der Gruppe
     * @param groupType Der Type der Grupp
     * @param isPrivat Die Öffentlichkeit der Gruppe
     * @param password Das Passwort der Gruppe
     * @param description Die Beschreibung der Gruppe
     * @param blobKey Das Bild der Gruppe
     */
    public UserGroup(String groupName, String creator, UserGroupType groupType, boolean isPrivat, String password, String description, BlobKey blobKey) {
        if (creator == null) {
            throw new IllegalArgumentException("creator can not be null");
        }
        this.name = groupName;
        this.creator = creator;
        this.groupType = groupType;
        this.privat = isPrivat;
        this.password = password;
        this.description = description;
        this.memberCount = 0;
        this.blobKey = blobKey;
        this.searchName = groupName.toLowerCase();
    }

    public String getCreator() {
        return creator;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrivat() {
        return privat;
    }

    public void setPrivat(boolean privat) {
        this.privat = privat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public UserGroupType getGroupType() {
        return groupType;
    }

    public BlobKey getBlobKey() {
        return blobKey;
    }

    public void setBlobKey(BlobKey blobKey) {
        this.blobKey = blobKey;
    }

    public HashMap<String, ArrayList<String>> getMemberRights() {
        return memberRights;
    }

    public void setMemberRights(HashMap<String, ArrayList<String>> memberRights) {
        this.memberRights = memberRights;
    }

    public String getSearchName() {
        return searchName;
    }

    public void addGroupMember(String member, ArrayList<String> rights) {
        if(memberRights == null){
            memberRights = new HashMap<>();
        }
        memberRights.put(member, rights);
        memberCount++;
    }

    public void removeGroupMember(String member) {
        memberRights.remove(member);
        memberCount--;
    }

    public Board getBlackBoard() {
        if (blackBoard != null) {
            return blackBoard.get();
        }
        return null;
    }

    public void setBlackBoard(Board board) {
        this.blackBoard = Ref.create(board);
    }

    public Board getNewsBoard() {
        if (newsBoard != null) {
            return newsBoard.get();
        }
        return null;
    }

    public void setNewsBoard(Board board) {
        this.newsBoard = Ref.create(board);
    }

    public UserGroupVisibleMembers getVisibleMembers() {
        if(visibleMembers != null){
            return visibleMembers.get();
        }
        return null;
    }

    public void setVisibleMembers(UserGroupVisibleMembers visibleMembers) {
        this.visibleMembers = Ref.create(visibleMembers);
    }
}

