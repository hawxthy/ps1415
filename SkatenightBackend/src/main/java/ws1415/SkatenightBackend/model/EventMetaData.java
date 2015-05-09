package ws1415.SkatenightBackend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Date;

/**
 * Speichert Metadaten zu einem Event, die im Umfang begrenzt und daher geeignet sind, wenn
 * beispielsweise nur eine Liste von Events verarbeitet werden soll, ohne alle Informationen des
 * Events herunter zu laden.
 * @author Richard Schulze
 */
@Entity
public class EventMetaData {
    @Id
    private Long id;
    private String icon;
    private String title;
    private Date date;
    private Long eventId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
