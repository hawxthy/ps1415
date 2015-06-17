package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.Date;

import ws1415.SkatenightBackend.UserEndpoint;
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
    private String visibleUploader;
    private String description;
    private Integer myRating;
    private Double avgRating;
    private BlobKey imageBlobKey;
    private PictureVisibility visibility;

    public PictureData(User user, Picture picture) throws OAuthRequestException {
        id = picture.getId();
        title = picture.getTitle();
        date = picture.getDate();
        uploader = picture.getUploader();
        UserPrimaryData userData = new UserEndpoint().getPrimaryData(user, uploader);
        visibleUploader = userData.getFirstName() + (!userData.getLastName().isEmpty() ? " " : "") + userData.getLastName();
        description = picture.getDescription().getValue();
        if (picture.getRatings() != null) {
            myRating = picture.getRatings().get(user.getEmail());
        }
        avgRating = picture.getAvgRating();
        imageBlobKey = picture.getImageBlobKey();
        this.visibility = picture.getVisibility();
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

    public String getVisibleUploader() {
        return visibleUploader;
    }

    public void setVisibleUploader(String visibleUploader) {
        this.visibleUploader = visibleUploader;
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
}
