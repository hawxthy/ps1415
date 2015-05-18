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
public class UserGroupNewsBoard {
    @Id
    String groupName;
    ArrayList<Ref<BoardEntry>> newsEntries;

    public UserGroupNewsBoard(){
        // Konstruktor für Objectify
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<BoardEntry> getNewsEntries() {
        if(newsEntries != null){
            ArrayList<BoardEntry> newsEntriesNormal = new ArrayList<>();
            for(Ref<BoardEntry> refEntry : newsEntries){
                newsEntriesNormal.add(refEntry.get());
            }
            return newsEntriesNormal;
        }else{
            return null;
        }
    }

    public void setNewsEntries(ArrayList<BoardEntry> newsEntriesNormal) {
        if(newsEntriesNormal != null){
            ArrayList<Ref<BoardEntry>> newsEntriesRef = new ArrayList<>();
            for(BoardEntry beNormal : newsEntriesNormal){
                newsEntriesRef.add(Ref.create(beNormal));
            }
            newsEntries = newsEntriesRef;
        }
    }

    public void addNewsEntry(BoardEntry be){
        if(newsEntries == null){
            newsEntries = new ArrayList<Ref<BoardEntry>>();
        }
        newsEntries.add(Ref.create(be));
    }

    public void removeNewsEntry(BoardEntry be){
        for(Ref<BoardEntry> beRef : newsEntries){
            if(beRef.get().getId().longValue() == be.getId().longValue()){
                newsEntries.remove(beRef);
            }
        }
    }
}
