package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import ws1415.SkatenightBackend.gcm.Message;
import ws1415.SkatenightBackend.gcm.MessageType;
import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.gcm.Sender;
import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.EventMetaData;
import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.model.Member;
import ws1415.SkatenightBackend.model.Route;
import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.UserLocation;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Stellt Methoden zur Verarbeitung von Events auf dem Backend bereit.
 * @author Richard Schulze
 */
public class EventEndpoint extends SkatenightServerEndpoint {

    /**
     * Gibt eine Liste aller auf dem Server gespeicherter Metadaten von Events zurück.
     * @return Eine Liste aller auf dem Server gespeicherter EventMetaData-Objekte.
     */
    public List<EventMetaData> getEventsMetaData() {
        return ofy().load().type(EventMetaData.class).list();
    }

    /**
     * Ruft ein Event aus der Datenbank ab.
     * @param metaDataId Die Id der Metadaten des Events das abgerufen werden soll.
     * @param eventId    Die Id des abzurufenden Events.
     * @return Das Event-Objekt inklusive Metadaten und Gallery-Objekt. Die Bilder der Galerie
     * werden nicht mit abgerufen.
     */
    public Event getEvent(@Named("metaDataId") Long metaDataId, @Named("eventId") Long eventId) {
        if (metaDataId == null) {
            return null;
        }
        return ofy().load().type(Event.class)
                .parent(Key.create(EventMetaData.class, metaDataId))
                .id(eventId)
                .safe();
    }

    /**
     * Erstellt ein neues Event mit den angegebenen Daten auf dem Server und gibt das persistierte
     * Event-Objekt zurück. Kann nur von Benutzern aufgerufen werden, die in der globalen Domäne die
     * Rolle Veranstalter haben.
     *
     * @param user            Der aufrufende Benutzer.
     * @param icon            TODO
     * @param title           Der Titel des anzulegenden Events.
     * @param date            Das Datum des Events.
     * @param headerImage     TODO
     * @param description     Der Beschreibungstext des Events.
     * @param meetingPlace    Der Treffpunkt für das Event.
     * @param fee             Die Gebühr für das Event.
     * @param images          TODO
     * @return Das persistierte Event-Objekt.
     */
    // TODO Route hinzufügen
    public Event createEvent(User user, @Named("icon") String icon, @Named("title") String title,
                             @Named("date") Date date, @Named("headerImage") String headerImage,
                             Text description, @Named("meetingPlace") String meetingPlace,
                             @Named("fee") int fee, @Named("images") List<String> images) {
        // TODO User prüfen
        // TODO Event prüfen

        Gallery gallery = new Gallery();
        ofy().save().entity(gallery).now();

        EventMetaData metaData = new EventMetaData();
        metaData.setIcon(icon);
        metaData.setTitle(title);
        metaData.setDate(date);
        ofy().save().entity(metaData).now();

        Event event = new Event();
        event.setHeaderImage(headerImage);
        event.setDescription(description);
        event.setMeetingPlace(meetingPlace);
        event.setFee(fee);
        event.setImages(images);
        event.setMetaData(metaData);
        event.setGallery(gallery);
        ofy().save().entity(event).now();

        metaData.setEventId(event.getId());
        ofy().save().entity(metaData).now();

        return event;
    }

    /**
     * Löscht das gesamte Event inklusive Metadaten und Gallerie. Der aufrufende Benutzer muss die
     * Rolle Veranstalter in der globalen Domäne haben.
     * @param user Der aufrufende Benutzer.
     * @param id   Die ID der Metadaten des zu löschenden Events.
     */
    public void deleteEvent(User user, @Named("id") Long id) {
        if (id != null) {
            // TODO User prüfen
            // TODO Gallery und alle zugehörigen Blobs löschen

            // Die Eventmetadaten und alle untergeordneten Objekte löschen
            Key metaDataKey = Key.create(EventMetaData.class, id);
            ofy().delete().keys(ofy().load().ancestor(metaDataKey).keys().list());
            ofy().delete().key(metaDataKey).now();
        }
    }













































    // ---------------------------------------------------------------------------------------------
    // |                                  Alte Endpoint-Methoden                                   |
    // ---------------------------------------------------------------------------------------------

    /**
     * Aktualisiert das auf dem Server gespeicherte Event-Objekt. Diese Methode ist private, damit
     * aus den Apps der Parameter editing nicht manuell angegeben werden kann.
     *
     * @param user      Der User, der das Event-Objekt aktualisieren möchte.
     * @param e         Das neue Event-Objekt.
     * @param editing   true, wenn das Event editiert wurde, false, wenn es sich um ein neues Event
     *                  handelt. Hat Auswirkungen auf die Notification, die an die Benutzer gesendet
     *                  wird.
     * @param dateChanged true, wenn das Event geändert wurde und sich die Startzeit geändert hat,
     *                    sonst false.
     */
    private void createEvent(User user, Event e, boolean editing, boolean dateChanged) throws OAuthRequestException, IOException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!new HostEndpoint().isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            if (e != null) {
                Query q = pm.newQuery(Route.class);
                q.setFilter("name == nameParam");
                q.declareParameters("String nameParam");
                List<Route> results = (List<Route>) q.execute(e.getRoute().getName());
                if (!results.isEmpty()) {
                    e.setRoute(results.get(0));
                }
                // Weil sonst Nullpointer beim Editieren kommmt
                pm.makePersistent(e);

                // Benachrichtigung an User schicken
                long secondsTillStart = (e.getMetaData().getDate().getTime() - System.currentTimeMillis()) / 1000;

                // Benachrichtigung nur schicken, wenn Event in der Zukunft liegt
                if (secondsTillStart > 0) {
                    String eventTitle = e.getMetaData().getTitle();
                    Sender sender = new Sender(Constants.GCM_API_KEY);
                    Message.Builder mb = new Message.Builder()
                            // Nachricht erst anzeigen, wenn der Benutzer sein Handy benutzt
                            .delayWhileIdle(true)
                            .collapseKey("event_created")
                                    // Nachricht verfallen lassen, wenn Benutzer erst nach Event online geht
                            .timeToLive((int) secondsTillStart)
                            .addData("type", MessageType.EVENT_NOTIFICATION_MESSAGE.name())
                            .addData("content", eventTitle);
                    if (editing) {
                        mb.addData("title", "Ein Event wurde angepasst.");
                        mb.addData("date_changed", Boolean.toString(dateChanged));
                        if (dateChanged) {
                            mb.addData("event_id", Long.toString(e.getId()));
                            mb.addData("new_date", Long.toString(e.getMetaData().getDate().getTime()));
                        }
                    } else {
                        mb.addData("title", "Ein neues Event wurde erstellt.");
                    }
                    Message m = mb.build();
                    try {
                        sender.send(m, getRegistrationManager(pm).getRegisteredUser(), 1);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Fügt einen Member zu einem Event hinzu.
     *
     * @param keyId Die ID des Events
     * @param email die E-Mail des Teilnehmers
     */
    public void addMemberToEvent(@Named("id") long keyId, @Named("email") String email) {
        Event event = getEvent(0l, keyId);
        ArrayList<String> memberKeys = event.getMemberList();
        if (!memberKeys.contains(email)) {
            memberKeys.add(email);
            event.setMemberList(memberKeys);

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
        Event event = getEvent(0l, keyId);

        ArrayList<String> memberKeys = event.getMemberList();
        if (memberKeys.contains(email)) {
            memberKeys.remove(email);
            event.setMemberList(memberKeys);

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
        Event event = getEvent(0l, keyId);

        List<EndUser> endUsers = new ArrayList<>(event.getMemberList().size());
        for (String email: event.getMemberList()) {
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
        Event event = getEvent(0l, keyId);

        List<UserLocation> userLocations = new ArrayList<>();
        for (String email: event.getMemberList()) {
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
            route = pm.getObjectById(Route.class, event.getRoute().getKey());
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
     * Bearbeitet das Event mit der Id des übergebenen Events mit den Daten des übergebenen Events.
     *
     * @param event das zu bearbeitende Event mit den neuen Daten
     * @param user User, der die Methode aufruft
     * @return true, wenn Aktion erfolgreich, false sonst
     * @throws OAuthRequestException
     * @throws IOException
     */
    public BooleanWrapper editEvent(Event event, User user) throws OAuthRequestException, IOException {
        long keyId = event.getId();
        Date oldDate = getEvent(0l, keyId).getMetaData().getDate();
        Date newDate = event.getMetaData().getDate();
        boolean dateChanged = oldDate.equals(newDate);
        deleteEvent(user, event.getId());
        createEvent(user, event, true, dateChanged);
        return new BooleanWrapper(true);
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
                events.add(getEvent(0l, e.getId()));
            }
            return events;
        } else {
            result = new ArrayList<>();
        }
        return result;
    }
}
