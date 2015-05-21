package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ws1415.SkatenightBackend.model.Picture;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Stellt Methoden zur Verwaltung von Bildern und Alben zur Verfügung.
 * @author Richard Schulze
 */
public class GalleryEndpoint extends SkatenightServerEndpoint {
    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Erstellt ein Picture-Objekt, das zunächst ohne BlobKey zurück gegeben wird. Das Picture-Objekt enthält
     * eine Upload-URL, über die das Bild anschließend hochgeladen werden kann. Der BlobstoreUploadHandler
     * sorgt dafür, dass der BlobKey in dem Picture-Objekt gesetzt wird, sobald das Bild im Blobstore
     * gespeichert wurde. Er erstellt ebenfalls ein Thumbnail für das Bild.
     * @param user           Der Benutzer, der das Bild erstellt.
     * @param title          Der Titel des Bildes.
     * @param description    Die Beschreibung des Bildes.
     * @return Das auf dem Server erstellte Picture-Objekt.
     */
    public Picture createPicture(User user, @Named("title") String title, @Named("description") String description) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (title == null || title.isEmpty() || description == null || description.isEmpty()) {
            throw new IllegalArgumentException("invalid picture parameters");
        }

        Picture picture = new Picture();
        picture.setTitle(title);
        picture.setDate(new Date());
        picture.setUploader(user.getEmail());
        picture.setDescription(new Text(description));
        picture.setUploadUrl(blobstoreService.createUploadUrl("/images/upload"));
        ofy().save().entity(picture).now();

        return picture;
    }

    /**
     * Gibt das Picture-Objekt mit der angegebenen ID zurück.
     * @param id Die ID des abzurufenden Picture-Objekts.
     * @return Das Picture-Objekt mit der angegebenen ID.
     */
    public Picture getPicture(@Named("id") long id) {
        // TODO Eventuell mit User-Parameter sichern
        return ofy().load().type(Picture.class).id(id).safe();
    }

    /**
     * Löscht das Bild mit der angegebenen ID auf dem Server. Der aufrufende Benutzer muss das Bild
     * hochgeladen haben oder ein Admin sein.
     * @param user    Der aufrufende Benutzer.
     * @param id      Die ID des zu löschenden Bildes.
     */
    public void deletePicture(User user, @Named("id") long id) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Picture picture = ofy().load().type(Picture.class).id(id).now();
        if (picture != null) {
            if (!picture.getUploader().equals(user.getEmail()) && !new RoleEndpoint().isAdmin(user.getEmail()).value) {
                throw new OAuthRequestException("user is not the uploader of the picture and not an admin");
            }

            // TODO Bild aus allen Alben löschen, in denen es enthalten ist
            // Blob aus dem Blobstore löschen
            if (picture.getImageBlobKey() != null) {
                blobstoreService.delete(picture.getImageBlobKey());
            }
            ofy().delete().entity(picture).now();
        }
    }

    /**
     * Lässt den angegebenen Benutzer das Bild mit der in {@code rating} übergebenen Anzahl Sterne
     * bewerten. Dabei wird ebenfalls die durchschnittliche Bewertung des Bildes angepasst.
     * @param user         Der bewertende Benutzer.
     * @param pictureId    Die ID des zu bewertenden Bildes.
     * @param rating       Die Bewertung in Sternen.
     */
    public void ratePicture(User user, @Named("pictureId") long pictureId, @Named("rating") int rating) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("rating has to be in [0;5]");
        }

        Picture picture = ofy().load().type(Picture.class).id(pictureId).safe();
        Map<String, Integer> ratings = picture.getRatings();
        if (ratings == null) {
            ratings = new HashMap<>();
            picture.setRatings(ratings);
            picture.setAvgRating(0.0);
        }
        if (ratings.get(user) != null) {
            // Falls alte Bewertung existiert, dann zunächst von der durchschnittlichen Bewertung abziehen
            picture.setAvgRating((picture.getAvgRating() - ratings.get(user) / ratings.size()) * ratings.size() / (ratings.size() - 1));
        }
        ratings.put(user.getEmail(), rating);
        picture.setAvgRating(picture.getAvgRating() * (ratings.size() - 1) / ratings.size() + rating / ratings.size());
        ofy().save().entity(picture).now();
    }

}
