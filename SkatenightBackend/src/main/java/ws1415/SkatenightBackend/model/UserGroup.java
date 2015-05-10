package ws1415.SkatenightBackend.model;

import com.google.appengine.datanucleus.annotations.Unowned;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Repräsentiert eine Benutzergruppe.
 * @author Richard
 */
@PersistenceCapable
public class UserGroup {
    @PrimaryKey
    @Persistent
    private String name;            // Eindeutiger Name der Gruppe

    @Persistent
    @Unowned
    private boolean open;

    @Persistent
    @Unowned
    private GroupType type;

    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private EndUser creator;

    @Persistent
    @Unowned
    private Picture picture;

    // Eine Map die die E-Mail von einem Benutzer(vorne)
    // mit dem Namen eines Rangs(hinten) verbindet.
    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private Map<String, String> memberRanks = new HashMap<String, String>();

    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private List<Rank> ranking;

    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private List<BoardEntry> news;

    @Persistent
    @Unowned
    private List<BoardEntry> blackBoard;


    public UserGroup() {
        // Konstruktor für GAE
    }

    public UserGroup(EndUser creator) {
        if (creator == null) {
            throw new IllegalArgumentException("creator can not be null");
        }
        this.creator = creator;
        memberRanks = new HashMap<String, String>();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public EndUser getCreator() {
        return creator;
    }


    public Map<String, String> getMemberRanks() {

        return memberRanks;
    }

    public void setMemberRanks(Map<String, String> memberRanks) {
        this.memberRanks = memberRanks;
    }


    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }


    public GroupType getType() {
        return type;
    }

    public void setType(GroupType type) {
        this.type = type;
    }


    public List<BoardEntry> getBlackBoard() {
        return blackBoard;
    }

    public void setBlackBoard(List<BoardEntry> blackBoard) {
        this.blackBoard = blackBoard;
    }


    public List<BoardEntry> getNews() {
        return news;
    }

    public void setNews(List<BoardEntry> news) {
        this.news = news;
    }


    public List<Rank> getRanking() {
        return ranking;
    }

    public void setRanking(List<Rank> ranking) {
        this.ranking = ranking;
    }


    public Picture getPicture() {
        return picture;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
    }
}
