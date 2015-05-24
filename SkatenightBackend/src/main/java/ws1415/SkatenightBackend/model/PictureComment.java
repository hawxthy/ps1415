package ws1415.SkatenightBackend.model;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Repräsentiert einen Kommentar eines Benutzers zu einem Bild.
 * @author Richard Schulze
 */
@PersistenceCapable
public class PictureComment {
    @Persistent
    private String comment;
    @Persistent
    private Date date;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
