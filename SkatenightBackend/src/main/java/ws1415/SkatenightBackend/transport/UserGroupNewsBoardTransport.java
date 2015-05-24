package ws1415.SkatenightBackend.transport;

import java.util.ArrayList;

import ws1415.SkatenightBackend.model.UserGroup.BoardEntry;

/**
 * Created by Bernd on 19.05.2015.
 */
public class UserGroupNewsBoardTransport {
    String groupName;
    ArrayList<BoardEntry> boardEntries;

    public UserGroupNewsBoardTransport(String groupName, ArrayList<BoardEntry> boardEntries){
        this.groupName = groupName;
        this.boardEntries = boardEntries;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<BoardEntry> getBoardEntries() {
        if(boardEntries != null){
            return boardEntries;
        }
        return null;
    }

    public void setBoardEntries(ArrayList<BoardEntry> boardEntries) {
        this.boardEntries = boardEntries;
    }
}

