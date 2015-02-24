package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiNamespace;
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
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import ws1415.SkatenightBackend.gcm.Message;
import ws1415.SkatenightBackend.gcm.MessageType;
import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.gcm.Sender;


/**
 * Die ServerAPI, die Api-Methoden zur Verfügung stellt.
 *
 * @author Richard, Daniel
 */
@Api(name = "skatenightAPI",
    version = "v1",
    clientIds = {Constants.ANDROID_USER_CLIENT_ID, Constants.ANDROID_HOST_CLIENT_ID,
            Constants.WEB_CLIENT_ID, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = "skatenight.com", ownerName = "skatenight"))
public class SkatenightServerEndpoint {
    public static final int FIELD_UPDATE_INTERVAL = 30000;
    public static final Logger logger = Logger.getLogger(SkatenightServerEndpoint.class.getName());

    private PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(
            "transactions-optional");
    private long lastFieldUpdateTime = 0;

    /**
     * Fügt die angegebene Mail-Adresse als Veranstalter hinzu.
     * @param mail Die hinzuzufügende Mail-Adresse
     */
    public void addHost(User user, @Named("mail") String mail) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

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
    public void removeHost(User user, @Named("mail") String mail) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

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
     *
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
     * Gibt die Liste aller registrierten Veranstalter zurück.
     * @param user Der Benutzer, der die Liste anfordert. Er muss bereits als Veranstalter eingetra-
     *             gen sein.
     * @return Eine Liste aller Veranstalter.
     * @throws OAuthRequestException
     */
    public List<Host> getHosts(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        PersistenceManager pm = pmf.getPersistenceManager();
        List<Host> result;
        try {
            result = (List<Host>) pm.newQuery(Host.class).execute();
        } finally {
            pm.close();
        }
        if (result == null) {
            result = new ArrayList<Host>();
        }
        return result;
    }


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
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        PersistenceManager pm = pmf.getPersistenceManager();
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
                long secondsTillStart = (FieldType.getFusedDate(e).getTime() - System.currentTimeMillis()) / 1000;

                // Benachrichtigung nur schicken, wenn Event in der Zukunft liegt
                if (secondsTillStart > 0) {
                    String eventTitle = FieldType.getUniqueField(FieldType.TITLE.getId(), e).getValue();
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
                            mb.addData("new_date", Long.toString(FieldType.getFusedDate(e).getTime()));
                        }
                    } else {
                        mb.addData("title", "Ein neues Event wurde erstellt.");
                    }
                    Message m = mb.build();
                    try {
                        sender.send(m, getRegistrationManager(pm).getRegisteredUser(), 5);
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
     * Erstellt ein Member-Objekt für die angegebene
     * E-Mail.
     *
     * @param mail
     */
    public void createMember(@Named("mail") String mail) {
        Member m = getMember(mail);
        if (m == null) {
            m = new Member();
            m.setEmail(mail);
            m.setName(mail);

            PersistenceManager pm = pmf.getPersistenceManager();
            try {
                pm.makePersistent(m);
            } finally {
                pm.close();
            }
        }
    }

    /**
     * Aktualisiert die für die angegebene Mail-Adresse gespeicherte Position auf dem Server. Falls
     * kein Member-Objekt für die Mail-Adresse existiert, so wird ein neues Objekt angelegt.
     * @param mail Die Mail-Adresse des zu aktualisierenden Member-Objekts.
     * @param latitude
     * @param longitude
     */
    public void updateMemberLocation(@Named("mail") String mail,
                                     @Named("latitude") double latitude,
                                     @Named("longitude") double longitude,
                                     @Named("currentEventId") long currentEventId) {
        if (mail != null) {
            Member m = getMember(mail);
            m.setLatitude(latitude);
            m.setLongitude(longitude);
            m.setUpdatedAt(new Date());
            if (m.getCurrentEventId() == null || m.getCurrentEventId() != currentEventId) {
                m.setCurrentEventId(currentEventId);
                m.setCurrentWaypoint(0);
            }

            PersistenceManager pm = pmf.getPersistenceManager();
            try {
                calculateCurrentWaypoint(pm, m);
                pm.makePersistent(m);

                // Überprüfen ob mehr als 5 Minuten seit dem letzten Update vergangen sind.
                if (System.currentTimeMillis()-lastFieldUpdateTime >= FIELD_UPDATE_INTERVAL) {
                    calculateField(pm, m.getCurrentEventId());
                    lastFieldUpdateTime = System.currentTimeMillis();
                }
            } finally {
                pm.close();
            }
        }
    }

    /**
     * Hilfsmethode, die für den normalen Betireb nicht benötigt wird. Stellt eine Schnittstelle für
     * Simulationen her, die eine große Anzahl an Positionen auf dem Server aktualisieren ohne, dass
     * dabei pro Akutalisierung ein Serveraufruf notwendig ist.
     * Die Mails, Latituden und Logituden werden als Strings mit = als Trennzeichen kodiert, da ein
     * Aufruf mit Arrays nicht funktioniert hat.
     * @param mailParam Die Mail-Adressen der zu Aktualisierenden Member-Objekte als String, durch = getrennt.
     * @param latitudeParam Die neuen Latituden als String, durch = getrennt
     * @param longitudeParam Die neuen Longituden, durch = getrennt
     * @param currentEventId Die Event-ID, die für die Member gesetzt werden soll.
     */
    public void simulateMemberLocations(@Named("mails") String mailParam,
                                     @Named("latitudes") String latitudeParam,
                                     @Named("longitudes") String longitudeParam,
                                     @Named("currentEventId") long currentEventId) {
        StringTokenizer st = new StringTokenizer(mailParam, "=");
        String[] mail = new String[st.countTokens()];
        for (int i = 0; i < mail.length; i++) {
            mail[i] = st.nextToken();
        }
        st = new StringTokenizer(latitudeParam, "=");
        double[] latitude = new double[st.countTokens()];
        for (int i = 0; i < mail.length; i++) {
            latitude[i] = Double.parseDouble(st.nextToken());
        }
        st = new StringTokenizer(longitudeParam, "=");
        double[] longitude = new double[st.countTokens()];
        for (int i = 0; i < mail.length; i++) {
            longitude[i] = Double.parseDouble(st.nextToken());
        }

        if (mail != null && latitude != null && longitude != null) {
            Event event = getEvent(currentEventId);
            int count = Math.min(mail.length, Math.min(latitude.length, longitude.length));

            Member admin = getMember("richard-schulze@online.de");

            PersistenceManager pm = pmf.getPersistenceManager();
            UserGroup group = getUserGroup(pm, "Simulationsgruppe");
            if (group == null) {
                UserGroup ug = new UserGroup(admin);
                ug.setName("Simulationsgruppe");
                admin.addGroup(ug);
                pm.makePersistent(admin);
                pm.makePersistent(ug);
            }
            try {
                // Member-Objekte laden. Falls für eine angegebene Mail-Adresse kein Objekt besteht,
                // so wird es angelegt
                Member member;
                for (int i = 0; i < count; i++) {
                    try {
                        member = pm.getObjectById(Member.class, mail[i]);
                    } catch (Exception ex) {
                        // Skater nicht gefunden
                        member = new Member();
                        member.setEmail(mail[i]);
                        member.setName(mail[i]);
                    }
                    // Member zum Event hinzufügen, falls noch nicht geschehen
                    if (!event.getMemberList().contains(member.getEmail())) {
                        event.getMemberList().add(member.getEmail());
                    }
                    // Member in die Testgruppe aufnehmen, falls noch nicht geschehen
                    if (!member.getGroups().contains(group.getName())) {
                        member.addGroup(group);
                    }
                    pm.makePersistent(member);
                }
                pm.makePersistent(event);
                pm.makePersistent(group);
            } finally {
                pm.close();
            }

            for (int i = 0; i < count; i++) {
                updateMemberLocation(mail[i], latitude[i], longitude[i], currentEventId);
            }
        }
    }

    private void calculateCurrentWaypoint(PersistenceManager pm, Member member) {
        Long eventId = member.getCurrentEventId();
        if (eventId != null) {
            Event event = pm.getObjectById(Event.class, eventId);
            if (event != null) {
                Integer currentWaypoint = member.getCurrentWaypoint();
                if (currentWaypoint == null) {
                    member.setCurrentWaypoint(0);
                    currentWaypoint = 0;
                }
                List<RoutePoint> points = event.getRoute().getRoutePoints();
                if (currentWaypoint < points.size()-1) {
                    RoutePoint current = points.get(currentWaypoint);
                    RoutePoint next = points.get(currentWaypoint+1);
                    float distanceCurrent = distance(current.getLatitude(), current.getLongitude(), member.getLatitude(), member.getLongitude());
                    float distanceNext = distance(next.getLatitude(), next.getLongitude(), member.getLatitude(), member.getLongitude());
                    boolean findNextWaypoint = false;
                    if (distanceCurrent > Constants.MAX_NEXT_WAYPOINT_DISTANCE) {
                        findNextWaypoint = true;
                    }
                    if (distanceNext < distanceCurrent) {
                        if (distanceNext < Constants.MAX_NEXT_WAYPOINT_DISTANCE) {
                            member.setCurrentWaypoint(currentWaypoint+1);
                            findNextWaypoint = false;
                        }
                        else {
                            findNextWaypoint = true;
                        }
                    }
                    if (findNextWaypoint) {
                        // Den nächsten Wegpunkt finden:
                        float minDistance = Float.POSITIVE_INFINITY;
                        for (int i = currentWaypoint; i < points.size(); i++) {
                            float distance = distance(
                                    member.getLatitude(), member.getLongitude(),
                                    points.get(i).getLatitude(), points.get(i).getLongitude());

                            if (distance < 50.0f && distance < minDistance) {
                                member.setCurrentWaypoint(i);
                                minDistance = distance;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Berechnet anhand der aktuellen Positionen der Member das Feld.
     * Wobei das Feld um den Wegpunkte herum gebaut wird, welcher die meisten Member enthält
     * @param id event Id
     */
    private void calculateField(PersistenceManager pm, long id) {
        Event event = pm.getObjectById(Event.class, id);
        List<RoutePoint> points = event.getRoute().getRoutePoints();
        List<Member> members = getMembersFromEvent(event.getKey().getId());

        logger.info("points.size(): " + points.size());
        // array erstellen welches an der stelle n die Anzahl der Member enthält welche am RoutePoint n sind.
        int memberCountPerRoutePoint[] = new int[points.size()];
        for (Member member : members) {
            if (member.getCurrentEventId() != null && member.getCurrentEventId() == event.getKey().getId() && member.getCurrentWaypoint() != null) {
                logger.info(member.getEmail() + " current waypoint: " + member.getCurrentWaypoint());
                memberCountPerRoutePoint[member.getCurrentWaypoint()] = memberCountPerRoutePoint[member.getCurrentWaypoint()] + 1;
            }
        }

        // Den index des RoutePoints speichern an welchen die meisten Member sind.
        //int mostMemberPerWaypoint = -1;
        int mostMemberIndex = 0;
        for (int i = 0; i < memberCountPerRoutePoint.length; i++) {
            if (memberCountPerRoutePoint[i] > memberCountPerRoutePoint[mostMemberIndex]) {
                mostMemberIndex = i;
            }
        }

        // Vom mostMemberIndex rückwärts gehen bis 2 aufeinanderfolgende RoutePoints jeweils weniger
        // als 5 Member haben
        int first = mostMemberIndex;
        while (first > 0) {
            if (memberCountPerRoutePoint[first - 1] >= Constants.MIN_WAYPOINT_MEMBER_COUNT) {
                first--;
            } else if (first > 1 && memberCountPerRoutePoint[first - 2] >= Constants.MIN_WAYPOINT_MEMBER_COUNT) {
                first -= 2;
            } else {
                break;
            }
        }

        // Vom mostMemberIndex vorwaärts gehen bis 2 aufeinanderfolgende RoutePoints jeweils weniger
        // als 5 Member haben
        int last = mostMemberIndex;
        while (last < memberCountPerRoutePoint.length - 1) {
            if (memberCountPerRoutePoint[last + 1] >= Constants.MIN_WAYPOINT_MEMBER_COUNT) {
                last++;
            } else if (last < memberCountPerRoutePoint.length - 2 && memberCountPerRoutePoint[last + 2] >= Constants.MIN_WAYPOINT_MEMBER_COUNT) {
                last += 2;
            } else {
                break;
            }
        }

        event.setRouteFieldFirst(first);
        event.setRouteFieldLast(last);
        if (event.getRoute().getRoutePoints().size() == 0) {
            logger.info("RoutePoints wurden gelöscht!");
        }
        pm.makePersistent(event);
    }

    /**
     * Kopie der Funktion zur Distanzberechnung zwischen 2 Koordinaten aus dem Android Source code.
     *
     * @param startLat
     * @param startLon
     * @param endLat
     * @param endLon
     * @return
     */
    private float distance(double startLat, double startLon,
                           double endLat, double endLon) {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)
        int MAXITERS = 20;
        // Convert lat/long to radians
        startLat *= Math.PI / 180.0;
        endLat *= Math.PI / 180.0;
        startLon *= Math.PI / 180.0;
        endLon *= Math.PI / 180.0;
        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);
        double L = endLon - startLon;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(startLat));
        double U2 = Math.atan((1.0 - f) * Math.tan(endLat));
        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;
        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;
        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 :
                    cosU1cosU2 * sinLambda / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 :
                    cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)
            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1 + (uSquared / 16384.0) * // (3)
                    (4096.0 + uSquared *
                            (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * // (4)
                    (256.0 + uSquared *
                            (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) *
                    cosSqAlpha *
                    (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma * // (6)
                    (cos2SM + (B / 4.0) *
                            (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
                                    (B / 6.0) * cos2SM *
                                            (-3.0 + 4.0 * sinSigma * sinSigma) *
                                            (-3.0 + 4.0 * cos2SMSq)));
            lambda = L +
                    (1.0 - C) * f * sinAlpha *
                            (sigma + C * sinSigma *
                                    (cos2SM + C * cosSigma *
                                            (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)
            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }
        float distance = (float) (b * A * (sigma - deltaSigma));
        return distance;
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
        } catch (JDOObjectNotFoundException e) {
            // Wird geworfen, wenn kein Objekt mit dem angegebenen Schlüssel existiert
            // In diesem Fall null zurückgeben
            return null;
        } finally {
            pm.close();
        }
        return member;
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
                Member member = getMember(email);
                if (member.getCurrentEventId() == null || member.getCurrentEventId() != keyId) {
                    member.setCurrentEventId(keyId);
                    member.setCurrentWaypoint(0);
                }

                PersistenceManager pm = pmf.getPersistenceManager();
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
                    sender.send(m, new LinkedList<>(ids), 5);
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
            members.add(getMember(key));
        }
        return members;
    }

    /**
     * Speichert die angegebene Route auf dem Server
     *
     * @param user  Der User, der die Route hinzufügen möchte
     * @param route zu speichernde Route
     */
    public void addRoute(User user, Route route) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        PersistenceManager pm = pmf.getPersistenceManager();
        if (route != null) {
            try {
                pm.makePersistent(route);
            } finally {
                pm.close();
            }
        }
    }

    /**
     * Gibt eine Liste von allen gespeicherten Routen zurück
     *
     * @return Liste der Routen.
     */
    public List<Route> getRoutes() {
        PersistenceManager pm = pmf.getPersistenceManager();

        try {
            List<Route> result = (List<Route>) pm.newQuery(Route.class).execute();
            if (result.isEmpty()) {
                return new ArrayList<Route>();
            } else {
                return result;
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Lösche Route vom Server
     *
     * @param user Der User, der die Route löschen möchte
     * @param id   Die ID der zu löschenden Route.
     * @return true, wenn die Route gelöscht wurde, sonst false
     */
    public BooleanWrapper deleteRoute(User user, @Named("id") long id) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        List<Event> eventList = getAllEvents();
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getRoute().getKey().getId() == id) {
                return new BooleanWrapper(false);
            }
        }

        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            for (Route r : (List<Route>) pm.newQuery(Route.class).execute()) {
                if (r.getKey().getId() == id) {
                    pm.deletePersistent(r);
                    return new BooleanWrapper(true);
                }
            }
        } finally {
            pm.close();
        }
        return new BooleanWrapper(false);
    }

    /**
     * Durchsucht alle auf dem Server gespeicherten Events nach dem übergebenen Event und gibt dieses,
     * falls vorhanden zurück.
     *
     * @param keyId Id von dem Event
     * @return Das Event, null falls keins gefunden wurde
     */
    public Event getEvent(@Named("id") long keyId) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            pm.getFetchPlan().setMaxFetchDepth(3);
            return (Event) pm.getObjectById(Event.class, keyId);
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
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }
        PersistenceManager pm = pmf.getPersistenceManager();
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
        PersistenceManager pm = pmf.getPersistenceManager();

        Key key = event.getRoute().getKey();
        Route route = pm.getObjectById(Route.class, key);
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
        Date oldDate = FieldType.getFusedDate(getEvent(keyId));
        Date newDate = FieldType.getFusedDate(event);
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
        PersistenceManager pm = pmf.getPersistenceManager();

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

    /**
     * Wird von den Benutzer-Apps aufgerufen, wenn der Benutzer noch kein zu verwendendes Konto an-
     * gegeben hat. Es wird außerdem ein Member-Objekt für den Benutzer erstellt, falls es noch nicht
     * existiert.
     * @param user
     * @param regid
     */
    public void registerForGCM(User user, @Named("regid") String regid) throws OAuthRequestException {
        if (user != null) {
            Member m = getMember(user.getEmail());
            if (m == null) {
                createMember(user.getEmail());
            }

            RegistrationManager registrationManager;
            // Den GCM-Manager abrufen, falls er existiert, sonst einen neuen Manager anlegen.
            PersistenceManager pm = pmf.getPersistenceManager();
            try {
                registrationManager = getRegistrationManager(pm);
                registrationManager.addRegistrationId(user.getEmail(), regid);
                pm.makePersistent(registrationManager);
            } finally {
                pm.close();
            }
        }
    }

    /**
     * Ruft über den angegebenen PersistenceManager den RegistrationManager aus der Datenbank ab.
     * @param pm Der zu verwendende PersistenceManager.
     * @return Gibt den RegistrationManager zurück, der in der Datenbank gespeichert ist.
     */
    private RegistrationManager getRegistrationManager(PersistenceManager pm) {
        RegistrationManager registrationManager;
        Query q = pm.newQuery(RegistrationManager.class);
        List<RegistrationManager> result = (List<RegistrationManager>) q.execute();
        if (!result.isEmpty()) {
            registrationManager = result.get(0);
        } else {
            registrationManager = new RegistrationManager();
        }
        return registrationManager;
    }

    /**
     * Ruft die Gruppe mit dem angegebenen Namen ab.
     * @param name Der Name der abzurufenden Gruppe.
     * @return Die UserGroup-Entity.
     */
    private UserGroup getUserGroup(String name) {
        PersistenceManager pm = pmf.getPersistenceManager();

        try {
            return getUserGroup(pm, name);
        } finally {
            pm.close();
        }
    }
    private UserGroup getUserGroup(PersistenceManager pm, String name) {
        Query q = pm.newQuery(UserGroup.class);
        q.setFilter("name == nameParam");
        q.declareParameters("String nameParam");
        List<UserGroup> existingGroups = (List<UserGroup>) q.execute(name);
        if (existingGroups.isEmpty()) {
            return null;
        } else {
            return existingGroups.get(0);
        }
    }

    /**
     * Gibt eine Liste aller auf dem Server gespeicherter Benutzergruppen zurück.
     * @return Eine Liste aller Benutzergruppen.
     */
    public List<UserGroup> getAllUserGroups() {
        PersistenceManager pm = pmf.getPersistenceManager();

        try {
            List<UserGroup> result = (List<UserGroup>) pm.newQuery(UserGroup.class).execute();
            return result;
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt eine Liste aller Benutzergruppen des angegebenen Benutzers zurück.
     * @return Eine Liste aller Benutzergruppen.
     */
    public List<UserGroup> fetchMyUserGroups(User user) throws OAuthRequestException {
        Member member;
        if (user == null || (member = getMember(user.getEmail())) == null) {
            // Falls kein Benutzer angegeben, dann leere Liste zurückgeben.
            return new ArrayList<>();
        }

        PersistenceManager pm = pmf.getPersistenceManager();
        UserGroup ug;
        try {
            List<UserGroup> result = new LinkedList<>();
            List<String> missingGroups = new LinkedList<>();
            for (String g : member.getGroups()) {
                ug = getUserGroup(pm, g);
                if (ug != null) {
                    result.add(ug);
                } else {
                    missingGroups.add(g);
                }
            }
            if (missingGroups.size() > 0) {
                for (String g : missingGroups) {
                    member.getGroups().remove(g);
                }
                pm.makePersistent(member);
            }
            return result;
        } finally {
            pm.close();
        }
    }

    /**
     * Erstellt eine Gruppe mit dem angegebenen Namen
     * @param user Der Benutzer, der die Gruppe anlegen möchte.
     * @param name Der Name der neuen Gruppe.
     */
    public void createUserGroup(User user, @Named("name") String name) throws OAuthRequestException {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        Member member = getMember(user.getEmail());
        if (member == null) {
            throw new IllegalArgumentException("user is not registered");
        }
        if (getUserGroup(name) != null) {
            throw new IllegalArgumentException("group with submitted name already exists");
        }

        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            UserGroup ug = new UserGroup(member);
            ug.setName(name);
            member.addGroup(ug);
            pm.makePersistent(member);
            pm.makePersistent(ug);
            RegistrationManager rm = getRegistrationManager(pm);
            Message m = new Message.Builder()
                    .collapseKey("createUserGroup")
                    .timeToLive(6000)
                    .delayWhileIdle(false)
                    .addData("type", MessageType.GROUP_CREATED_NOTIFICATION_MESSAGE.name())
                    .build();
            Sender s = new Sender(Constants.GCM_API_KEY);
            s.send(m, rm.getRegisteredUser(), 5);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pm.close();
        }
    }

    /**
     * Löscht die Gruppe, falls der aufrufende Benutzer der Ersteller der Gruppe ist.
     * @param user Der Benutzer, der den Löschvorgang durchführen möchte.
     * @param name Der Name, der zu löschenden Gruppe.
     */
    public void deleteUserGroup(User user, @Named("name") String name) throws OAuthRequestException {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        Member member = getMember(user.getEmail());
        if (member == null) {
            throw new IllegalArgumentException("user is not registered");
        }
        UserGroup ug = getUserGroup(name);
        if (ug != null) {
            if (!ug.getCreator().getEmail().equals(user.getEmail())) {
                throw new IllegalArgumentException("user is not creator of group");
            }

            // Benutzer abrufen, die in der Gruppe sind
            Member[] members = new Member[ug.getMembers().size()];
            int index = 0;
            for (String mail : ug.getMembers()) {
                members[index++] = getMember(mail);
            }

            PersistenceManager pm = pmf.getPersistenceManager();
            try {
                Set<String> regids = new HashSet<>();
                RegistrationManager rm = getRegistrationManager(pm);
                for (Member m : members) {
                    m.removeGroup(ug);
                    pm.makePersistent(m);
                    regids.add(rm.getUserIdByMail(m.getEmail()));
                }
                pm.makePersistent(ug);
                pm.deletePersistent(ug);

                // Notification senden
                Sender sender = new Sender(Constants.GCM_API_KEY);
                Message.Builder mb = new Message.Builder()
                        // Nachricht erst anzeigen, wenn der Benutzer sein Handy benutzt
                        .delayWhileIdle(false)
                        .collapseKey("group_" + ug.getName() + "_deleted")
                                // Nachricht verfallen lassen, wenn Benutzer erst nach Event online geht
                        .addData("type", MessageType.GROUP_DELETED_NOTIFICATION_MESSAGE.name())
                        .addData("content", ug.getName())
                        .addData("title", "Eine Gruppe wurde geloescht");
                Message m = mb.build();
                try {
                    sender.send(m, new LinkedList<>(regids), 5);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } finally {
                pm.close();
            }
        }
    }

    /**
     * Fügt den aufrufenden Benutzer zu der angegebenen Gruppe hinzu.
     * @param user Der aufrufende Benutzer.
     * @param groupName Der Name der beizutretenden Gruppe
     */
    public void joinUserGroup(User user, @Named("groupName") String groupName) throws OAuthRequestException {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        Member member = getMember(user.getEmail());
        if (member == null) {
            throw new IllegalArgumentException("user is not registered");
        }

        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            UserGroup ug = getUserGroup(pm, groupName);
            if (ug == null) {
                throw new IllegalArgumentException("a group with the submitted group name does not exist");
            }
            member.addGroup(ug);
            pm.makePersistent(member);
            pm.makePersistent(ug);
        } finally {
            pm.close();
        }
    }

    /**
     * Entfernt den aufrufenden Benutzer aus der angegebenen Gruppe.
     * @param user Der aufrufende Benutzer.
     * @param groupName Der Name der zu verlassenden Gruppe.
     */
    public void leaveUserGroup(User user, @Named("groupName") String groupName) {
        if (user == null) {
            throw new NullPointerException("no user submitted");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("no group name submitted");
        }
        Member member = getMember(user.getEmail());
        if (member == null) {
            throw new IllegalArgumentException("user is not registered");
        }

        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            UserGroup ug = getUserGroup(pm, groupName);
            if (ug == null) {
                throw new IllegalArgumentException("a group with the submitted group name does not exist");
            }
            if (user.getEmail().equals(ug.getCreator().getEmail())) {
                throw new IllegalArgumentException("you can not leave your own group");
            }
            member.removeGroup(ug);
            pm.makePersistent(member);
            pm.makePersistent(ug);
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt eine Liste der Mitglieder der übergebenen Gruppe zurück.
     *
     * @param userGroup
     * @return
     */
    public ArrayList<Member> fetchGroupMembers(@Named("userGroup") String userGroup){
        ArrayList<Member> members = new ArrayList<>();
            UserGroup tmpGroup = getUserGroup(userGroup);
            for(String s : tmpGroup.getMembers()){
                members.add(getMember(s));
            }
        return members;
    }

    /**
     * Dient nur DEBUG-Zwecken. Es kann eine Liste ller GCM-IDs für eine Mail-Adresse abgerufen werden.
     * @param mail
     * @return
     */
    public ArrayList<CharSequence> debugGetIDs(@Named("mail") String mail) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            RegistrationManager rm = getRegistrationManager(pm);
            ArrayList<CharSequence> result = new ArrayList<>();
            result.add(rm.getUserIdByMail(mail));
            return result;
        } finally {
            pm.close();
        }
    }

    /**
     * Dient DEBUG-Zwecken. Schickt die angegebene Nachricht and die angegebene ID als Notification.
     * @param id
     * @param msg
     */
    public void debugSendGCM(@Named("id") String id, @Named("msg") String msg) throws IOException {
        Sender sender = new Sender(Constants.GCM_API_KEY);
        Message m = new Message.Builder()
                .delayWhileIdle(false)
                .timeToLive(3600)
                .addData("type", MessageType.NOTIFICATION_MESSAGE.name())
                .addData("title", "DEBUG")
                .addData("content", msg)
                .build();
        sender.send(m, id, 5);

    }

}
