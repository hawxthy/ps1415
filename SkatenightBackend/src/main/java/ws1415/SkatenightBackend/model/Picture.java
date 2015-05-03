package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Text;

import java.util.List;
import java.util.Map;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Speichert die kompletten Bilddaten inklusive Metadaten, Kommentaren und Bewertungen.
 * @author Richard Schulze
 */
@PersistenceCapable
public class Picture {
    @Persistent
    private Text description;
    @Persistent
    private Map<String, Integer> ratings;
    @Persistent
    private Double avgRating;
    @Persistent
    private String image;

    @Persistent
    private PictureMetaData metaData;
    @Persistent
    private List<PictureComment> pictureComments;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
}
