package ws1415.ps1415.model;

import com.skatenight.skatenightAPI.model.PictureFilter;

import java.io.Serializable;

/**
 * Serialisierbare Version des PictureFilters.
 * @author Richard Schulze
 */
public class SerializablePictureFilter implements Serializable {
    private String cursorString;
    private Long galleryId;
    private Integer limit;
    private String userId;

    public SerializablePictureFilter(PictureFilter filter) {
        cursorString = filter.getCursorString();
        galleryId = filter.getGalleryId();
        limit = filter.getLimit();
        userId = filter.getUserId();
    }

    public PictureFilter getPictureFilter() {
        return new PictureFilter()
                .setCursorString(cursorString)
                .setGalleryId(galleryId)
                .setLimit(limit)
                .setUserId(userId);
    }
}
