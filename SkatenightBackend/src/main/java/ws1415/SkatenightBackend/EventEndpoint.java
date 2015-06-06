package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.EventParticipationVisibility;
import ws1415.SkatenightBackend.model.EventRole;
import ws1415.SkatenightBackend.model.Privilege;
import ws1415.SkatenightBackend.model.UserLocation;
import ws1415.SkatenightBackend.transport.EventData;
import ws1415.SkatenightBackend.transport.EventFilter;
import ws1415.SkatenightBackend.transport.EventMetaData;
import ws1415.SkatenightBackend.transport.EventMetaDataList;
import ws1415.SkatenightBackend.transport.EventParticipationData;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Stellt Methoden zur Verarbeitung von Events auf dem Backend bereit.
 * @author Richard Schulze
 */
public class EventEndpoint extends SkatenightServerEndpoint {
    private static final Logger log = Logger.getLogger(EventEndpoint.class.getName());

    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Weist dem Benutzer mit der angegebenen Email
     * @param user       Der aufrufende Benutzer.
     * @param eventId    Die ID des Events, in dem die Rolle zugewiesen werden soll.
     * @param endUser    Der Benutzer, dem die Rolle zugewiesen werden soll.
     * @param role       Die zuzuweisende Rolle.
     */
    public void assignRole(User user, @Named("eventId") long eventId, @Named("endUser") String endUser, @Named("role") EventRole role) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Event event = ofy().load().group(EventParticipationData.class).type(Event.class).id(eventId).safe();
        if (hasPrivilege(event, user.getEmail(), Privilege.ASSIGN_ROLE)) {
            event.getMemberList().put(endUser, role);
            if (!event.getMemberVisibility().containsKey(endUser)) {
                // Falls der Benutzer noch keine Sichtbarkeit für die Teilnahme definiert hat, dann PRIVATE vorgeben
                event.getMemberVisibility().put(endUser, EventParticipationVisibility.PRIVATE);
            }
            ofy().save().entity(event).now();
        } else {
            throw new OAuthRequestException("insufficient privileges");
        }
    }

    /**
     * Prüft ob der angegebene Benutzer in dem übergebenen Event das entsprechende Privileg besitzt.
     * @param event     Das Event, für das das Privileg geprüft werden soll.
     * @param endUser   Die Mail-Adresse des zu prüfenden Benutzers.
     * @param privilege Das zu prüfende Privileg.
     * @return true, wenn der Benutzer das Privileg im angegebenen Event besitzt, sonst false.
     */
    private boolean hasPrivilege(Event event, String endUser, Privilege privilege) {
        if (event == null || endUser == null || privilege == null
                || !event.getMemberList().containsKey(endUser)) {
            return false;
        }
        return event.getMemberList().get(endUser).hasPrivilege(privilege);
    }

    /**
     * Ruft die Metadaten von Events auf dem Server ab.
     * Für diese Methode ist explizit angegeben, dass sie die HTTP-Methode POST verwendet, damit der
     * Parameter {@code filter} übertragen werden kann.
     * @param user      Der aufrufende Benutzer.
     * @param filter    Der Filter, nach dem die Events abgerufen werden sollen. Falls keine User-Id
     *                  im Filter angegeben ist, werden alle Events abgerufen.
     * @return Eine Liste aller auf dem Server gespeicherter EventMetaData-Objekte, die die Kriterien
     * des Filters erfüllen.
     */
    @ApiMethod(httpMethod = "POST")
    public EventMetaDataList listEvents(User user, EventFilter filter) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (filter == null) {
            throw new IllegalArgumentException("no filter submitted");
        }
        if (filter.getLimit() <= 0) {
            throw new IllegalArgumentException("invalid filter parameters: limit has to be positive");
        }

        Query<Event> q = ofy().load().group(EventMetaData.class).type(Event.class).limit(filter.getLimit());
        if (filter.getCursorString() != null) {
            q = q.startAt(Cursor.fromWebSafeString(filter.getCursorString()));
        }
        if (filter.getUserId() != null) {
            // TODO Die Events abrufen, an denen ein User teilnimmt und prüfen, welche der aufrufende Benutzer davon sehen darf
        }
        q = q.order("-date");
        QueryResultIterator<Event> iterator = q.iterator();

        EventMetaDataList result = new EventMetaDataList();
        result.setList(new LinkedList<EventMetaData>());
        while (iterator.hasNext()) {
            result.getList().add(new EventMetaData(iterator.next()));
        }
        result.setCursorString(iterator.getCursor().toWebSafeString());

        return result;
    }

    /**
     * Ruft ein Event aus der Datenbank ab.
     * @param eventId    Die Id des abzurufenden Events.
     * @return Das Event-Objekt inklusive aller Daten.
     */
    public EventData getEvent(User user, @Named("eventId") long eventId) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        return new EventData(user, ofy().load().type(Event.class).id(eventId).safe());
    }

    /**
     * Erstellt ein neues Event mit den angegebenen Daten auf dem Server und gibt das persistierte
     * Event-Objekt zurück. Kann nur von Benutzern aufgerufen werden, die in der globalen Domäne die
     * Rolle Veranstalter haben.
     *
     * @param user     Der aufrufende Benutzer.
     * @param event    Das zu erstellende Event.
     * @return Das persistierte Event-Objekt.
     */
    public Event createEvent(User user, Event event) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!new UserEndpoint().existsUser(user.getEmail()).value) {
            throw new IllegalArgumentException("submitted user is not registered");
        }
        if (!new RoleEndpoint().isAdmin(user.getEmail()).value) {
            throw new OAuthRequestException("user is not an admin");
        }

        // Event prüfen
        if (event.getId() != null) {
            throw new IllegalArgumentException("event was already created (id is not empty)");
        }
        if (event.getDate() == null || event.getFee() < 0 || event.getMeetingPlace() == null
                || event.getMeetingPlace().isEmpty() || event.getTitle() == null
                || event.getTitle().isEmpty() || event.getRoute() == null) {
            throw new IllegalArgumentException("invalid event");
        }

        // Den aufrufenden Benutzer als Host eintragen
        if (event.getMemberList() == null) {
            event.setMemberList(new HashMap<String, EventRole>());
        }
        event.getMemberList().put(user.getEmail(), EventRole.HOST);
        event.getMemberVisibility().put(user.getEmail(), EventParticipationVisibility.PUBLIC);
        event.setImagesUploadUrl(blobstoreService.createUploadUrl("/images/upload"));
        ofy().save().entity(event).now();

        // TODO GCM-Nachricht an Geräte schicken, damit Event-Liste aktualisiert wird

        return event;
    }

    /**
     * Editiert das auf dem Server gepsicherte Event mit den Daten des übergebenen Events.
     * Der aufrufende Benutzer muss das Privileg EDIT_EVENT im Event haben.
     * @param user     Der aufrufende Benutzer.
     * @param event    Die neuen Daten des Events.
     */
    public Event editEvent(User user, Event event) throws OAuthRequestException {
        if (event == null) {
            throw new IllegalArgumentException("no event submitted");
        }
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }

        // TODO BlobKeys aktualisieren und alte Blobs löschen

        Event oldEvent = ofy().load().type(Event.class).id(event.getId()).safe();
        if (!hasPrivilege(oldEvent, user.getEmail(), Privilege.EDIT_EVENT)) {
            throw new OAuthRequestException("insufficient privileges");
        }
        ofy().save().entity(event).now();
        return event;
    }

    /**
     * Löscht das angegebene Event. Der aufrufende Benutzer muss die Rolle Veranstalter in der
     * globalen Domäne haben.
     * @param user Der aufrufende Benutzer.
     * @param id   Die ID des zu löschenden Events.
     */
    public void deleteEvent(User user, @Named("id") long id) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Event event = ofy().load().group(EventParticipationData.class).type(Event.class).id(id).now();
        if (event != null) {
            if (!hasPrivilege(event, user.getEmail(), Privilege.DELETE_EVENT)) {
                throw new OAuthRequestException("insufficient privileges");
            }
            if (event.getIcon() != null) {
                blobstoreService.delete(event.getIcon());
            }
            if (event.getImages() != null && !event.getImages().isEmpty()) {
                for (BlobKey key : event.getImages()) {
                    blobstoreService.delete(key);
                }
            }

            // TODO R: Performance testen
            UserEndpoint userEndpoint = new UserEndpoint();
            for (String participant : event.getMemberList().keySet()) {
                userEndpoint.removeEventFromUser(participant, event);
            }

            // Abhängige Gallerien löschen
            ofy().delete().entities(event.getGalleries()).now();
            ofy().delete().entity(event).now();
        }
    }

    /**
     * Fügt den aufrufenden Teilnehmer zum angegebenen Event hinzu.
     * @param user       Der aufrufende Benutzer, der dem Event hinzugefügt werden soll
     * @param eventId    Die ID des Events, dem beigetreten wird.
     * @param visibility Die Sichtbarkeit, die für die Teilnahme eingetragen wird.
     */
    public void joinEvent(User user, @Named("eventId") long eventId, @Named("visibility") EventParticipationVisibility visibility)
            throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Event event = ofy().load().group(EventParticipationData.class).type(Event.class).id(eventId).safe();
        if (!event.getMemberList().containsKey(user.getEmail())) {
            event.getMemberList().put(user.getEmail(), EventRole.PARTICIPANT);
            event.getMemberVisibility().put(user.getEmail(), visibility);
            ofy().save().entity(event).now();
            new UserEndpoint().addEventToUser(user.getEmail(), event);
        }
    }

    /**
     * Entfernt den aufrufenden Benutzer aus dem Event mit der angegebenen ID.
     * @param user       Der zu entfernende User.
     * @param eventId    Die ID des Events, das verlassen wird.
     * @throws IllegalStateException falls der letzte Host versucht aus dem Event auszutreten.
     */
    public void leaveEvent(User user, @Named("eventId") long eventId) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Event event = ofy().load().group(EventParticipationData.class).type(Event.class).id(eventId).safe();
        event.getMemberList().remove(user.getEmail());
        if (!event.getMemberList().containsValue(EventRole.HOST)) {
            throw new IllegalStateException("the last host of an event can not leave");
        }
        event.getMemberVisibility().remove(user.getEmail());
        new UserEndpoint().removeEventFromUser(user.getEmail(), event);
        ofy().save().entity(event).now();
    }

    /**
     * Ändert den Sichtbarkeitsstatus einer Teilnahme am betreffenden Event.
     * @param user
     * @param eventId
     * @param visibility
     */
    public void changeParticipationVisibility(User user, @Named("eventId") long eventId, @Named("visibility") EventParticipationVisibility visibility)
            throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Event event = ofy().load().group(EventParticipationData.class).type(Event.class).id(eventId).safe();
        if (!event.getMemberList().containsKey(user.getEmail())) {
            throw new IllegalArgumentException("user has to be participating to change visibility");
        }
        event.getMemberVisibility().put(user.getEmail(), visibility);
        ofy().save().entity(event).now();
    }










































    // ---------------------------------------------------------------------------------------------
    // |                                  Alte Endpoint-Methoden                                   |
    // ---------------------------------------------------------------------------------------------

    /**
     * Gibt eine Liste von Standortinformationen zu allen Membern, welche an dem übergenen Event teilnehmen.
     *
     * @param keyId
     * @return
     */
    public List<UserLocation> getMemberLocationsFromEvent(@Named("id") long keyId) {
        Event event = ofy().load().type(Event.class).id(keyId).safe();

        List<UserLocation> userLocations = new ArrayList<>();
        for (String email: event.getMemberList().keySet()) {
            userLocations.add(new UserEndpoint().getUserLocation(email));
        }
        return userLocations;
    }
}
