package ws1415.SkatenightBackend.model;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;

import ws1415.SkatenightBackend.model.BoardEntry;

/**
 * Created by Bernd on 18.05.2015.
 */
@Entity
public class UserGroupBlackBoard {
    @Id
    String groupName;
    ArrayList<Ref<BoardEntry>> boardEntries;

    public UserGroupBlackBoard(){
        // Konstruktor für Objectify
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<BoardEntry> getBoardEntries() {
        if(boardEntries != null){
            ArrayList<BoardEntry> boardEntriesNormal = new ArrayList<>();
            for(Ref<BoardEntry> refEntry : boardEntries){
                boardEntriesNormal.add(refEntry.get());
            }
            return boardEntriesNormal;
        }else{
            return null;
        }
    }

    public void setBoardEntries(ArrayList<BoardEntry> boardEntriesNormal) {
        if(boardEntriesNormal != null){
            ArrayList<Ref<BoardEntry>> boardEntriesRef = new ArrayList<>();
            for(BoardEntry beNormal : boardEntriesNormal){
                boardEntriesRef.add(Ref.create(beNormal));
            }
            boardEntries = boardEntriesRef;
        }
    }

    public void addBoardEntry(BoardEntry be){
        if(boardEntries == null){
            boardEntries = new ArrayList<Ref<BoardEntry>>();
        }
        boardEntries.add(Ref.create(be));
    }

    public void removeBoardEntry(BoardEntry be){
        for(Ref<BoardEntry> beRef : boardEntries){
            if(beRef.get().getId().longValue() == be.getId().longValue()){
                boardEntries.remove(beRef);
            }
        }
    }
}
