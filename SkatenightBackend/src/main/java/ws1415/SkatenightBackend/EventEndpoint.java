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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.gcm.Message;
import ws1415.SkatenightBackend.gcm.MessageType;
import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.gcm.Sender;
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
import ws1415.SkatenightBackend.transport.StringWrapper;
import ws1415.SkatenightBackend.transport.UserLocationInfo;

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
            if (!event.getMemberList().values().contains(EventRole.HOST)) {
                throw new IllegalStateException("the last host of an event can not be deleted");
            }
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
            log.info("user not participating");
            return false;
        }
        log.info("role: " + event.getMemberList().get(endUser));
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

        EventMetaDataList result = new EventMetaDataList();
        result.setList(new LinkedList<EventMetaData>());
        if (filter.getUserId() != null) {
            // Events abrufen, an denen ein Benutzer teilnimmt
            List<Long> eventIDs = new UserEndpoint().listEventsFromUser(filter.getUserId());

            if (eventIDs != null) {
                List<Event> events = new LinkedList<>(ofy().load().group(EventMetaData.class).type(Event.class).ids(eventIDs).values());
                Collections.sort(events, new Comparator<Event>() {
                    @Override
                    public int compare(Event o1, Event o2) {
                        int c = o1.getDate().compareTo(o2.getDate());
                        if (c == 0) {
                            c = o1.getId().compareTo(o2.getId());
                        }
                        return c;
                    }
                });

                // Zählt wieiviele Events mit diesem Cursor bereits abgerufen wurden
                int count = 0;
                if (filter.getCursorString() != null) {
                    Long lastId = Long.parseLong(filter.getCursorString().substring(0, filter.getCursorString().indexOf(' ')));
                    Long lastCount = Long.parseLong(filter.getCursorString().substring(filter.getCursorString().indexOf(' ') + 1));

                    while (lastCount > 0 && !events.isEmpty()) {
                        if (events.get(0).getId().equals(lastId)) {
                            events.remove(0);
                            count++;
                            break;
                        }
                        events.remove(0);
                        count++;
                        lastCount--;
                    }

                }

                // Minimale Sichtbarkeit bestimmen, die gegeben sein muss
                EventParticipationVisibility minVisibility;
                if (user.getEmail().equals(filter.getUserId())) {
                    // Der Benutzer ruft seine eigenen Events ab
                    minVisibility = EventParticipationVisibility.PRIVATE;
                } else if (new UserEndpoint().isFriendWith(filter.getUserId(), user.getEmail())) {
                    // Der Benutzer ruft die Events eines Freundes ab
                    minVisibility = EventParticipationVisibility.FRIENDS;
                } else {
                    // Der Benutzer ruft die Events eines unbekannten ab
                    minVisibility = EventParticipationVisibility.PUBLIC;
                }

                long id = -1;
                Iterator<Event> iterator = events.iterator();
                while (iterator.hasNext() && result.getList().size() < filter.getLimit()) {
                    Event e = iterator.next();
                    if (e.getMemberVisibility().get(filter.getUserId()).compareTo(minVisibility) >= 0) {
                        result.getList().add(new EventMetaData(e));
                        id = e.getId();
                        count++;
                    }
                }

                result.setCursorString(id + " " + count);
            }
        } else {
            // Alle Events abrufen
            QueryResultIterator<Event> iterator;
            Query<Event> q = ofy().load().group(EventMetaData.class).type(Event.class).limit(filter.getLimit());
            if (filter.getCursorString() != null) {
                q = q.startAt(Cursor.fromWebSafeString(filter.getCursorString()));
            }
            q = q.order("-date");
            iterator = q.iterator();
            while (iterator.hasNext() && result.getList().size() < filter.getLimit()) {
                result.getList().add(new EventMetaData(iterator.next()));
            }
            result.setCursorString(iterator.getCursor().toWebSafeString());
        }

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
        Event event = ofy().load().type(Event.class).id(eventId).safe();
        EventData data = new EventData(user, event);
        for (String participant : event.getMemberVisibility().keySet()) {
            switch (event.getMemberVisibility().get(participant)) {
                case PRIVATE:
                    if (!user.getEmail().equals(participant)) {
                        data.getMemberList().remove(participant);
                    }
                    break;
                case FRIENDS:
                    if (!user.getEmail().equals(participant) && new UserEndpoint().isFriendWith(participant, user.getEmail())) {
                        data.getMemberList().remove(participant);
                    }
                    break;
            }
        }
        return data;
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

        // Event in der Liste des Benutzers eintragen
        new UserEndpoint().addEventToUser(user.getEmail(), event);

        // GCM-Nachricht an Geräte schicken, damit Event-Liste aktualisiert wird
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            RegistrationManager rm = getRegistrationManager(pm);
            if (rm.getRegisteredUser() != null && !rm.getRegisteredUser().isEmpty()) {
                Sender sender = new Sender(Constants.GCM_API_KEY);
                Message.Builder mb = new Message.Builder()
                        // Nachricht erst anzeigen, wenn der Benutzer sein Handy benutzt
                        .delayWhileIdle(false)
                        .collapseKey("new_event")
                        .addData("type", MessageType.EVENT_NOTIFICATION_MESSAGE.name())
                        .addData("content", event.getTitle())
                        .addData("title", "Neue Veranstaltung!");
                Message m = mb.build();
                try {
                    sender.send(m, new LinkedList<>(rm.getRegisteredUser()), 1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            pm.close();
        }

        return event;
    }

    /**
     * Editiert das auf dem Server gepsicherte Event mit den Daten des übergebenen Events.
     * Der aufrufende Benutzer muss das Privileg EDIT_EVENT im Event haben.
     * @param user     Der aufrufende Benutzer.
     * @param event    Die neuen Daten des Events.
     */
    public Event editEvent(User user, Event event) throws OAuthRequestException, IOException {
        if (event == null) {
            throw new IllegalArgumentException("no event submitted");
        }
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }

        Event oldEvent = ofy().load().type(Event.class).id(event.getId()).safe();
        if (!hasPrivilege(oldEvent, user.getEmail(), Privilege.EDIT_EVENT)) {
            throw new OAuthRequestException("insufficient privileges");
        }

        boolean date_changed = event.getDate().equals(oldEvent.getDate());
        oldEvent.setTitle(event.getTitle());
        oldEvent.setDate(event.getDate());
        oldEvent.setDescription(event.getDescription());
        oldEvent.setMeetingPlace(event.getMeetingPlace());
        oldEvent.setFee(event.getFee());
        oldEvent.setDynamicFields(event.getDynamicFields());
        oldEvent.setRoute(event.getRoute());
        ofy().save().entity(oldEvent).now();

        // Nachricht an Teilnehmer des Events schicken
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            RegistrationManager rm = getRegistrationManager(pm);
            List<String> regIds = new LinkedList<>();
            for (String s : oldEvent.getMemberList().keySet()) {
                if (rm.getUserIdByMail(s) != null) {
                    regIds.add(rm.getUserIdByMail(s));
                }
            }
            if (rm.getRegisteredUser() != null) {
                Sender sender = new Sender(Constants.GCM_API_KEY);
                Message.Builder mb = new Message.Builder()
                        // Nachricht erst anzeigen, wenn der Benutzer sein Handy benutzt
                        .delayWhileIdle(false)
                        .collapseKey("new_event")
                        .addData("type", MessageType.EVENT_NOTIFICATION_MESSAGE.name())
                        .addData("content", event.getTitle())
                        .addData("date_changed", Boolean.toString(date_changed))
                        .addData("new_date", String.valueOf(event.getDate().getTime()))
                        .addData("event_id", String.valueOf(event.getId()))
                        .addData("title", "Veranstaltungsdaten aktualisiert!");
                Message m = mb.build();
                sender.send(m, regIds, 1);
            }
        } finally {
            pm.close();
        }

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
        Event event = ofy().load().type(Event.class).id(id).now();
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
     * @return Gibt die Rolle zurück, die dem Teilnehmer zugeordnet wurde.
     */
    public StringWrapper joinEvent(User user, @Named("eventId") long eventId, @Named("visibility") EventParticipationVisibility visibility)
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
        return new StringWrapper(event.getMemberList().get(user.getEmail()).name());
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
     * Hilfsmethode für die Endpoint-Klassen, die einen Benutzer aus der Liste der Teilnehmer eines Events entfernt.
     * @param mail       Die Mail-Adresse des zu entfernenden Benutzers.
     * @param eventId    Die ID des Events, aus dem der Benutzer entfernt werden soll.
     */
    protected void removeUserFromEvent(String mail, long eventId) {
        Event event = ofy().load().group(EventParticipationData.class).type(Event.class).id(eventId).safe();
        event.getMemberList().remove(mail);
        event.getMemberVisibility().remove(mail);
        ofy().save().entity(event).now();
    }

    /**
     * Ändert den Sichtbarkeitsstatus einer Teilnahme am betreffenden Event.
     * @param user          Der Benutzer, für den die Teilnahme-Sichtbarkeit geändert wird.
     * @param eventId       Die ID des Events.
     * @param visibility    Die neue Sichtbarkeit.
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

    /**
     * Sendet eine Durchsage per GCM an alle Teilnehmer des Events mit der angegebenen ID, die in der
     * entsprechenden Rolle angemeldet sind.
     * @param user       Der aufrufende Benutzer.
     * @param eventId    Die ID des Events, für das die Durchsage gesendet wird.
     * @param role       Die Rolle, an die gesendet wird.
     * @param title      Der Titel der Durchsage.
     * @param message    Die Nachricht der Durchsage.
     */
    public void sendBroadcast(User user, @Named("eventId") long eventId, @Named("role") EventRole role,
                              @Named("title") String title, @Named("message") String message)
            throws OAuthRequestException, IOException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Event event = ofy().load().group(EventParticipationData.class).type(Event.class).id(eventId).safe();
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            RegistrationManager rm = getRegistrationManager(pm);
            List<String> regIds = new LinkedList<>();
            for (String s : event.getMemberList().keySet()) {
                if (event.getMemberList().get(s) == role && rm.getUserIdByMail(s) != null) {
                    regIds.add(rm.getUserIdByMail(s));
                }
            }
            if (!regIds.isEmpty()) {
                Message m = new Message.Builder()
                        .timeToLive(2419200)
                        .addData("type", MessageType.NOTIFICATION_MESSAGE.name())
                        .addData("title", title)
                        .addData("content", message)
                        .build();
                Sender s = new Sender(Constants.GCM_API_KEY);
                s.send(m, regIds, 1);
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt die Positionen der Teilnehmer einer Veranstaltung zurück, die die angegebene Rolle eingenommen
     * haben. Der aufrufende Benutzer muss ein Teilnehmer der Veranstaltung sein und kann nur bestimmte
     * Rollen abrufen.
     * @param eventId    Die Id der Veranstaltung.
     * @param role       Die abzurufende Rolle.
     * @return Eine Liste der Positionen der Teilnehmer, die die angegebene Rolle einnehmen.
     */
    public List<UserLocationInfo> getEventRolePositions(User user, @Named("eventId") long eventId, @Named("role") EventRole role) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Event event = ofy().load().group(EventParticipationData.class).type(Event.class).id(eventId).safe();
        if (!event.getMemberList().containsKey(user.getEmail())) {
            throw new OAuthRequestException("user has to be participant");
        }
        List<String> roleParticipants = new LinkedList<>();
        for (String s : event.getMemberList().keySet()) {
            if (event.getMemberList().get(s) == role) {
                roleParticipants.add(s);
            }
        }
        return new UserEndpoint().listUserLocationInfo(user, roleParticipants);
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
