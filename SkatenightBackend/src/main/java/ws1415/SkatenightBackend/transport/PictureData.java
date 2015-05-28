package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.model.Picture;
import ws1415.SkatenightBackend.model.PictureVisibility;

/**
 * Repräsentiert die vollständigen Daten eines Bildes, die der abrufende Benutzer sehen darf.
 * @author Richard Schulze
 */
public class PictureData {
    private Long id;
    private String title;
    private Date date;
    private String uploader;
    private String description;
    private Integer myRating;
    private Double avgRating;
    private BlobKey imageBlobKey;
    private PictureVisibility visibility;
    private List<Gallery> galleries;

    public PictureData(User user, Picture picture) {
        id = picture.getId();
        title = picture.getTitle();
        date = picture.getDate();
        uploader = picture.getUploader();
        description = picture.getDescription().getValue();
        if (picture.getRatings() != null) {
            myRating = picture.getRatings().get(user.getEmail());
        }
        avgRating = picture.getAvgRating();
        imageBlobKey = picture.getImageBlobKey();
        this.visibility = picture.getVisibility();
        galleries = picture.getGalleries();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMyRating() {
        return myRating;
    }

    public void setMyRating(Integer myRating) {
        this.myRating = myRating;
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

    public List<Gallery> getGalleries() {
        return galleries;
    }

    public void setGalleries(List<Gallery> galleries) {
        this.galleries = galleries;
    }
}
