package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Mapify;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.mapper.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repräsentiert eine Benutzergruppe.
 * @author Bernd Eissing
 */
@Entity
public class UserGroup {
    @Id
    private String name;            // Eindeutiger Name der Gruppe
    private boolean open;
    private String creator;
    private Picture picture;
    @Load
    private ArrayList<GroupMemberRights> memberRanks;
    @Load
    @Parent
    private Ref<GroupMetaData> metaData;
    private ArrayList<Ref<BoardEntry>> news;
    private ArrayList<Ref<BoardEntry>> blackBoard;


    public UserGroup() {
        // Konstruktor für GAE
    }

    public UserGroup(String creator) {
        if (creator == null) {
            throw new IllegalArgumentException("creator can not be null");
        }
        this.creator = creator;
        memberRanks = new ArrayList<GroupMemberRights>();
        news = new ArrayList<Ref<BoardEntry>>();
        blackBoard = new ArrayList<Ref<BoardEntry>>();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getCreator() {
        return creator;
    }

    public ArrayList<GroupMemberRights> getMemberRanks() {
        return memberRanks;
    }

    public void setMemberRanks(ArrayList<GroupMemberRights> memberRanks) {
        this.memberRanks = memberRanks;
    }

    /**
     * Methode um die Rechte von Mitgliedern der Usergruppe zu setzen. Hier wird
     * Ref versteckt, damit eine normale Liste von GroupMemberRights zurück gegeben
     * werden kann.
     *
     * @return Liste von GroupMemberRights
     */


    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }


    public ArrayList<BoardEntry> getBlackBoard() {
        if(this.blackBoard == null){
            return null;
        }
        ArrayList<BoardEntry> blackBoard = new ArrayList<BoardEntry>();
        for(Ref<BoardEntry> tmp : this.blackBoard){
            blackBoard.add(tmp.get());
        }
        return blackBoard;
    }

    public void setBlackBoard(ArrayList<BoardEntry> blackBoard) {
        ArrayList<Ref<BoardEntry>> blackBoardRef = new ArrayList<>();
        for(BoardEntry tmp : blackBoard){
            blackBoardRef.add(Ref.create(tmp));
        }
        this.blackBoard = blackBoardRef;
    }


    public ArrayList<BoardEntry> getNews() {
        if(this.news == null){
            return null;
        }
        ArrayList<BoardEntry> news = new ArrayList<BoardEntry>();
        for(Ref<BoardEntry> tmp : this.news){
            news.add(tmp.get());
        }
        return news;
    }

    public void setNews(List<BoardEntry> news) {
        ArrayList<Ref<BoardEntry>> newsRef = new ArrayList<>();
        for(BoardEntry tmp : news){
            newsRef.add(Ref.create(tmp));
        }
        this.news = newsRef;
    }

    public Picture getPicture() {
        return picture;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
    }

    public GroupMetaData getMetaData() {
        return metaData.get();
    }

    public void setMetaData(GroupMetaData metaData) {
        this.metaData = Ref.create(metaData);
    }

    /**
     * Eine embedded Klasse zum speichern von Rechten zu jedem Mitglied der
     * Nutzergrppe. Die Klasse hat zwei Felder, eins mit dem Namen des Mitglieds
     * und das andere mit einer Liste von Rechten, die das Mitglied hat.
     */
    private static class GroupMemberRights{
        String member;
        ArrayList<String> rights;

        public GroupMemberRights(){
        }

        public String getMember() {
            return member;
        }

        public void setMember(String member) {
            this.member = member;
        }

        public ArrayList<String> getRights() {
            return rights;
        }

        public void setRights(ArrayList<String> rights) {
            this.rights = rights;
        }
    }

    public void addGroupMember(String member, ArrayList<String> rights){
        GroupMemberRights gmr = new GroupMemberRights();
        gmr.setMember(member);
        gmr.setRights(rights);
        memberRanks.add(gmr);
    }

    public void removeGroupMember(String member){
        for (GroupMemberRights gmr : getMemberRanks()){
            if(gmr.getMember().equals(member)){
                getMemberRanks().remove(gmr);
            }
        }
    }

    public List<String> getMembers(){
        ArrayList<String> memberList = new ArrayList<>();
        for(GroupMemberRights gmr : getMemberRanks()){
            memberList.add(gmr.getMember());
        }
        return memberList;
    }

    public void addBlackBoardMessage(BoardEntry be){
        if(be != null){
            ArrayList<BoardEntry> blackBoard = getBlackBoard();
            blackBoard.add(be);
            setBlackBoard(blackBoard);
        }
    }

    public void removeBoardMessage(BoardEntry be){
        if(be != null){
            getBlackBoard().remove(be);
        }
    }

    public void addNewsMessage(BoardEntry be){
        if(be != null){
            getNews().add(be);
        }
    }
}
