package ws1415.SkatenightBackend.model;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;

/**
 * Created by Bernd Eissing on 26.05.2015.
 */
@Entity
public class Board {
    @Id
    String groupName;
    ArrayList<Ref<BoardEntry>> entries;

    public Board(){
        // Konstruktor für GAE
    }

    public Board(String groupName, BoardEntry be){
        this.groupName = groupName;
        addBoardMessage(be);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<BoardEntry> getBoardEntries() {
        if (entries != null) {
            ArrayList<BoardEntry> boardEntries = new ArrayList<>();
            for (Ref<BoardEntry> ref : entries) {
                boardEntries.add(ref.get());
            }
            return boardEntries;
        }
        return null;
    }

    public void setBoardEntries(ArrayList<BoardEntry> entries) {
        ArrayList<Ref<BoardEntry>> boardEntries = new ArrayList<>();
        if(entries != null){
            for(BoardEntry be : entries){
                boardEntries.add(Ref.create(be));
            }
        }
        this.entries = boardEntries;
    }

    public void addBoardMessage(BoardEntry be) {
        if (entries == null) {
            entries = new ArrayList<Ref<BoardEntry>>();
        }
        entries.add(Ref.create(be));
    }

    public void removeBoardMessage(Long boardEntryId) {
        if (entries != null) {
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).get().getId().longValue() == boardEntryId.longValue()) {
                    entries.remove(entries.get(i));
                }
            }
        }
    }
}
