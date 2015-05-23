package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.model.GalleryContainer;
import ws1415.SkatenightBackend.model.Picture;
import ws1415.SkatenightBackend.transport.GalleryViewOptions;
import ws1415.SkatenightBackend.transport.PictureMetaData;
import ws1415.SkatenightBackend.transport.PictureMetaDataList;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Stellt Methoden zur Verwaltung von Bildern und Alben zur Verfügung.
 * @author Richard Schulze
 */
public class GalleryEndpoint extends SkatenightServerEndpoint {
    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Erstellt eine Gallery und fügt sie dem Container hinzu, der im Gallery-Objekt angegeben ist.
     * @param user           Der Benutzer, der die Gallery erstellt.
     * @param gallery        Die zu erstellende Gallery.
     * @return Die erstellte Gallery mit gesetztem ID-Feld.
     */
    public Gallery createGallery(User user, Gallery gallery)
            throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }

        // Gallery prüfen
        if (gallery.getId() != null) {
            throw new IllegalArgumentException("gallery was already created (id is not null)");
        }
        if (gallery.getTitle() == null || gallery.getTitle().isEmpty()) {
            throw new IllegalArgumentException("title can not be empty");
        }

        // Container abrufen und Rechte prüfen
        GalleryContainer container = (GalleryContainer) ofy().load().kind(gallery.getContainerClass()).id(gallery.getContainerId()).safe();
        if (!container.canAddGallery(user)) {
            throw new OAuthRequestException("insufficient privileges");
        }

        // Gallery speichern und dem Container hinzufügen
        ofy().save().entity(gallery).now();
        container.addGallery(user, gallery);
        ofy().save().entity(container).now();

        return gallery;
    }

    /**
     * Editiert die angegebene Gallery.
     * @param user       Der aufrufende Benutzer.
     * @param gallery    Die zu editierende Gallery.
     * @return Die editierte Gallery.
     */
    public Gallery editGallery(User user, Gallery gallery) throws OAuthRequestException {
        // TODO: Problem, wenn das Gallery-Objekt geänderte Container-Daten enthält
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (gallery.getId() == null) {
            throw new IllegalArgumentException("gallery has to be created first");
        }
        GalleryContainer container = (GalleryContainer) ofy().load().kind(gallery.getContainerClass()).id(gallery.getContainerId()).safe();
        if (!container.canEditGallery(user)) {
            throw new OAuthRequestException("insufficient privileges");
        }
        if (gallery.getTitle() == null || gallery.getTitle().isEmpty()) {
            throw new IllegalArgumentException("title can not be empty");
        }

        ofy().save().entity(gallery).now();
        return gallery;
    }

    /**
     * Löscht die Gallery mit der angegebenen ID.
     * @param user         Der Benutzer, der die Gallery löschen möchte.
     * @param galleryId    Die ID der zu löschenden Gallery.
     */
    public void deleteGallery(User user, @Named("galleryId") long galleryId) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }

        Gallery gallery = ofy().load().type(Gallery.class).id(galleryId).safe();
        GalleryContainer container = (GalleryContainer) ofy().load().kind(gallery.getContainerClass()).id(gallery.getContainerId()).safe();
        if (!container.canRemoveGallery(user)) {
            throw new OAuthRequestException("insufficient privileges");
        }
        container.removeGallery(user, gallery);
        ofy().save().entity(container).now();

        ofy().delete().entity(gallery).now();
    }

    /**
     * Gibt eine Liste von Bild-Metadaten zurück, die anhand der übergebenen ViewOptions ausgewählt
     * werden.
     * Für diese Methode ist explizit angegeben, dass sie die HTTP-Methode POST verwendet, damit der
     * Parameter {@code viewOptions} übertragen werden kann.
     * @param user           Der aufrufende Benutzer.
     * @param viewOptions    Die anzuwendenden ViewOptions.
     * @return Eine Liste von Bild-Metadaten, die anhand der ViewOptions ausgewählt werden.
     */
    @ApiMethod(httpMethod = "POST")
    public PictureMetaDataList listPictures(User user, GalleryViewOptions viewOptions) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }

        Query<Picture> q = ofy().load().group(PictureMetaData.class).type(Picture.class).limit(viewOptions.getLimit());
        if (viewOptions.getCursorString() != null) {
            q = q.startAt(Cursor.fromWebSafeString(viewOptions.getCursorString()));
        }
        QueryResultIterator<Picture> iterator = q.iterator();

        PictureMetaDataList result = new PictureMetaDataList();
        result.setList(new LinkedList<PictureMetaData>());
        int count = 0;
        while (count < viewOptions.getLimit() && iterator.hasNext()) {
            result.getList().add(new PictureMetaData(iterator.next()));
            count++;
        }
        result.setCursorString(iterator.getCursor().toWebSafeString());

        return result;
    }

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
        // TODO Nur die durchschnittliche und die eigene Bewertung an die App übertragen
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
            // Die Anzahl der Bewertungen wird als float zwischengespeichert, damit beim Teilen keine
            // Rundungsfehler durch das automatische Casten entstehen
            float count = (float) ratings.size();
            picture.setAvgRating((picture.getAvgRating() - ratings.get(user) / count) * count / (count - 1));
        }
        ratings.put(user.getEmail(), rating);
        // Die Anzahl der Bewertungen wird als float zwischengespeichert, damit beim Teilen keine
        // Rundungsfehler durch das automatische Casten entstehen
        float count = (float) ratings.size();
        picture.setAvgRating(picture.getAvgRating() * (count - 1) / count + rating / count);
        ofy().save().entity(picture).now();
    }

}
