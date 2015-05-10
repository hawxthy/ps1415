package ws1415.veranstalterapp.task;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.QueryEventsTask;

//import com.skatenight.skatenightAPI.model.Field;


/**
 * Created by Richard Schulze, Martin Wrodarczyk on 10.11.2014.
 */
public class QueryEventTaskTest extends AuthTaskTestCase {
    private Route route1, route2;
    private Event event1, event2;
    private List<Event> testEvents;
    private long time;

    /**
     * Stellt eine Verbindung zum Testserver her und bereitet die Testdaten vor.
     */
    public QueryEventTaskTest() {
        Date date = new Date();
        time = date.getTime();
        // Testdaten erstellen
        route1 = new Route();
        route1.setName("Route 1");
        route1.setLength("5 km");
        route1.setRouteData(new Text().setValue("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
                "B?B?F?H@F@H@F@FBF@FDHDFDFDDFDFBF@D?FLDNBRBT@LD\\BZBL@^@X@j@At@?|AAd@?@?n@@r@Bb@D" +
                "^PhAVbBJj@Jp@Hj@TrBAr@HbB??yA\\{@XQFQDWHUH_@D]ECA}@Sa@MUGi@My@QC?OCOCI?ODYBUDa@J" +
                "c@Nc@RKFMJQPOTa@d@_BvB_@d@UXSPSLSJOFG@SDKB_@Dw@FQGs@DqBNK@QBO@OAWC_AMMC]Gg@IQCOA" +
                "[CWAa@?m@@e@@mBJI@SBO@c@FSD_@HC@g@L_@L[LOHC@_@PKFWNMHEB??IHIJGFGJEFGJEJA?CFENKd@" +
                "Qp@CHIXK\\[v@Yr@OX??a@q@QYOUIIk@w@a@q@Ua@Qa@S_@??aCoFQa@Q_A??[T??qAqGQkAKm@G_@Im" +
                "@MeAY_F??`AEb@Ah@Hv@Ft@Fl@HP@LA??BA@ABIBMBU@K@I@I@E?EBEBIBGFGJKVUTQRKRIJEn@WJCHC" +
                "FADAHANCHA^EPAPENAFABABAFEHEDGJKRWFG??LENCLAR@NBHYXw@ZaAL_@BMBKDQ@QR_BFq@Di@HO?u" +
                "@???a@Aa@?a@Ag@@U@W@SBOAIBM@AF[HYNa@Rk@Tg@Xa@??R[JWJa@Lk@d@kBF[DY??b@wBNo@FG@EDM" +
                "DIDKNWFI^]HK\\U??RMPMLM@C??Za@HQRe@Tk@l@uAP]Nk@Lo@DSF_@??DuB?Y?YBmAF}@??j@@\\BVB" +
                "VB~ATRBPDPDXDz@F??PRNHJLHRHPRh@DTHT??LZTd@Xf@JNNVb@l@`BdBbBjB??BNhBvB??LJ\\VJ@LP" +
                "j@t@FLXh@Vp@BF??b@vANh@d@fBh@pB@HDXBRBTB^Dj@?BBVCZ"));

        route2 = new Route();
        route2.setName("Route 2");
        route2.setLength("10,3 km");
        route2.setRouteData(new Text().setValue("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
                "B?B?F?H@F@H@F@FBF@FDHDFDFDDFDFBF@D?FLDNBRBT@LD\\BZBL@^@X@j@At@?|AAd@?@?n@@r@Bb@D" +
                "^PhAVbBJj@Jp@Hj@TrBAr@HbB??yA\\{@XQFQDWHUH_@D]ECA}@Sa@MUGi@My@QC?OCOCI?ODYBUDa@J" +
                "c@Nc@RKFMJQPOTa@d@_BvB_@d@UXSPSLSJOFG@SDKB_@Dw@FQGs@DqBNK@QBO@OAWC_AMMC]Gg@IQCOA" +
                "[CWAa@?m@@e@@mBJI@SBO@c@FSD_@HC@g@L_@L[LOHC@_@PKFWNMHEB??IHIJGFGJEFGJEJA?CFENKd@" +
                "Qp@CHIXK\\[v@Yr@OX??a@q@QYOUIIk@w@a@q@Ua@Qa@S_@??aCoFQa@Q_A??[T??qAqGQkAKm@G_@Im" +
                "@MeAY_F??`AEb@Ah@Hv@Ft@Fl@HP@LA??BA@ABIBMBU@K@I@I@E?EBEBIBGFGJKVUTQRKRIJEn@WJCHC" +
                "FADAHANCHA^EPAPENAFABABAFEHEDGJKRWFG??LENCLAR@NBHYXw@ZaAL_@BMBKDQ@QR_BFq@Di@HO?u" +
                "@???a@Aa@?a@Ag@@U@W@SBOAIBM@AF[HYNa@Rk@Tg@Xa@??R[JWJa@Lk@d@kBF[DY??b@wBNo@FG@EDM" +
                "DIDKNWFI^]HK\\U??RMPMLM@C??Za@HQRe@Tk@l@uAP]Nk@Lo@DSF_@??DuB?Y?YBmAF}@??j@@\\BVB" +
                "VB~ATRBPDPDXDz@F??PRNHJLHRHPRh@DTHT??LZTd@Xf@JNNVb@l@`BdBbBjB??BNhBvB??LJ\\VJ@LP" +
                "j@t@FLXh@Vp@BF??b@vANh@d@fBh@pB@HDXBRBTB^Dj@?BBVCZ"));

        event1 = new Event();
        event1.getMetaData().setTitle("Skatenight 1");
        event1.setFee(200);
        event1.getMetaData().setDate(new DateTime(time));
        event1.setMeetingPlace("MS");
        event1.setRoute(route1);
        event1.setDescription(new Text().setValue("Die erste Skatenightt"));

        event2 = new Event();
        event2.getMetaData().setTitle("Skatenight 2");
        event2.setFee(500);
        event2.getMetaData().setDate(new DateTime(time));
        event2.setMeetingPlace("Schlossplatz");
        event2.setRoute(route1);
        event2.setDescription(new Text().setValue("Die zweite Skatenight"));

        testEvents = new ArrayList<Event>();
        testEvents.add(event1);
        testEvents.add(event2);
    }

    /**
     * Überträgt die Testdaten auf den Server.
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();

        // Bestehende Events löschen
        List<Event> events = ServiceProvider.getService().eventEndpoint().getAllEvents()
                .execute().getItems();
        if (events != null) {
            for (Event e : events) {
                ServiceProvider.getService().eventEndpoint().deleteEvent(e.getKey().getId())
                        .execute();
            }
        }

        // Bestehende Routen löschen
        List<Route> routes = ServiceProvider.getService().routeEndpoint().getRoutes()
                .execute().getItems();
        if (routes != null) {
            for (Route r : routes) {
                ServiceProvider.getService().routeEndpoint().deleteRoute(r.getKey()
                        .getId()).execute();
            }
        }

        // Test-Events auf den Server übertragen
        ServiceProvider.getService().routeEndpoint().addRoute(event1.getRoute()).execute();
        ServiceProvider.getService().routeEndpoint().addRoute(event2.getRoute()).execute();
        ServiceProvider.getService().eventEndpoint().createEvent(event1).execute();
        ServiceProvider.getService().eventEndpoint().createEvent(event2).execute();
    }


    /**
     * Prüft, ob das durch den QueryEventTask abgerufene Event die korrekten Daten enthält.
     *
     * @throws java.util.concurrent.ExecutionException
     * @throws InterruptedException
     */
    public void testTask() throws ExecutionException, InterruptedException, IOException {
        QueryEventsTask task = new QueryEventsTask(null);
        List<Event> eventList = task.execute().get();

        assertNotNull("events are null", eventList);
        int testIndex = -1;
        for (int i = 0; i < eventList.size(); i++) {
            String event1Title = event1.getMetaData().getTitle();
            String selEventTitle = eventList.get(i).getMetaData().getTitle();

            // Passendes Event zum Vergleich über den Titel auswählen
            if (event1Title.equals(selEventTitle)) {
                // Event 1
                testIndex = 0;
            } else {
                // Event 2
                testIndex = 1;
            }

            String selectedEventTitle = testEvents.get(testIndex).getMetaData().getTitle();
            long selectedEventDate = testEvents.get(testIndex).getMetaData().getDate().getValue();
            int selectedEventFee = testEvents.get(testIndex).getFee();
            String selectedEventLocation = testEvents.get(testIndex).getMeetingPlace();
            String selectedEventDescription = testEvents.get(testIndex).getDescription().getValue();

            String serverEventTitle = eventList.get(i).getMetaData().getTitle();
            long serverEventDate = eventList.get(i).getMetaData().getDate().getValue();
            int serverEventFee = eventList.get(i).getFee();
            String serverEventLocation = eventList.get(i).getMeetingPlace();
            String serverEventDescription = eventList.get(i).getDescription().getValue();

            assertEquals(i + ".event: wrong title", serverEventTitle, selectedEventTitle);
            // Datum kann Millisekunden abweichen, diese Abweichung zulassen
            assertTrue(i + ".event: wrong date", serverEventDate > selectedEventDate -20 && serverEventDate < selectedEventDate + 20);
            assertEquals(i + ".event: wrong fee", serverEventFee, selectedEventFee);
            assertEquals(i + ".event: wrong location", serverEventLocation, selectedEventLocation);
            assertEquals(i + ".event: wrong description", serverEventDescription, selectedEventDescription);
            assertNotNull(i + ".event: route is null", eventList.get(i).getRoute());
            assertEquals(i + ".event: wrong route name", eventList.get(i).getRoute().getName(), testEvents.get(testIndex).getRoute().getName());
            assertEquals(i + ".event: wrong route length", eventList.get(i).getRoute().getLength(), testEvents.get(testIndex).getRoute().getLength());
            assertNotNull(i + ".event: route data is null", eventList.get(i).getRoute().getRouteData());
            assertEquals(i + ".event: wrong route data", eventList.get(i).getRoute().getRouteData().getValue(),
                    testEvents.get(testIndex).getRoute().getRouteData().getValue());
        }

        // Bestehende Events löschen
        List<Event> events = ServiceProvider.getService().eventEndpoint().getAllEvents()
                .execute().getItems();
        if (events != null) {
            for (Event e : events) {
                ServiceProvider.getService().eventEndpoint().deleteEvent(e.getKey().getId())
                        .execute();
            }
        }

        // Bestehende Routen löschen
        List<Route> routes = ServiceProvider.getService().routeEndpoint().getRoutes()
                .execute().getItems();
        if (routes != null) {
            for (Route r : routes) {
                ServiceProvider.getService().routeEndpoint().deleteRoute(r.getKey()
                        .getId()).execute();
            }
        }

    }
}


