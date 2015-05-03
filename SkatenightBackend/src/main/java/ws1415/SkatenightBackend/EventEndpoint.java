package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

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
import ws1415.SkatenightBackend.model.Member;
import ws1415.SkatenightBackend.model.Route;

/**
 * Created by Richard on 01.05.2015.
 */
public class EventEndpoint extends SkatenightServerEndpoint {

    /**
     * Aktualisiert das auf dem Server gespeicherte Event-Objekt.
     *
     * @param user Der User, der das Event-Objekt aktualisieren möchte.
     * @param e    Das neue Event-Objekt.
     */
    public void createEvent(User user, Event e) throws OAuthRequestException, IOException {
        createEvent(user, e, false, false);
    }

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
                e.setKey(null);
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
                            mb.addData("event_id", Long.toString(e.getKey().getId()));
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
     * Gibt die Events des Teilnehmers mit der übergebenen E-Mail zurück.
     *
     * @param email die E-Mail des Teilnehmers
     * @return Liste von Events, an denen der Teilnehmer teilnimmt
     */
    public List<Event> getCurrentEventsForMember(@Named("email") String email) {
        //  Nur Events ausgeben die auch JETZT stattfinden.
        List<Event> out = new ArrayList<Event>();
        List<Event> eventList = getAllEvents();
        for (Event e : eventList) {
            if (e.getMemberList().contains(email)) {
                out.add(e);
            }
        }
        return out;
    }

    /**
     * Fügt einen Member zu einem Event hinzu.
     *
     * @param keyId Die ID des Events
     * @param email die E-Mail des Teilnehmers
     */
    public void addMemberToEvent(@Named("id") long keyId, @Named("email") String email) {
        Event event = getEvent(keyId);
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
        Event event = getEvent(keyId);

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
    public List<Member> getMembersFromEvent(@Named("id") long keyId) {
        Event event = getEvent(keyId);

        List<Member> members = new ArrayList<Member>(event.getMemberList().size());
        for (String key: event.getMemberList()) {
            members.add(new UserEndpoint().getMember(key));
        }
        return members;
    }

    /**
     * Durchsucht alle auf dem Server gespeicherten Events nach dem übergebenen Event und gibt dieses,
     * falls vorhanden zurück.
     *
     * @param keyId Id von dem Event
     * @return Das Event, null falls keins gefunden wurde
     */
    public Event getEvent(@Named("id") long keyId) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            pm.getFetchPlan().setMaxFetchDepth(3);
            try {
                return (Event) pm.getObjectById(Event.class, keyId);
            } catch (Exception ex) {
                // Wenn Event nicht gefunden wurde, dann null zurückgeben
                return null;
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Löscht  das übergebene Event vom Server, falls dieses existiert
     *
     * @param keyId die Id von dem Event
     * @param user  der User, der die Operation aufruft
     * @throws OAuthRequestException
     */
    public BooleanWrapper deleteEvent(@Named("id") long keyId, User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!new HostEndpoint().isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Event event = getEvent(keyId);
            if (event != null) {
                pm.makePersistent(event);
                pm.deletePersistent(event);
                return new BooleanWrapper(true);
            }
        } finally {
            pm.close();
        }
        return new BooleanWrapper(false);
    }

    /**
     * Ändert das bestehende Event auf dem Server mit den Daten von
     * dem übergebenen Event.
     *
     * @param event Das zu ändernde Event
     */
    private void updateEvent(Event event) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

        Key key = event.getRoute().getKey();
        Route route;
        try {
            route = pm.getObjectById(Route.class, key);
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
        long keyId = event.getKey().getId();
        Date oldDate = getEvent(keyId).getMetaData().getDate();
        Date newDate = event.getMetaData().getDate();
        boolean dateChanged = oldDate.equals(newDate);
        BooleanWrapper b = deleteEvent(keyId, user);
        if(b.value) {
            createEvent(user, event, true, dateChanged);
        }

        return new BooleanWrapper(b.value);
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
                events.add(getEvent(e.getKey().getId()));
            }
            return events;
        } else {
            result = new ArrayList<>();
        }
        return result;
    }
}
