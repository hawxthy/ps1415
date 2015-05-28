package ws1415.SkatenightBackend.transport;

import ws1415.SkatenightBackend.model.Gallery;

/**
 * Repräsentiert die Metadaten einer Gallery. Dazu gehören insbesondere nicht die Bilder einer Gallery.
 * @author Richard Schulze
 */
public class GalleryMetaData {
    private Long id;
    private String title;

    public GalleryMetaData(Gallery gallery) {
        id = gallery.getId();
        title = gallery.getTitle();
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
}
