package ws1415.SkatenightBackend.transport;

import ws1415.SkatenightBackend.model.Gallery;

/**
 * Repräsentiert die Metadaten einer Gallery. Dazu gehören insbesondere nicht die Bilder einer Gallery.
 * @author Richard Schulze
 */
public class GalleryMetaData {
    private Long id;
    private String title;
    private String containerClass;
    private Long containerId;

    public GalleryMetaData(Gallery gallery) {
        id = gallery.getId();
        title = gallery.getTitle();
        this.containerClass = gallery.getContainerClass();
        this.containerId = gallery.getContainerId();
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

    public String getContainerClass() {
        return containerClass;
    }

    public void setContainerClass(String containerClass) {
        this.containerClass = containerClass;
    }

    public Long getContainerId() {
        return containerId;
    }

    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }
}
