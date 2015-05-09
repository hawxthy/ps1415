package ws1415.SkatenightBackend.model;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;

import java.util.List;
import java.util.Map;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Speichert die kompletten Bilddaten inklusive Metadaten, Kommentaren und Bewertungen.
 * @author Richard Schulze
 */
@Entity
public class Picture {
    @Id
    private Long id;
    private Text description;
    private Map<String, Integer> ratings;
    private Double avgRating;
    private BlobKey imageBlobKey;
    private PictureMetaData metaData;
    private List<PictureComment> pictureComments;

    /**
     * Speichert die Upload-URL für den Upload in den Blobstore. Dient nur der Übertragung an die App
     * und wird aus diesem Grund nicht persistiert.
     */
    @Ignore
    private String uploadUrl;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Text getDescription() {
        return description;
    }

    public void setDescription(Text description) {
        this.description = description;
    }

    public Map<String, Integer> getRatings() {
        return ratings;
    }

    public void setRatings(Map<String, Integer> ratings) {
        this.ratings = ratings;
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

    public PictureMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(PictureMetaData metaData) {
        this.metaData = metaData;
    }

    public List<PictureComment> getPictureComments() {
        return pictureComments;
    }

    public void setPictureComments(List<PictureComment> pictureComments) {
        this.pictureComments = pictureComments;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
}
