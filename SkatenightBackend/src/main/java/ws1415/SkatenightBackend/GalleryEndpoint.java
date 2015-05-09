package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

import java.util.Date;

import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.model.Picture;
import ws1415.SkatenightBackend.model.PictureMetaData;

/**
 * Stellt Methoden zur Verwaltung von Gallerien zur Verfügung.
 * @author Richard Schulze
 */
public class GalleryEndpoint extends SkatenightServerEndpoint {
    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();


    /**
     * Erstellt ein Picture-Objekt, das zunächst ohne BlobKey zurück gegeben wird. Das Picture-Objekt enthält
     * eine Upload-URL, über die das Bild anschließend hochgeladen werden kann. Der BlobstoreUploadHandler
     * sorgt dafür, dass der BlobKey in dem Picture-Objekt gesetzt wird, sobald das Bild im Blobstore
     * gespeichert wurde. Er erstellt ebenfalls ein Thumbnail für das Bild und hinterlegt den BlobKey
     * im PictureMetaData-Objekt.
     */
    public Picture createPicture(User user, Gallery gallery, @Named("title") String title, @Named("description") String description) {
        // TODO Parameter prüfen
        // TODO Uploader setzen

        PictureMetaData metaData = new PictureMetaData();
        metaData.setTitle(title);
        metaData.setDate(new Date());
        metaData.setGallery(gallery);

        Picture picture = new Picture();
        picture.setDescription(new Text(description));
        picture.setMetaData(metaData);
        picture.setUploadUrl(blobstoreService.createUploadUrl("/images/upload"));

        return picture;
    }

}
