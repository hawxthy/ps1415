package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Api;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Die ServerAPI, die alle Zugriffe auf den Server für die Skatenight-App entgegen nimmt.
 */
@Api(name = "skatenightAPI",
    version = "v1")
public class SkatenightServerEndpoint {
    private DatastoreService datastore;

    public SkatenightServerEndpoint() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

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
            event.setName((String) e.getProperty("name"));
        }
        return event;
    }

    public void setEvent(Event e) {
        Entity event = new Entity("Event", "root_event");
        event.setProperty("name", e.getName());
        datastore.put(event);
    }

}
