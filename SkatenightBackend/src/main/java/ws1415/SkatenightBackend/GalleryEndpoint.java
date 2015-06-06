package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;

import java.awt.Container;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.model.GalleryContainer;
import ws1415.SkatenightBackend.model.Picture;
import ws1415.SkatenightBackend.model.PictureVisibility;
import ws1415.SkatenightBackend.model.UserGalleryContainer;
import ws1415.SkatenightBackend.transport.GalleryMetaData;
import ws1415.SkatenightBackend.transport.PictureData;
import ws1415.SkatenightBackend.transport.PictureFilter;
import ws1415.SkatenightBackend.transport.PictureMetaData;
import ws1415.SkatenightBackend.transport.PictureMetaDataList;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Stellt Methoden zur Verwaltung von Bildern und Alben zur Verfügung.
 * @author Richard Schulze
 */
public class GalleryEndpoint extends SkatenightServerEndpoint {
    private Logger log = Logger.getLogger(GalleryEndpoint.class.getName());

    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Ruft die Metadaten der Gallery mit der angegebenen ID ab.
     * @param user         Der Benutzer, der die Gallery abrufen möchte.
     * @param galleryId    Die ID der abzurufenden Gallery.
     * @return Die Gallery mit der angegebenen ID.
     */
    public GalleryMetaData getGalleryMetaData(User user, @Named("galleryId") long galleryId) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        // TODO Rechte des Benutzers prüfen
        return new GalleryMetaData(ofy().load().type(Gallery.class).id(galleryId).safe());
    }

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
        if (gallery.getContainerClass() == null || gallery.getContainerClass().isEmpty()) {
            throw new IllegalArgumentException("container class can not be empty");
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
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (gallery.getId() == null) {
            throw new IllegalArgumentException("gallery has to be created first");
        }

        // Sicherstellen, dass die Container-Daten nicht geändert werden
        Gallery oldGallery = ofy().load().type(Gallery.class).id(gallery.getId()).safe();
        if (!oldGallery.getContainerClass().equals(gallery.getContainerClass())) {
            throw new IllegalArgumentException("container class can not be changed");
        }
        if (oldGallery.getContainerId() != gallery.getContainerId()) {
            throw new IllegalArgumentException("container id can not be changed");
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

        // Die Gallery aus allen Bildern entfernen, die die Gallery referenzieren
        for (Picture p : gallery.getPictures()) {
            p.removeGallery(gallery);
        }
        ofy().save().entities(gallery.getPictures()).now();

        ofy().delete().entity(gallery).now();
    }

    /**
     * Ruft den UserGalleryContainer für den Benutzer mit der in {@code mail} angegebenen E-Mail-Adresse
     * ab. Falls kein Container existiert, so wird ein leerer Container angelegt.
     * @param user    Der aufrufende Benutzer.
     * @param mail    Die E-Mail-Adresse des Benutzers, für den der Container abgerufen wird.
     * @return Der GalleryContainer für den angegebenen Benutzer.
     */
    public UserGalleryContainer getGalleryContainerForMail(User user, @Named("mail") String mail) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        // TODO R: ggf. prüfen, ob der aufrufende Benutzer die Gallerien abrufen darf
        List<UserGalleryContainer> container = ofy().load().type(UserGalleryContainer.class).filter("user", mail).list();
        if (container == null || container.isEmpty()) {
            UserGalleryContainer createdContainer = new UserGalleryContainer();
            createdContainer.setUser(mail);
            ofy().save().entity(createdContainer).now();
            return createdContainer;
        } else {
            return container.get(0);
        }
    }

    /**
     * Gibt eine Liste von Bild-Metadaten zurück, die anhand der übergebenen ViewOptions ausgewählt
     * werden.
     * Für diese Methode ist explizit angegeben, dass sie die HTTP-Methode POST verwendet, damit der
     * Parameter {@code filter} übertragen werden kann.
     * @param user           Der aufrufende Benutzer.
     * @param filter    Die anzuwendenden ViewOptions. Falls sowohl eine Gallery- als auch eine
     *                       User-ID angegeben ist, so wird die User-ID ignoriert.
     * @return Eine Liste von Bild-Metadaten, die anhand der ViewOptions ausgewählt werden.
     */
    @ApiMethod(httpMethod = "POST")
    public PictureMetaDataList listPictures(User user, PictureFilter filter) throws OAuthRequestException, UnauthorizedException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (filter == null) {
            throw new NullPointerException("no filter submitted");
        }
        if (filter.getLimit() <= 0 || (filter.getGalleryId() == null && filter.getUserId() == null)) {
            throw new IllegalArgumentException("invalid filter: limit has to be positive and either galleryId or userId has to be non-null");
        }

        Query<Picture> q = ofy().load().group(PictureMetaData.class).type(Picture.class).limit(filter.getLimit());
        if (filter.getGalleryId() != null) {
            // Nach Gallery filtern
            q = q.filter("galleries", Ref.create(Key.create(Gallery.class, filter.getGalleryId())));
        } else {
            // Nach aufrufendem Benutzer als Uploader filtern
            q = q.filter("uploader", filter.getUserId());
        }
        q = q.order("-date");
        if (filter.getCursorString() != null) {
            q = q.startAt(Cursor.fromWebSafeString(filter.getCursorString()));
        }
        QueryResultIterator<Picture> iterator = q.iterator();

        // Minimale Sichtbarkeitseinstellung bestimmen, bei der der aufrufende Benutzer ein Bild abrufen darf
        PictureVisibility minVisibility;
        if (filter.getGalleryId() != null) {
            Gallery gallery = ofy().load().type(Gallery.class).id(filter.getGalleryId()).safe();
            if (gallery.getContainerClass().equals("User")/* && id == ... */) {
                // TODO Fall implementieren: Es wird eine Benutzergallery abgerufen
                // Hier muss wieder geprüft werden, ob der Benutzer, dessen Gallery abgerufen wird,
                // ein Freund ist
                minVisibility = null;
            } else {
                // Es wird eine öffentliche Gallery (z.B. von einem Event) abgerufen
                // Da einer öffentlichen Gallery nur öffentliche Bilder hinzugefügt werden können,
                // muss hier nicht in gesonderter Form auf die Sichtabrkeit geachtet werden.
                minVisibility = PictureVisibility.PUBLIC;
            }
        } else {
            if (user.getEmail().equals(filter.getUserId())) {
                // Falls der Benutzer seine eigenen Bilder abruft
                minVisibility = PictureVisibility.PRIVATE;
            } else {
                // Sonst testen, ob der abrufende Benutzer ein Freund ist
                boolean friend = new UserEndpoint().isFriendWith(filter.getUserId(), user.getEmail());

                if (friend) {
                    minVisibility = PictureVisibility.FRIENDS;
                } else {
                    minVisibility = PictureVisibility.PUBLIC;
                }
            }
        }

        PictureMetaDataList result = new PictureMetaDataList();
        result.setList(new LinkedList<PictureMetaData>());
        int count = 0;
        Picture tmpPicture;
        PictureMetaData tmpMetaData;
        while (count < filter.getLimit() && iterator.hasNext()) {
            tmpPicture = iterator.next();
            if (tmpPicture.getVisibility().compareTo(minVisibility) >= 0) {
                tmpMetaData = new PictureMetaData(tmpPicture);
            } else {
                tmpMetaData = new PictureMetaData();
                tmpMetaData.setId(-1l);
            }
            result.getList().add(tmpMetaData);
            count++;
        }
        result.setCursorString(iterator.getCursor().toWebSafeString());

        return result;
    }

    /**
     * Erstellt ein Picture-Objekt, das zunächst ohne BlobKey zurück gegeben wird. Das Picture-Objekt enthält
     * eine Upload-URL, über die das Bild anschließend hochgeladen werden kann. Der BlobstoreHandler
     * sorgt dafür, dass der BlobKey in dem Picture-Objekt gesetzt wird, sobald das Bild im Blobstore
     * gespeichert wurde. Er erstellt ebenfalls ein Thumbnail für das Bild.
     * @param user           Der Benutzer, der das Bild erstellt.
     * @param title          Der Titel des Bildes.
     * @param description    Die Beschreibung des Bildes.
     * @param visibility     Die Sichtbarkeitseinstellung für das Bild.
     * @return Das auf dem Server erstellte Picture-Objekt.
     */
    public Picture createPicture(User user, @Named("title") String title, @Named("description") String description,
                                 @Named("visibility") PictureVisibility visibility) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (title == null || title.isEmpty() || description == null || description.isEmpty() || visibility == null) {
            throw new IllegalArgumentException("invalid picture parameters");
        }

        Picture picture = new Picture();
        picture.setTitle(title);
        picture.setDate(new Date());
        picture.setUploader(user.getEmail());
        picture.setDescription(new Text(description));
        picture.setVisibility(visibility);
        picture.setUploadUrl(blobstoreService.createUploadUrl("/images/upload"));
        ofy().save().entity(picture).now();

        return picture;
    }

    /**
     * Editiert die Eigenschaften eines Bildes. Der BlobKey, also die hinterlegte Bild-Datei kann nicht
     * nachträglich geändert werden.
     * @param user           Der aufrufende User.
     * @param pictureId      Die ID des zu ändernden Bildes.
     * @param title          Der neue Titel des Bildes.
     * @param description    Die neue Beschreibung des Bildes.
     * @param visibility     Die neue Sichtbarkeit des Bildes. Falls die Sichtbarkeit für andere Benutzer,
     *                       die das Bild in einer Gallery referenziert haben, entzogen wird, wird das Bild
     *                       aus der entsprechenden Gallery entfernt.
     */
    public void editPicture(User user, @Named("pictureId") long pictureId, @Named("title") String title,
                            @Named("description") String description, @Named("visibility") PictureVisibility visibility)
            throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Picture picture = ofy().load().group(PictureMetaData.class).type(Picture.class).id(pictureId).safe();
        if (!picture.getUploader().equals(user.getEmail()) && !new RoleEndpoint().isAdmin(user.getEmail()).value) {
            throw new OAuthRequestException("user has to be uploader of the picture or an admin");
        }
        picture.setTitle(title);
        picture.setDescription(new Text(description));
        picture.setVisibility(visibility);
        ofy().save().entity(picture).now();
    }

    /**
     * Gibt das Picture-Objekt mit der angegebenen ID zurück.
     * @param id Die ID des abzurufenden Picture-Objekts.
     * @return Das Picture-Objekt mit der angegebenen ID.
     */
    public PictureData getPicture(User user, @Named("id") long id) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }

        // TODO Sichtbarkeitseinstellungen implementieren
        return new PictureData(user, ofy().load().type(Picture.class).id(id).safe());
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

            // Das Bild aus allen Gallerien entfernen, in denen es referenziert wird
            for (Gallery gallery : picture.getGalleries()) {
                gallery.removePicture(picture);
            }
            ofy().save().entities(picture.getGalleries()).now();

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

        Picture picture = ofy().load().group(PictureMetaData.class).type(Picture.class).id(pictureId).safe();
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

    /**
     * Fügt das Bild mit der angegebenen ID zu der Gallery hinzu.
     * @param user         Der Benutzer, der das Bild hinzufügen möchte.
     * @param pictureId    Die ID des hinzuzufügenden Bildes.
     * @param galleryId    Die ID der Gallery, zu der das Bild hinzugefügt wird.
     */
    public void addPictureToGallery(User user, @Named("pictureId") Long pictureId, @Named("galleryId") Long galleryId) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        // TODO Sichtbarkeitseinstellungen von Bildern beachten
        Gallery gallery = ofy().load().type(Gallery.class).id(galleryId).safe();
        GalleryContainer container = (GalleryContainer) ofy().load().kind(gallery.getContainerClass()).id(gallery.getContainerId()).safe();
        Picture picture = ofy().load().type(Picture.class).id(pictureId).safe();
        if (!container.canAddPictures(user, picture)) {
            throw new OAuthRequestException("insufficient privileges");
        }
        gallery.addPicture(picture);
        picture.addGallery(gallery);
        ofy().save().entities(picture, gallery).now();
    }

    /**
     * Entfernt das Bild mit der angegebenen ID aus der Gallery.
     * @param user         Der Benuter, der das Bild entfernen möchte.
     * @param pictureId    Die ID des zu entfernenden Bildes.
     * @param galleryId    Die ID der Gallery, aus der das Bild entfernt wird.
     */
    public void removePictureFromGallery(User user, @Named("pictureId") long pictureId, @Named("galleryId") long galleryId) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        // TODO Sichtbarkeitseinstellungen von Bildern beachten
        Gallery gallery = ofy().load().type(Gallery.class).id(galleryId).safe();
        GalleryContainer container = (GalleryContainer) ofy().load().kind(gallery.getContainerClass()).id(gallery.getContainerId()).safe();
        Picture picture = ofy().load().type(Picture.class).id(pictureId).safe();
        if (!container.canRemovePictures(user, picture)) {
            throw new OAuthRequestException("insufficient privileges");
        }
        gallery.removePicture(picture);
        picture.removeGallery(gallery);
        ofy().save().entities(picture, gallery).now();
    }

    /**
     * Ändert die Sichtbarkeitseinstellung für das Bild mit der angegebenen ID.
     * @param user          Der Benutzer, der die Scihtbarkeitseinstellung ändert.
     * @param pictureId     Die ID des Bildes, dessen Sichtbarkeitseinstellung geändert wird.
     * @param visibility    Die neue Sichtbarkeitseinstellung für das Bild.
     */
    public void changeVisibility(User user, @Named("pictureId") long pictureId, @Named("visibility") PictureVisibility visibility) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (visibility == null) {
            throw new IllegalArgumentException("null is not a valid visibility");
        }
        Picture picture = ofy().load().type(Picture.class).id(pictureId).safe();
        // TODO Eventuell anders bestimmen, wer zum Ändern der Sichtbarkeit berechtigt ist
        if (!user.getEmail().equals(picture.getUploader())) {
            throw new OAuthRequestException("insufficient privileges");
        }
        picture.setVisibility(visibility);
        ofy().save().entity(picture).now();
    }

}
