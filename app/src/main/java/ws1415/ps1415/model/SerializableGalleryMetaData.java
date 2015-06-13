package ws1415.ps1415.model;

import com.skatenight.skatenightAPI.model.GalleryMetaData;

import java.io.Serializable;

/**
 * Definiert die gleichen Attribute wie die Klasse GalleryMetaData, ist jedoch serialisierbar. Dies ist
 * notwendig, um in einigen Activities GalleryMetaData-Objekte in einem Bundle zu speichern.
 * @author Richard Schulze
 */
public class SerializableGalleryMetaData implements Serializable {
    private String containerClass;
    private Long containerId;
    private Long id;
    private String title;

    public SerializableGalleryMetaData(GalleryMetaData metaData) {
        containerClass = metaData.getContainerClass();
        containerId = metaData.getContainerId();
        id = metaData.getId();
        title = metaData.getTitle();
    }

    public GalleryMetaData getGalleryMetaData() {
        return new GalleryMetaData()
                .setContainerClass(containerClass)
                .setContainerId(containerId)
                .setId(id)
                .setTitle(title);
    }
}
