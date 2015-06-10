package ws1415.SkatenightBackend.transport;

import java.util.List;
import java.util.Map;

import ws1415.SkatenightBackend.model.Gallery;

/**
 * Transportklasse, die eine Map von GalleryMetaData-Objekten und dazugehörige Zusatztitel speichert.
 * @author Richard Schulze
 */
public class ExpandableGalleriesWrapper {
    private List<GalleryMetaData> galleries;
    private List<String> additionalTitles;


    public ExpandableGalleriesWrapper(List<GalleryMetaData> galleries, List<String> additionalTitles) {
        this.galleries = galleries;
        this.additionalTitles = additionalTitles;
    }

    public List<GalleryMetaData> getGalleries() {
        return galleries;
    }

    public void setGalleries(List<GalleryMetaData> galleries) {
        this.galleries = galleries;
    }

    public List<String> getAdditionalTitles() {
        return additionalTitles;
    }

    public void setAdditionalTitles(List<String> additionalTitles) {
        this.additionalTitles = additionalTitles;
    }
}
