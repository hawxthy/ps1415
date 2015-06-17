package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;

import java.util.Date;

import ws1415.SkatenightBackend.model.Event;

/**
 * Transportklasse, die ausschließlich die Metadaten(ID, Icon, Titel und Datum) eines Events überträgt.
 * @author Richard Schulze
 */
public class EventMetaData {
    private Long id;
    private BlobKey icon;
    private String title;
    private Date date;

    public EventMetaData(Event event) {
        this.id = event.getId();
        this.icon = event.getIcon();
        this.title = event.getTitle();
        this.date = event.getDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BlobKey getIcon() {
        return icon;
    }

    public void setIcon(BlobKey icon) {
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
}
