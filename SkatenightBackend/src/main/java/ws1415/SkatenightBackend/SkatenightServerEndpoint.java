package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.io.IOException;
import java.util.Date;

/**
 * Die ServerAPI, die alle Zugriffe auf den Server für die Skatenight-App entgegen nimmt.
 * @author Richard, Daniel
 */
@Api(name = "skatenightAPI",
    version = "v1",
    clientIds = {Constants.ANDROID_USER_CLIENT_ID, Constants.ANDROID_HOST_CLIENT_ID,
            Constants.WEB_CLIENT_ID, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE})
public class SkatenightServerEndpoint {
    private Key hostRootKey;
    private DatastoreService datastore;

    public SkatenightServerEndpoint() {
        datastore = DatastoreServiceFactory.getDatastoreService();

        // Veranstalter definieren
        Entity root = new Entity("HostRoot", "root");
        datastore.put(root);
        hostRootKey = root.getKey();

        Entity e = new Entity("Host", "example", hostRootKey);
        e.setProperty("email", "example@example.com");
        datastore.put(e);
    }

    /**
     * Fügt die angegebene Mail-Adresse als Veranstalter hinzu.
     * @param mail Die hinzuzufügende Mail-Adresse
     */
    public void addHost(@Named("mail") String mail) {
        Entity e = new Entity("Host", mail, hostRootKey);
        e.setProperty("email", mail);
        datastore.put(e);
    }

    /**
     * Entfernt die angegebene Mail-Adresse aus den Veranstaltern.
     * @param mail Die zu entfernende Mail-Adresse
     */
    public void removeHost(@Named("mail") String mail) {
        datastore.delete(KeyFactory.createKey("Host", mail));
    }

    /**
     * Prüft, ob die angegebene Mail-Adresse zu einem authorisierten Veranstalter-Account gehört.
     * @param mail Die zu prüfende Mail
     * @return true, wenn der Account ein authorisierter Veranstalter ist, false sonst
     */
    public BooleanWrapper isHost(@Named("mail") String mail) {
        Query.Filter mailFilter = new Query.FilterPredicate("email", Query.FilterOperator.EQUAL,
                mail);
        Query q = new Query("Host", hostRootKey);
        q.setFilter(mailFilter);
        PreparedQuery pq = datastore.prepare(q);
        boolean isHost = (pq.countEntities(FetchOptions.Builder.withLimit(1)) == 1);
        return new BooleanWrapper(isHost);
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
            event.setDate((Date) e.getProperty("date"));
            event.setFee((Integer) e.getProperty("fee"));
            event.setLocation((String) e.getProperty("location"));
            event.setDescription((Text) e.getProperty("description"));
        }
        return event;
    }

    /**
     * Aktualisiert das auf dem Server gespeicherte Event-Objekt.
     * @param user Der User, der das Event-Objekt aktualisieren möchte.
     * @param e Das neue Event-Objekt.
     */
    public void setEvent(User user, Event e)throws OAuthRequestException, IOException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Query.Filter mailFilter = new Query.FilterPredicate("email", Query.FilterOperator.EQUAL,
                user.getEmail());
        Query q = new Query("Host", hostRootKey);
        q.setFilter(mailFilter);
        PreparedQuery pq = datastore.prepare(q);
        boolean isHost = (pq.countEntities(FetchOptions.Builder.withLimit(1)) == 1);

        if (isHost) {
            Entity event = new Entity("Event", "root_event");
            event.setProperty("title", e.getTitle());
            event.setProperty("date", e.getDate());
            event.setProperty("fee", e.getFee());
            event.setProperty("location", e.getLocation());
            event.setProperty("description", e.getDescription());
            datastore.put(event);
        } else {
            throw new OAuthRequestException("user is not a host");
        }
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












