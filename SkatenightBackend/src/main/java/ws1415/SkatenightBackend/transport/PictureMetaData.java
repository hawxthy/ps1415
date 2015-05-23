package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;

import java.util.Date;

import ws1415.SkatenightBackend.model.Picture;

/**
 * Repräsentiert die Metadaten eines Bildes.
 * @author Richard Schulze
 */
public class PictureMetaData {
    private Long id;
    private String title;
    private Date date;
    private BlobKey imageBlobKey;

    public PictureMetaData(Picture picture) {
        this.id = picture.getId();
        this.title = picture.getTitle();
        this.date = picture.getDate();
        this.imageBlobKey = picture.getImageBlobKey();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BlobKey getImageBlobKey() {
        return imageBlobKey;
    }

    public void setImageBlobKey(BlobKey imageBlobKey) {
        this.imageBlobKey = imageBlobKey;
    }
}
