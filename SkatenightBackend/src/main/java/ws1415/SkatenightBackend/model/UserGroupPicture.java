package ws1415.SkatenightBackend.model;

import com.google.appengine.api.blobstore.BlobKey;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;

/**
 * Created by Bernd Eissing on 26.05.2015.
 */
@Entity
public class UserGroupPicture {
    @Id
    private Long id;
    @Ignore
    private String pictureUrl;
    private BlobKey pictureBlobKey;

    public Long getId() {
        return id;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public BlobKey getPictureBlobKey() {
        return pictureBlobKey;
    }

    public void setPictureBlobKey(BlobKey pictureBlobKey) {
        this.pictureBlobKey = pictureBlobKey;
    }
}
