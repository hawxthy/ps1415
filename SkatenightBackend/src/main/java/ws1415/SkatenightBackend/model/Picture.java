package ws1415.SkatenightBackend.model;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnore;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ws1415.SkatenightBackend.transport.PictureMetaData;

/**
 * Speichert die kompletten Bilddaten inklusive Metadaten, Kommentaren und Bewertungen.
 * @author Richard Schulze
 */
@Entity
public class Picture implements BlobKeyContainer, CommentContainer {
    @Id
    private Long id;
    private String title;
    @Index
    private Date date;
    @Index
    private String uploader;
    private Text description;
    private Map<String, Integer> ratings;
    private Double avgRating;
    private BlobKey imageBlobKey;
    private PictureVisibility visibility;

    @Index
    @Load(unless = {PictureMetaData.class})
    @JsonIgnore
    private List<Ref<Gallery>> galleries = new LinkedList<>();
    @Load(unless = {PictureMetaData.class})
    private List<Ref<Comment>> comments = new LinkedList<>();

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

    public PictureVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(PictureVisibility visibility) {
        this.visibility = visibility;
    }

    public List<Gallery> getGalleries() {
        if (galleries == null) {
            return null;
        } else {
            List<Gallery> galleries = new LinkedList<>();
            for (Ref<Gallery> ref : this.galleries) {
                if (ref.get() != null) {
                    galleries.add(ref.get());
                }
            }
            return galleries;
        }
    }

    public void setGalleries(List<Gallery> galleries) {
        if (galleries == null) {
            this.galleries = null;
        } else {
            this.galleries = new LinkedList<>();
            for (Gallery gallery : galleries) {
                this.galleries.add(Ref.create(gallery));
            }
        }
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    @Override
    public void consumeBlobKeys(List<BlobKey> keys) {
        if (keys != null && !keys.isEmpty()) {
            imageBlobKey = keys.get(0);
        }
    }

    public void addGallery(Gallery gallery) {
        if (gallery == null) {
            throw new NullPointerException("null, as a gallery, can not be added");
        }
        galleries.add(Ref.create(gallery));
    }

    public void removeGallery(Gallery gallery) {
        if (gallery == null) {
            throw new NullPointerException("null, as a gallery, can not be removed");
        }
        galleries.remove(Ref.create(gallery));
    }

    public List<Comment> getComments() {
        if (comments == null) {
            return null;
        } else {
            List<Comment> comments = new LinkedList<>();
            for (Ref<Comment> ref : this.comments) {
                Comment c = ref.get();
                if (c != null) {
                    comments.add(c);
                }
            }
            return comments;
        }
    }

    @Override
    public void addComment(Comment comment) {
        if (comment == null) {
            throw new NullPointerException("null, as a comment, can not be added");
        }
        comments.add(Ref.create(comment));
    }

    @Override
    public void removeComment(Comment comment) {
        if (comment == null) {
            throw new NullPointerException("null, as a comment, can not be removed");
        }
        comments.remove(Ref.create(comment));
    }

    @Override
    public boolean canAddComment(User user) {
        return true;
    }

    @Override
    public boolean canDeleteComment(User user) {
        return false;
    }
}
