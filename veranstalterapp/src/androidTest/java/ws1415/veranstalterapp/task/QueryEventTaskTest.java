package ws1415.veranstalterapp.task;

import android.test.AndroidTestCase;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ws1415.common.util.EventUtil;
import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.fragment.ShowEventsFragment;
import ws1415.veranstalterapp.util.EventUtils;
import ws1415.veranstalterapp.util.FieldType;


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
        event1.setDynamicFields(new ArrayList<Field>());

        Field tmpField = new Field();
        tmpField.setType(FieldType.TITLE.getId());
        tmpField.setTitle("Title");
        tmpField.setValue("Skatenight 1");
        event1.getDynamicFields().add(0, tmpField);

        Field tmpField2 = new Field();
        tmpField2.setType(FieldType.FEE.getId());
        tmpField2.setTitle("Fee");
        tmpField2.setValue("2");
        event1.getDynamicFields().add(1, tmpField2);

        Field tmpField3 = new Field();
        tmpField3.setType(FieldType.DATE.getId());
        tmpField3.setTitle("Date");
        tmpField3.setValue(Long.toString(time));
        event1.getDynamicFields().add(2, tmpField3);

        Field tmpField4 = new Field();
        tmpField4.setType(FieldType.TIME.getId());
        tmpField4.setTitle("Time");

        tmpField4.setValue(Long.toString(time));
        event1.getDynamicFields().add(3, tmpField4);

        Field tmpField5 = new Field();
        tmpField5.setType(FieldType.LOCATION.getId());
        tmpField5.setTitle("Location");
        tmpField5.setValue("MS");
        event1.getDynamicFields().add(4, tmpField5);

        Field tmpField6 = new Field();
        tmpField6.setType(FieldType.ROUTE.getId());
        tmpField6.setTitle("Map");
        tmpField6.setValue(route1.getName());
        event1.getDynamicFields().add(5, tmpField6);
        event1.setRoute(route1);

        Field tmpField7 = new Field();
        tmpField7.setType(FieldType.DESCRIPTION.getId());
        tmpField7.setTitle("Description");
        tmpField7.setValue("Die erste Skatenightt");
        event1.getDynamicFields().add(6, tmpField7);


        event2 = new Event();
        event2.setDynamicFields(new ArrayList<Field>());

        tmpField = new Field();
        tmpField.setType(FieldType.TITLE.getId());
        tmpField.setTitle("Title");
        tmpField.setValue("Skatenight 2");
        event2.getDynamicFields().add(0, tmpField);

        tmpField2 = new Field();
        tmpField2.setType(FieldType.FEE.getId());
        tmpField2.setTitle("Fee");
        tmpField2.setValue("5");
        event2.getDynamicFields().add(1, tmpField2);

        tmpField3 = new Field();
        tmpField3.setType(FieldType.DATE.getId());
        tmpField3.setTitle("Date");
        tmpField3.setValue(Long.toString(time));
        event2.getDynamicFields().add(2, tmpField3);

        tmpField4 = new Field();
        tmpField4.setType(FieldType.TIME.getId());
        tmpField4.setTitle("Time");

        tmpField4.setValue(Long.toString(time));
        event2.getDynamicFields().add(3, tmpField4);

        tmpField5 = new Field();
        tmpField5.setType(FieldType.LOCATION.getId());
        tmpField5.setTitle("Location");
        tmpField5.setValue("Schlossplatz");
        event2.getDynamicFields().add(4, tmpField5);

        tmpField6 = new Field();
        tmpField6.setType(FieldType.ROUTE.getId());
        tmpField6.setTitle("Map");
        tmpField6.setValue(route2.getName());
        event2.getDynamicFields().add(5, tmpField6);
        event2.setRoute(route1);

        tmpField7 = new Field();
        tmpField7.setType(FieldType.DESCRIPTION.getId());
        tmpField7.setTitle("Description");
        tmpField7.setValue("Daie zweite Skatenight");
        event2.getDynamicFields().add(6, tmpField7);


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
        List<Event> events = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents()
                .execute().getItems();
        if (events != null) {
            for (Event e : events) {
                ServiceProvider.getService().skatenightServerEndpoint().deleteEvent(e.getKey().getId())
                        .execute();
            }
        }

        // Bestehende Routen löschen
        List<Route> routes = ServiceProvider.getService().skatenightServerEndpoint().getRoutes()
                .execute().getItems();
        if (routes != null) {
            for (Route r : routes) {
                ServiceProvider.getService().skatenightServerEndpoint().deleteRoute(r.getKey()
                        .getId()).execute();
            }
        }

        // Test-Events auf den Server übertragen
        ServiceProvider.getService().skatenightServerEndpoint().addRoute(event1.getRoute()).execute();
        ServiceProvider.getService().skatenightServerEndpoint().addRoute(event2.getRoute()).execute();
        ServiceProvider.getService().skatenightServerEndpoint().createEvent(event1).execute();
        ServiceProvider.getService().skatenightServerEndpoint().createEvent(event2).execute();
    }


    /**
     * Prüft, ob das durch den QueryEventTask abgerufene Event die korrekten Daten enthält.
     *
     * @throws java.util.concurrent.ExecutionException
     * @throws InterruptedException
     */
    public void testTask() throws ExecutionException, InterruptedException, IOException {
        QueryEventTask task = new QueryEventTask();
        List<Event> eventList = task.execute(new ShowEventsFragment() {
            @Override
            public void setEventsToListView(List<Event> results) {
                // Methode mit leerem Rumpf überschreiben,
                // da der Callback für den Test nicht relevant ist
            }
        }).get();

        assertNotNull("events are null", eventList);
        int testIndex = -1;
        for (int i = 0; i < eventList.size(); i++) {
            String event1Title = EventUtils.getUniqueField(FieldType.TITLE.getId(), event1).getValue();
            String selEventTitle = EventUtils.getUniqueField(FieldType.TITLE.getId(), eventList.get(i)).getValue();

            // Passendes Event zum Vergleich über den Titel auswählen
            if (event1Title.equals(selEventTitle)) {
                // Event 1
                testIndex = 0;
            } else {
                // Event 2
                testIndex = 1;
            }

            String selectedEventTitle = EventUtils.getUniqueField(FieldType.TITLE.getId(), testEvents.get(testIndex)).getValue();
            long selectedEventDate = Long.parseLong(EventUtils.getUniqueField(FieldType.DATE.getId(), testEvents.get(testIndex)).getValue());
            String selectedEventFee = EventUtils.getUniqueField(FieldType.FEE.getId(), testEvents.get(testIndex)).getValue();
            String selectedEventLocation = EventUtils.getUniqueField(FieldType.LOCATION.getId(), testEvents.get(testIndex)).getValue();
            String selectedEventDescription = EventUtils.getUniqueField(FieldType.DESCRIPTION.getId(), testEvents.get(testIndex)).getValue();

            String serverEventTitle = EventUtils.getUniqueField(FieldType.TITLE.getId(), eventList.get(i)).getValue();
            long serverEventDate = Long.parseLong(EventUtils.getUniqueField(FieldType.DATE.getId(), eventList.get(i)).getValue());
            String serverEventFee = EventUtils.getUniqueField(FieldType.FEE.getId(), eventList.get(i)).getValue();
            String serverEventLocation = EventUtils.getUniqueField(FieldType.LOCATION.getId(), eventList.get(i)).getValue();
            String serverEventDescription = EventUtils.getUniqueField(FieldType.DESCRIPTION.getId(), eventList.get(i)).getValue();

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
        List<Event> events = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents()
                .execute().getItems();
        if (events != null) {
            for (Event e : events) {
                ServiceProvider.getService().skatenightServerEndpoint().deleteEvent(e.getKey().getId())
                        .execute();
            }
        }

        // Bestehende Routen löschen
        List<Route> routes = ServiceProvider.getService().skatenightServerEndpoint().getRoutes()
                .execute().getItems();
        if (routes != null) {
            for (Route r : routes) {
                ServiceProvider.getService().skatenightServerEndpoint().deleteRoute(r.getKey()
                        .getId()).execute();
            }
        }

    }
}


