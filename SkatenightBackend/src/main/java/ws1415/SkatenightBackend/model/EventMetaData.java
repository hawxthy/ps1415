package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Key;

import java.util.Date;

import javax.jdo.annotations.Persistent;

/**
 * Speichert Metadaten zu einem Event, die im Umfang begrenzt und daher geeignet sind, wenn
 * beispielsweise nur eine Liste von Events verarbeitet werden soll, ohne alle Informationen des
 * Events herunter zu laden.
 * @author Richard Schulze
 */
public class EventMetaData {
    @Persistent
    private String icon;
    @Persistent
    private String title;
    @Persistent
    private Date date;
    @Persistent
    private Key eventKey;

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

    public Key getEventKey() {
        return eventKey;
    }

    public void setEventKey(Key eventKey) {
        this.eventKey = eventKey;
    }
}
