package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;


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
    private PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(
            "transactions-optional");

    /**
     * Fügt die angegebene Mail-Adresse als Veranstalter hinzu.
     * @param mail Die hinzuzufügende Mail-Adresse
     */
    public void addHost(@Named("mail") String mail) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Query q = pm.newQuery(Host.class);
            q.setFilter("email == emailParam");
            q.declareParameters("String emailParam");
            List<Host> results = (List<Host>) q.execute(mail);
            if (results.isEmpty()) {
                Host h = new Host();
                h.setEmail(mail);
                pm.makePersistent(h);
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Entfernt die angegebene Mail-Adresse aus den Veranstaltern.
     * @param mail Die zu entfernende Mail-Adresse
     */
    public void removeHost(@Named("mail") String mail) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Query q = pm.newQuery(Host.class);
            q.setFilter("email == emailParam");
            q.declareParameters("String emailParam");
            List<Host> results = (List<Host>) q.execute(mail);
            if (!results.isEmpty()) {
                pm.deletePersistentAll(results);
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Prüft, ob die angegebene Mail-Adresse zu einem authorisierten Veranstalter-Account gehört.
     * @param mail Die zu prüfende Mail
     * @return true, wenn der Account ein authorisierter Veranstalter ist, false sonst
     */
    public BooleanWrapper isHost(@Named("mail") String mail) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Query q = pm.newQuery(Host.class);
            q.setFilter("email == emailParam");
            q.declareParameters("String emailParam");
            List<Host> results = (List<Host>) q.execute(mail);
            return new BooleanWrapper(!results.isEmpty());
        } finally {
            pm.close();
        }
    }

    /**
     * Liefert das aktuell auf dem Server hinterlegte Event-Objekt.
     * @return Das aktuelle Event-Objekt.
     */
    public Event getEvent() {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            List<Event> results = (List<Event>) pm.newQuery(Event.class).execute();
            if (results.isEmpty()) {
                return null;
            } else {
                // Name abrufen, damit die Daten gefetcht werden
                results.get(0).getRoute().getName();
                return results.get(0);
            }
        } finally {
            pm.close();
        }
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
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            // Altes Event-Objekt löschen
            List<Event> events = (List<Event>) pm.newQuery(Event.class).execute();
            pm.deletePersistentAll(events);
            if (e != null) {
                Query q = pm.newQuery(Route.class);
                q.setFilter("name == nameParam");
                q.declareParameters("String nameParam");
                List<Route> results = (List<Route>) q.execute(e.getRoute().getName());
                if (!results.isEmpty()) {
                    e.setRoute(results.get(0));
                }
                pm.makePersistent(e);
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Dient Testzwecken, damit das Event ohne credentials gesetzt werden kann.
     */
    public void setEventTestMethod(Event e) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            // Altes Event-Objekt löschen
            List<Event> events = (List<Event>) pm.newQuery(Event.class).execute();
            pm.deletePersistentAll(events);
            if (e != null) {
                Query q = pm.newQuery(Route.class);
                q.setFilter("name == nameParam");
                q.declareParameters("String nameParam");
                List<Route> results = (List<Route>) q.execute(e.getRoute().getName());
                if (!results.isEmpty()) {
                    e.setRoute(results.get(0));
                }
                pm.makePersistent(e);
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Aktualisiert die für die angegebene Mail-Adresse gespeicherte Position auf dem Server. Falls
     * kein Member-Objekt für die Mail-Adresse existiert, so wird ein neues Objekt angelegt.
     * @param mail Die Mail-Adresse des zu aktualisierenden Member-Objekts.
     * @param location Die neue Position.
     */
    public void updateMemberLocation(@Named("mail") String mail,
                                     @Named("location") String location) {
        if (mail != null) {
            Member m = getMember(mail);
            if (m == null) {
                // Neuen Member anlegen
                m = new Member();
                m.setEmail(mail);
                /**
                 * TODO:Als Name wird zurzeit die Mail-Adresse verwendet, da noch keine Eingabe-
                 * möglichkeit für den Namen besteht. (sollte im nächsten Sprint übernommen werden)
                 */
                m.setName(mail);
            }
            m.setLocation(location);
            m.setUpdatedAt(new Date());

            PersistenceManager pm = pmf.getPersistenceManager();
            try {
                pm.makePersistent(m);
            } finally {
                pm.close();
            }
        }
    }

    /**
     * Liefert das auf dem Server hinterlegte Member-Objekt mit der angegebenen Mail.
     * @param email Die Mail-Adresse des Member-Objekts, das abgerufen werden soll.
     * @return Das aktuelle Member-Objekt.
     */
    public Member getMember(@Named("email") String email) {
        Member member = null;
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Query q = pm.newQuery(Member.class);
            q.setFilter("email == emailParam");
            q.declareParameters("String emailParam");
            List<Member> results = (List<Member>) q.execute(email);
            if (!results.isEmpty()) {
                member = results.get(0);
            }
        } catch(JDOObjectNotFoundException e) {
            // Wird geworfen, wenn kein Objekt mit dem angegebenen Schlüssel existiert
            // In diesem Fall null zurückgeben
            return null;
        } finally {
            pm.close();
        }
        return member;
    }

    /**
     * Speichert die angegebene Route auf dem Server
     * @param route zu speichernde Route
     */
    public void addRoute(Route route){
        PersistenceManager pm = pmf.getPersistenceManager();
        if(route != null){
            try{
                pm.makePersistent(route);
            }finally {
                pm.close();
            }
        }
    }

    /**
     *  Gibt eine Liste von allen gespeicherten Routen zurück
     *  @return Liste der Routen.
     */
    public List<Route> getRoutes(){
        PersistenceManager pm = pmf.getPersistenceManager();

        try{
            List<Route> result = (List<Route>) pm.newQuery(Route.class).execute();
            if(result.isEmpty()){
                return new ArrayList<Route>();
            }else{
                return result;
            }
        }finally{
            pm.close();
        }
    }

    /**
     * Lösche Route vom Server
     * @param id Die ID der zu löschenden Route.
     * @return true, wenn die Route gelöscht wurde, sonst false
     */
    public BooleanWrapper deleteRoute(@Named("id") long id) {
        Event event = getEvent();
        if (event.getRoute().getKey().getId() == id) {
            return new BooleanWrapper(false);
        }
        PersistenceManager pm = pmf.getPersistenceManager();
        try{
            for (Route r : (List<Route>) pm.newQuery(Route.class).execute()) {
                if (r.getKey().getId() == id) {
                    pm.deletePersistent(r);
                    return new BooleanWrapper(true);
                }
            }
        }finally{
            pm.close();
        }
        return new BooleanWrapper(false);
    }

}












