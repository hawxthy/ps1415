package ws1415.veranstalterapp.task;

import android.test.AndroidTestCase;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.fragment.ShowEventsFragment;
import ws1415.veranstalterapp.util.EventUtils;
import ws1415.veranstalterapp.util.FieldType;

/**
 * Created by Martin Wrodarczyk on 10.11.2014.
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
        event.setDynamicFields(new ArrayList<Field>());
        long time = new Date().getTime();

        Field tmpField = new Field();
        tmpField.setType(FieldType.TITLE.getId());
        tmpField.setTitle("Title");
        tmpField.setValue("Skatenight 1");
        event.getDynamicFields().add(0, tmpField);

        Field tmpField2 = new Field();
        tmpField2.setType(FieldType.FEE.getId());
        tmpField2.setTitle("Fee");
        tmpField2.setValue("2");
        event.getDynamicFields().add(1, tmpField2);

        Field tmpField3 = new Field();
        tmpField3.setType(FieldType.DATE.getId());
        tmpField3.setTitle("Date");
        tmpField3.setValue(Long.toString(time));
        event.getDynamicFields().add(2, tmpField3);

        Field tmpField4 = new Field();
        tmpField4.setType(FieldType.TIME.getId());
        tmpField4.setTitle("Time");

        tmpField4.setValue(Long.toString(time));
        event.getDynamicFields().add(3, tmpField4);

        Field tmpField5 = new Field();
        tmpField5.setType(FieldType.LOCATION.getId());
        tmpField5.setTitle("Location");
        tmpField5.setValue("MS");
        event.getDynamicFields().add(4, tmpField5);

        Field tmpField6 = new Field();
        tmpField6.setType(FieldType.ROUTE.getId());
        tmpField6.setTitle("Map");
        tmpField6.setValue(route.getName());
        event.getDynamicFields().add(5, tmpField6);
        event.setRoute(route);

        Field tmpField7 = new Field();
        tmpField7.setType(FieldType.DESCRIPTION.getId());
        tmpField7.setTitle("Description");
        tmpField7.setValue("Die erste Skatenight");
        event.getDynamicFields().add(6, tmpField7);
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
        ServiceProvider.getService().skatenightServerEndpoint().addRoute(event.getRoute()).execute();
        ServiceProvider.getService().skatenightServerEndpoint().createEvent(event).execute();
    }

    public void testTask() throws ExecutionException, InterruptedException, IOException {
        QueryEventTask task = new QueryEventTask();
        List<Event> eventList = task.execute(new ShowEventsFragment() {
            @Override
            public void setEventsToListView(List<Event> results) {
                // Methode mit leerem Rumpf überschreiben,
                // da der Callback für den Test nicht relevant ist
            }
        }).get();

        assertNotNull("eventList is null", eventList);
        assertTrue("eventList != 1 events", eventList.size() != 1);

        Event serverEvent = eventList.get(0);

        String addedEventTitle = EventUtils.getUniqueField(FieldType.TITLE.getId(), event).getValue();
        long addedEventDate = Long.parseLong(EventUtils.getUniqueField(FieldType.DATE.getId(), event).getValue());
        String addedEventFee = EventUtils.getUniqueField(FieldType.FEE.getId(), event).getValue();
        String addedEventLocation = EventUtils.getUniqueField(FieldType.LOCATION.getId(), event).getValue();
        String addedEventDescription = EventUtils.getUniqueField(FieldType.DESCRIPTION.getId(), event).getValue();

        String serverEventTitle = EventUtils.getUniqueField(FieldType.TITLE.getId(), serverEvent).getValue();
        long serverEventDate = Long.parseLong(EventUtils.getUniqueField(FieldType.DATE.getId(), serverEvent).getValue());
        String serverEventFee = EventUtils.getUniqueField(FieldType.FEE.getId(), serverEvent).getValue();
        String serverEventLocation = EventUtils.getUniqueField(FieldType.LOCATION.getId(), serverEvent).getValue();
        String serverEventDescription = EventUtils.getUniqueField(FieldType.DESCRIPTION.getId(), serverEvent).getValue();

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
        ServiceProvider.getService().skatenightServerEndpoint().deleteEvent(serverEvent.getKey().getId());
        ServiceProvider.getService().skatenightServerEndpoint().deleteRoute(serverEvent.getRoute().getKey()
                .getId()).execute();
    }
}
