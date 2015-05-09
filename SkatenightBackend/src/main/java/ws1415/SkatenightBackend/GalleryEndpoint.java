package ws1415.SkatenightBackend;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

/**
 * Stellt Methoden zur Verwaltung von Gallerien zur Verfügung.
 * @author Richard Schulze
 */
public class GalleryEndpoint extends SkatenightServerEndpoint {
    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();


    /**
     * Gibt eine Upload-URL für einen Blobstore-Upload zurück.
     * @return Eine URL die für einen Upload in den Blobstore genutzt werden kann.
     */
    public UploadUrl getUploadUrl() {
        UploadUrl url = new UploadUrl();
        url.setUrl(blobstoreService.createUploadUrl("/images/upload"));
        return url;
    }


    public class UploadUrl {
        String url;

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

}
