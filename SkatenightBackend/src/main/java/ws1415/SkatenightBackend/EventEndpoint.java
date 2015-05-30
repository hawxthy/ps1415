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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.gcm.Message;
import ws1415.SkatenightBackend.gcm.MessageType;
import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.gcm.Sender;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.EventRole;
import ws1415.SkatenightBackend.model.Member;
import ws1415.SkatenightBackend.model.Privilege;
import ws1415.SkatenightBackend.model.Route;
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
        // TODO Methode ggf. mit User-Objekt sichern
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
    public EventData getEvent(@Named("eventId") long eventId) {
        return new EventData(ofy().load().type(Event.class).id(eventId).safe());
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

            // TODO Teilnehmer durchgehen und Event entfernen

            // Abhängige Gallerien löschen
            ofy().delete().entities(event.getGalleries()).now();
            ofy().delete().entity(event).now();
        }
    }

    /**
     * Fügt den aufrufenden Teilnehmer zum angegebenen Event hinzu.
     * @param user       Der aufrufende Benutzer, der dem Event hinzugefügt werden soll
     * @param eventId    Die ID des Events, dem beigetreten wird.
     */
    public void joinEvent(User user, @Named("eventId") long eventId) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Event event = ofy().load().group(EventParticipationData.class).type(Event.class).id(eventId).safe();
        if (!event.getMemberList().containsKey(user.getEmail())) {
            event.getMemberList().put(user.getEmail(), EventRole.PARTICIPANT);
            ofy().save().entity(event).now();
            // new UserEndpoint().getUserProfile(user, user.getEmail()).addEvent(event);
            // TODO Muss geändertes EndUser-Objekt gespeichert werden?
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
        //new UserEndpoint().getUserProfile(user, user.getEmail()).removeEvent(event);
        // TODO Muss geändertes EndUser-Objekt gespeichert werden?
        ofy().save().entity(event).now();
    }













































    // ---------------------------------------------------------------------------------------------
    // |                                  Alte Endpoint-Methoden                                   |
    // ---------------------------------------------------------------------------------------------

    /**
     * Fügt einen Member zu einem Event hinzu.
     *
     * @param keyId Die ID des Events
     * @param email die E-Mail des Teilnehmers
     */
    public void addMemberToEvent(@Named("id") long keyId, @Named("email") String email) {
        Event event = ofy().load().type(Event.class).id(keyId).safe();
        Set<String> memberKeys = event.getMemberList().keySet();
        if (!memberKeys.contains(email)) {
            event.getMemberList().put(email, EventRole.PARTICIPANT);

            updateEvent(event);

            if (event.isNotificationSend()) {
                // CurrentEventID setzen
                Member member = new UserEndpoint().getMember(email);
                if (member.getCurrentEventId() == null || member.getCurrentEventId() != keyId) {
                    member.setCurrentEventId(keyId);
                    member.setCurrentWaypoint(0);
                }

                PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
                pm.makePersistent(member);
                RegistrationManager registrationManager = getRegistrationManager(pm);

                Set<String> ids = new HashSet<>();
                ids.add(registrationManager.getUserIdByMail(email));

                Sender sender = new Sender(Constants.GCM_API_KEY);
                Message m = new Message.Builder()
                        .delayWhileIdle(false)
                        .timeToLive(3600)
                        .addData("type", MessageType.EVENT_START_MESSAGE.name())
                        .addData("eventId", Long.toString(keyId))
                        .build();
                try {
                    sender.send(m, new LinkedList<>(ids), 1);
                }
                catch (IOException e) {

                }
                if (pm != null) pm.close();
            }
        }
    }

    /**
     * Entfernt einen Member von einem Event.
     *
     * @param keyId Die ID des Events
     * @param email Die E-Mail des Members
     */
    public void removeMemberFromEvent(@Named("id") long keyId, @Named("email") String email) {
        Event event = ofy().load().type(Event.class).id(keyId).safe();

        Set<String> memberKeys = event.getMemberList().keySet();
        if (memberKeys.contains(email)) {
            event.getMemberList().remove(email);
            updateEvent(event);
        }
    }

    /**
     * Gibt eine Liste von allen Membern, welche an dem übergenen Event teilnehmen.
     *
     * @param keyId die Id von dem Event
     * @return List von Teilnehmern
     */
    public List<EndUser> getMembersFromEvent(@Named("id") long keyId) {
        Event event = ofy().load().type(Event.class).id(keyId).safe();

        List<EndUser> endUsers = new ArrayList<>(event.getMemberList().size());
        for (String email: event.getMemberList().keySet()) {
            endUsers.add(new UserEndpoint().getFullUser(email));
        }
        return endUsers;
    }

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

    /**
     * Ändert das bestehende Event auf dem Server mit den Daten von
     * dem übergebenen Event.
     *
     * @param event Das zu ändernde Event
     */
    private void updateEvent(Event event) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

        Route route;
        try {
            route = pm.getObjectById(Route.class, event.getRoute().getId());
        } catch(Exception ex) {
            route = null;
        }
        if (route != null) {
            event.setRoute(route);
        }

        try {
            pm.makePersistent(event);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Gibt eine ArrayList von allen auf dem Server gespeicherten Events zurück.
     *
     * @return Liste mit allen Events
     */
    public List<Event> getAllEvents() {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

        List<Event> result = null;
        try {
            result = (List<Event>) pm.newQuery(Event.class).execute();
        } finally {
            pm.close();
        }
        if (result != null) {
            // Events nocheinmal abrufen, da die Route nicht vollständig über ein QUery abgerufen werden kann
            List<Event> events = new ArrayList<>();
            for (Event e : result) {
                events.add(ofy().load().type(Event.class).id(e.getId()).safe());
            }
            return events;
        } else {
            result = new ArrayList<>();
        }
        return result;
    }
}
