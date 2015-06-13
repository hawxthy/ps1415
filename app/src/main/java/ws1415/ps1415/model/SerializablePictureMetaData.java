package ws1415.ps1415.model;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.PictureMetaData;

import java.io.Serializable;

/**
 * Serialisierbare Version der Klasse PictureMetaData.
 * @author Richard Schulze
 */
public class SerializablePictureMetaData implements Serializable {
    private Double avgRating;
    private DateTime date;
    private Long id;
    private String imageBlobKeyString;
    private String title;
    private String uploader;
    private String visibility;

    public SerializablePictureMetaData(PictureMetaData metaData) {
        avgRating = metaData.getAvgRating();
        date = metaData.getDate();
        id = metaData.getId();
        imageBlobKeyString = (metaData.getImageBlobKey() != null ? metaData.getImageBlobKey().getKeyString() : null);
        title = metaData.getTitle();
        uploader = metaData.getUploader();
        visibility = metaData.getVisibility();
    }

    public PictureMetaData getPictureMetaData() {
        return new PictureMetaData()
                .setAvgRating(avgRating)
                .setDate(date)
                .setId(id)
                .setImageBlobKey(imageBlobKeyString != null ? new BlobKey().setKeyString(imageBlobKeyString) : null)
                .setTitle(title)
                .setUploader(uploader)
                .setVisibility(visibility);
    }
}
