package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

import java.util.Date;
import java.util.logging.Logger;

import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.model.Picture;
import ws1415.SkatenightBackend.model.PictureMetaData;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Stellt Methoden zur Verwaltung von Gallerien zur Verfügung.
 * @author Richard Schulze
 */
public class GalleryEndpoint extends SkatenightServerEndpoint {
    private static final Logger log = Logger.getLogger(GalleryEndpoint.class.getName());

    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Gibt das Objekt für die globale Gallery zurück. Falls noch keine globale Gallery existiert,
     * wird diese angelegt.
     * @return Die globale Gallery.
     */
    public Gallery getGlobalGallery() {
        Gallery gallery = ofy().load().type(Gallery.class).id(Gallery.GLOBAL_GALLERY_ID).now();
        if (gallery == null) {
            gallery = new Gallery();
            gallery.setId(Gallery.GLOBAL_GALLERY_ID);
            ofy().save().entity(gallery).now();
        }
        return gallery;
    }

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
        ofy().save().entity(metaData).now();

        Picture picture = new Picture();
        picture.setDescription(new Text(description));
        picture.setMetaData(metaData);
        picture.setUploadUrl(blobstoreService.createUploadUrl("/images/upload"));
        ofy().save().entity(picture).now();

        metaData.setPictureId(picture.getId());
        ofy().save().entity(metaData).now();

        return picture;
    }

    /**
     * Gibt das Picture-Objekt mit der angegebenen ID zurück.
     * @param id Die ID des abzurufenden Picture-Objekts.
     * @return Das Picture-Objekt mit der angegebenen ID.
     */
    public Picture getPicture(@Named("id") long id) {
        return ofy().load().type(Picture.class).id(id).safe();
    }

}
