package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;

import java.util.Date;

import ws1415.SkatenightBackend.model.Picture;
import ws1415.SkatenightBackend.model.PictureVisibility;

/**
 * Repräsentiert die Metadaten eines Bildes.
 * @author Richard Schulze
 */
public class PictureMetaData {
    private Long id;
    private String title;
    private Date date;
    private String uploader;
    private Double avgRating;
    private BlobKey imageBlobKey;
    private PictureVisibility visibility;

    public PictureMetaData() {

    }

    public PictureMetaData(Picture picture) {
        id = picture.getId();
        title = picture.getTitle();
        date = picture.getDate();
        uploader = picture.getUploader();
        avgRating = picture.getAvgRating();
        imageBlobKey = picture.getImageBlobKey();
        visibility = picture.getVisibility();
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

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public BlobKey getImageBlobKey() {
        return imageBlobKey;
    }

    public void setImageBlobKey(BlobKey imageBlobKey) {
        this.imageBlobKey = imageBlobKey;
    }

    public PictureVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(PictureVisibility visibility) {
        this.visibility = visibility;
    }
}
