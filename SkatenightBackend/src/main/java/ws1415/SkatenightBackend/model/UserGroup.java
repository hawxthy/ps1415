package ws1415.SkatenightBackend.model;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.HashMap;

import ws1415.SkatenightBackend.transport.UserGroupBlackBoardTransport;
import ws1415.SkatenightBackend.transport.UserGroupMetaData;
import ws1415.SkatenightBackend.transport.UserGroupNewsBoardTransport;

/**
 * Repräsentiert eine Benutzergruppe.
 *
 * @author Bernd Eissing
 */
@Entity
public class UserGroup {
    @Id
    private String name;            // Eindeutiger Name der Gruppe
    private String creator;
    private boolean open;
    private int memberCount;
    private String groupType;
    @Load(unless = UserGroupMetaData.class)
    private HashMap<String, ArrayList<String>> memberRights;
    @Load(UserGroupPicture.class)
    private Ref<UserGroupPicture> picture;
    @Load(UserGroupBlackBoardTransport.class)
    private Ref<Board> blackBoard;
    @Load(UserGroupNewsBoardTransport.class)
    private Ref<Board> newsBoard;


    public UserGroup() {
        // Konstruktor für GAE
    }

    public UserGroup(String creator, String groupType) {
        if (creator == null) {
            throw new IllegalArgumentException("creator can not be null");
        }
        this.creator = creator;
        this.groupType = groupType;
    }

    public String getCreator() {
        return creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getGroupType() {
        return groupType;
    }

    public UserGroupPicture getPicture() {
        if (picture != null) {
            return picture.get();
        }
        return null;
    }

    public void setPicture(UserGroupPicture picture) {
        this.picture = Ref.create(picture);
    }

    public HashMap<String, ArrayList<String>> getMemberRights() {
        if (memberRights != null) {
            return memberRights;
        }
        return null;
    }

    public void setMemberRights(HashMap<String, ArrayList<String>> memberRights) {
        this.memberRights = memberRights;
    }

    public void addGroupMember(String member, ArrayList<String> rights) {
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



}

