package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.Member;
import ws1415.SkatenightBackend.model.Route;
import ws1415.SkatenightBackend.model.RoutePoint;
import ws1415.SkatenightBackend.model.UserGroup;

/**
 * Created by Richard on 01.05.2015.
 */
public class RouteEndpoint extends SkatenightServerEndpoint {
    private static final Logger logger = Logger.getLogger(RouteEndpoint.class.getName());

    public static final int FIELD_UPDATE_INTERVAL = 30000;
    private long lastFieldUpdateTime = 0;

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
            Member m = new UserEndpoint().getMember(mail);
            m.setLatitude(latitude);
            m.setLongitude(longitude);
            m.setUpdatedAt(new Date());
            if (m.getCurrentEventId() == null || m.getCurrentEventId() != currentEventId) {
                m.setCurrentEventId(currentEventId);
                m.setCurrentWaypoint(0);
            }

            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
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
            Event event = new EventEndpoint().getEvent(currentEventId);
            int count = Math.min(mail.length, Math.min(latitude.length, longitude.length));

            Member admin = new UserEndpoint().getMember("richard-schulze@online.de");

            PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
            UserGroup group = new GroupEndpoint().getUserGroup(pm, "Simulationsgruppe");
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
            Event event;
            try {
                event = pm.getObjectById(Event.class, eventId);
            } catch(Exception ex) {
                // Falls event nicht gefunden wurde
                event = null;
            }
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
        Event event;
        try {
            event = pm.getObjectById(Event.class, id);
        } catch(Exception ex) {
            // Falls Event nicht gefunden wurde, dann Feldberechnung abbrechen
            return;
        }
        List<RoutePoint> points = event.getRoute().getRoutePoints();
        List<Member> members = new EventEndpoint().getMembersFromEvent(event.getKey().getId());

        logger.info("points.size(): " + points.size());
        // array erstellen welches an der stelle n die Anzahl der Member enthält welche am RoutePoint n sind.
        int memberCountPerRoutePoint[] = new int[points.size()];
        for (Member member : members) {
            if (member.getCurrentEventId() != null && member.getCurrentEventId() == event.getKey().getId() && member.getCurrentWaypoint() != null && member.getCurrentWaypoint() < memberCountPerRoutePoint.length) {
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
     * Speichert die angegebene Route auf dem Server
     *
     * @param user  Der User, der die Route hinzufügen möchte
     * @param route zu speichernde Route
     */
    public void addRoute(User user, Route route) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!new HostEndpoint().isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
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
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

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
        if (!new HostEndpoint().isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        List<Event> eventList = new EventEndpoint().getAllEvents();
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getRoute().getKey().getId() == id) {
                return new BooleanWrapper(false);
            }
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
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

}
