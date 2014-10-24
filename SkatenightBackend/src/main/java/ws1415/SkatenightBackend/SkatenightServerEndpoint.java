package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Api;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

import java.util.Date;

/**
 * Die ServerAPI, die alle Zugriffe auf den Server für die Skatenight-App entgegen nimmt.
 * @author Richard, Daniel
 */
@Api(name = "skatenightAPI",
    version = "v1")
public class SkatenightServerEndpoint {
    private DatastoreService datastore;

    public SkatenightServerEndpoint() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    /**
     * Liefert das aktuell auf dem Server hinterlegte Event-Objekt.
     * @return Das aktuelle Event-Objekt.
     */
    public Event getEvent() {
        Key key = KeyFactory.createKey("Event", "root_event");
        Entity e = null;
        try {
            e = datastore.get(key);
        } catch (EntityNotFoundException ex) {
            ex.printStackTrace();
        }
        Event event = null;
        if (e != null) {
            event = new Event();
            event.setTitle((String) e.getProperty("title"));
            event.setDate((String) e.getProperty("date"));
            event.setFee((String) e.getProperty("fee"));
            event.setLocation((String) e.getProperty("location"));
            event.setDescription((Text) e.getProperty("description"));
        }
        return event;
    }

    /**
     * Aktualisiert das auf dem Server gespeicherte Event-Objekt.
     * @param e Das neue Event-Objekt.
     */
    public void setEvent(Event e) {
        Entity event = new Entity("Event", "root_event");
        event.setProperty("title", e.getTitle());
        event.setProperty("date", e.getDate());
        event.setProperty("fee", e.getFee());
        event.setProperty("location", e.getLocation());
        event.setProperty("description", e.getDescription());
        datastore.put(event);
    }

    /**
     * Liefert das aktuell auf dem Server hinterlegte Event-Objekt.
     * @return Das aktuelle Event-Objekt.
     */
    public Member getMember() {
        Key key = KeyFactory.createKey("Member", "root_event");
        Entity m = null;
        try {
            m = datastore.get(key);
        } catch (EntityNotFoundException ex) {
            ex.printStackTrace();
        }
        Member member = null;
        if (m != null) {
            member = new Member();
            member.setName((String) m.getProperty("name"));
            member.setUpdatedAt((String) m.getProperty("updatedAt"));
            member.setLocation((String) m.getProperty("location"));
        }
        return member;
    }

    /**
     * Aktualisiert das auf dem Server gespeicherte Member-Objekt.
     * @param m Das neue Member-Objekt.
     */
    public void setMember(Member m) {
        Entity member = new Entity("Member", "root_event");
        member.setProperty("name", m.getName());
        member.setProperty("updatedAt", m.getUpdatedAt());
        member.setProperty("location", m.getLocation());
        datastore.put(member);
    }

}












