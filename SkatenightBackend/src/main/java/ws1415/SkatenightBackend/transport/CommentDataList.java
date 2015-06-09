package ws1415.SkatenightBackend.transport;

import java.util.List;

import ws1415.SkatenightBackend.model.Comment;

/**
 * Erweitert eine Liste um den Cursorstring, der durch das Abrufen der Liste aus dem Datastore entstanden ist.
 * @author Richard Schulze
 */
public class CommentDataList {
    private List<CommentData> comments;
    private String cursorString;

    public List<CommentData> getComments() {
        return comments;
    }

    public void setComments(List<CommentData> comments) {
        this.comments = comments;
    }

    public String getCursorString() {
        return cursorString;
    }

    public void setCursorString(String cursorString) {
        this.cursorString = cursorString;
    }
}
