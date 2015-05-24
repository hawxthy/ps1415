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
import ws1415.SkatenightBackend.transport.UserGroupPicture;

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
    @Load(unless = UserGroupMetaData.class)
    private HashMap<String, ArrayList<String>> memberRights;
    @Load(UserGroupPicture.class)
    private Ref<UserGroupPicture> picture;
    @Load(UserGroupBlackBoardTransport.class)
    private ArrayList<BoardEntry> blackBoard;
    @Load(UserGroupNewsBoardTransport.class)
    private ArrayList<BoardEntry> newsBoard;


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

    public ArrayList<BoardEntry> getBlackBoard() {
        return blackBoard;
    }

    public void setBlackBoard(ArrayList<BoardEntry> blackBoard) {
        this.blackBoard = blackBoard;
    }

    public ArrayList<BoardEntry> getNewsBoard() {
        return newsBoard;
    }

    public void setNewsBoard(ArrayList<BoardEntry> newsBoard) {
        this.newsBoard = newsBoard;
    }

    public void addBlackBoardMessage(BoardEntry be){
        if(blackBoard == null){
            blackBoard = new ArrayList<BoardEntry>();
        }
        blackBoard.add(be);
    }

    public void removeBlackBoardMessage(long boardEntryId){
        if(blackBoard != null){
            for(int i = 0; i < blackBoard.size(); i++){
                if(blackBoard.get(i).getId() == boardEntryId){
                    blackBoard.remove(blackBoard.get(i));
                }
            }
        }
    }

    public void addNewsBoardMessage(BoardEntry be){
        if(newsBoard == null){
            newsBoard = new ArrayList<BoardEntry>();
        }
        newsBoard.add(be);
    }

    public void removeNewsBoardMessage(long boardEntryId){
        if(newsBoard != null){
            for(int i = 0; i < newsBoard.size(); i++){
                if(newsBoard.get(i).getId() == boardEntryId){
                    newsBoard.remove(newsBoard.get(i));
                }
            }
        }
    }

    /**
     * Embedded Klasse zum modellieren von BlackBoard einträgen. Hier sollte
     * IMMER der Konstruktor BoardEntry(String writer, String message) benutzt
     * werden, sonst kann dieser BoardEntry später nicht im BlackBoard
     * gefunden werden.
     */
    public static class BoardEntry {
        private long id;
        private String writer;
        private String message;

        public BoardEntry() {
            // Konstruktor für GAE
        }

        public BoardEntry(String message, String writer, long id) {
            this.writer = writer;
            this.message = message;
            this.id = id;
        }
        private void setId(long id){
            this.id = id;
        }

        public long getId(){
            return id;
        }

        public String getWriter() {
            return writer;
        }

        public void setWriter(String writer) {
            this.writer = writer;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

