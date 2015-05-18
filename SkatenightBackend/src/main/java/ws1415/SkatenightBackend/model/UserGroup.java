package ws1415.SkatenightBackend.model;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.HashMap;

import ws1415.SkatenightBackend.transport.UserGroupMetaData;
import ws1415.SkatenightBackend.transport.UserGroupPicture;

/**
 * Repräsentiert eine Benutzergruppe.
 * @author Bernd Eissing
 */
@Entity
public class UserGroup {
    @Id
    private String name;            // Eindeutiger Name der Gruppe
    private String creator;
    private boolean open;
    private int memberCount;
    @Load(unless=UserGroupMetaData.class)
    private HashMap<String, ArrayList<String>> memberRights;
    @Load(UserGroupPicture.class)
    private Ref<UserGroupPicture> picture;
    @Load(UserGroupBlackBoard.class)
    private Ref<UserGroupBlackBoard> blackBoard;
    @Load(UserGroupNewsBoard.class)
    private Ref<UserGroupNewsBoard> newsBoard;


    public UserGroup() {
        // Konstruktor für GAE
    }

    public UserGroup(String creator) {
        if (creator == null) {
            throw new IllegalArgumentException("creator can not be null");
        }
        this.creator = creator;

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

    public UserGroupPicture getPicture() {
        return picture.get();
    }

    public void setPicture(UserGroupPicture picture) {
        this.picture = Ref.create(picture);
    }

    public HashMap<String, ArrayList<String>> getMemberRights() {
        return memberRights;
    }

    public void setMemberRights(HashMap<String, ArrayList<String>> memberRights) {
        this.memberRights = memberRights;
    }

    public Ref<UserGroupBlackBoard> getBlackBoard() {
        if(blackBoard !=null){
            return blackBoard;
        }
        return  null;
    }

    public void setBlackBoard(UserGroupBlackBoard blackBoard) {
        this.blackBoard = Ref.create(blackBoard);
    }

    public UserGroupNewsBoard getNewsBoard() {
        if(newsBoard != null){
            return newsBoard.get();
        }
        return null;
    }

    public void setNewsBoard(UserGroupNewsBoard newsBoard) {
        this.newsBoard = Ref.create(newsBoard);
    }

    public void addGroupMember(String member, ArrayList<String> rights){
        memberRights.put(member, rights);
    }

    public void removeGroupMember(String member){
        memberRights.remove(member);
    }
}
