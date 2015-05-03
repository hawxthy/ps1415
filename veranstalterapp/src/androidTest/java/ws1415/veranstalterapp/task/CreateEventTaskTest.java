package ws1415.veranstalterapp.task;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.QueryEventsTask;

/**
 * Created by Martin Wrodarczyk  on 10.11.2014.
 */
public class CreateEventTaskTest extends AuthTaskTestCase {
    private Route route;
    private Event event;

    public CreateEventTaskTest() {
        route = new Route();
        route.setName("Route 1");
        route.setLength("5 km");
        route.setRouteData(new Text().setValue("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
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

        event = new Event();
        long time = new Date().getTime();

        event.getMetaData().setTitle("Skatenight 1");
        event.setFee(200);
        event.getMetaData().setDate(new DateTime(time));
        event.setMeetingPlace("MS");
        event.setRoute(route);
        event.setDescription(new Text().setValue("Die erste Skatenight"));
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
        ServiceProvider.getService().routeEndpoint().addRoute(event.getRoute()).execute();
        ServiceProvider.getService().eventEndpoint().createEvent(event).execute();
    }

    public void testTask() throws ExecutionException, InterruptedException, IOException {
        QueryEventsTask task = new QueryEventsTask(null);
        List<Event> eventList = task.execute().get();

        assertNotNull("eventList is null", eventList);
        assertTrue("eventList != 1 events", eventList.size() == 1);

        Event serverEvent = eventList.get(0);

        String addedEventTitle = event.getMetaData().getTitle();
        long addedEventDate = event.getMetaData().getDate().getValue();
        int addedEventFee = event.getFee();
        String addedEventLocation = event.getMeetingPlace();
        String addedEventDescription = event.getDescription().getValue();

        String serverEventTitle = serverEvent.getMetaData().getTitle();
        long serverEventDate = serverEvent.getMetaData().getDate().getValue();
        int serverEventFee = serverEvent.getFee();
        String serverEventLocation = serverEvent.getMeetingPlace();
        String serverEventDescription = serverEvent.getDescription().getValue();

        assertEquals("wrong title", serverEventTitle, addedEventTitle);
        // Datum kann Millisekunden abweichen, diese Abweichung zulassen
        assertTrue("wrong date", serverEventDate > addedEventDate - 20 && serverEventDate < addedEventDate + 20);
        assertEquals("wrong fee", serverEventFee, addedEventFee);
        assertEquals("wrong location", serverEventLocation, addedEventLocation);
        assertEquals("wrong description", serverEventDescription, addedEventDescription);
        assertNotNull("route is null", serverEvent);
        assertEquals("wrong route name", serverEvent.getRoute().getName(), event.getRoute().getName());
        assertEquals("wrong route length", serverEvent.getRoute().getLength(), event.getRoute().getLength());
        assertEquals("wrong route data", serverEvent.getRoute().getRouteData().getValue(),
                event.getRoute().getRouteData().getValue());

        // Route und Event löschen
        ServiceProvider.getService().eventEndpoint().deleteEvent(serverEvent.getKey().getId()).execute();
        ServiceProvider.getService().routeEndpoint().deleteRoute(serverEvent.getRoute().getKey()
                .getId()).execute();
    }
}
