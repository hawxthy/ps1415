package ws1415.SkatenightBackend.transport;

import com.googlecode.objectify.annotation.Index;

import java.util.Date;

import ws1415.SkatenightBackend.model.Comment;

/**
 * Enthält die vollständigen Informationen des Kommentars, die für den aufrufenden Benutzer sichtbar sind.
 * @author Richard Schulze
 */
public class CommentData {
    private Long id;
    private String author;
    private Date date;
    private String comment;

    public CommentData(Comment comment) {
        id = comment.getId();
        author = comment.getAuthor();
        date = comment.getDate();
        this.comment = comment.getComment();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
