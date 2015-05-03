package ws1415.SkatenightBackend.model;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Speichert die Metadaten zu einem Bild.
 * @author Richard Schulze
 */
@PersistenceCapable
public class PictureMetaData {
    @Persistent
    private String title;
    @Persistent
    private Date date;
    @Persistent
    private String thumbnail;

    @Persistent
    private Gallery gallery;
    @Persistent
    private Member uploader;

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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Gallery getGallery() {
        return gallery;
    }

    public void setGallery(Gallery gallery) {
        this.gallery = gallery;
    }

    public Member getUploader() {
        return uploader;
    }

    public void setUploader(Member uploader) {
        this.uploader = uploader;
    }
}
