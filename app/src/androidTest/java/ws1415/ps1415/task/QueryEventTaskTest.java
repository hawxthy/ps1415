package ws1415.ps1415.task;

import android.test.AndroidTestCase;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ws1415.ps1415.activity.ShowEventsActivity;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.task.QueryEventTask;

/**
 * Created by Richard Schulze, Martin Wrodarczyk on 10.11.2014.
 */
public class QueryEventTaskTest extends AndroidTestCase {
    private Route route1, route2;
    private Event event1, event2;
    private List<Event> testEvents;
    /**
     * Stellt eine Verbindung zum Testserver her und bereitet die Testdaten vor.
     */
    public QueryEventTaskTest() {
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
        event1.setTitle("Skatenight 1");
        event1.setFee("2");
        event1.setDate(new DateTime(new Date()));
        event1.setLocation("MS");
        event1.setRoute(route1);
        event1.setDescription(new Text().setValue("Die erste Skatenight"));

        event2 = new Event();
        event2.setTitle("Skatenight 2");
        event2.setFee("5");
        event2.setDate(new DateTime(new Date()));
        event2.setLocation("Schlossplatz");
        event2.setRoute(route2);
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

        // Test-Events auf den Server übertragen
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
        List<Event> eventList = task.execute(new ShowEventsActivity() {
            @Override
            public void setEventsToListView(List<Event> results) {
                // Methode mit leerem Rumpf überschreiben,
                // da der Callback für den Test nicht relevant ist
            }
        }).get();

        assertNotNull("events are null", eventList);
        for (int i = 0; i < eventList.size(); i++) {
            assertEquals(i + ".event: wrong title", eventList.get(i).getTitle(), testEvents.get(i).getTitle());
            assertEquals(i + ".event: wrong date", eventList.get(i).getDate().getValue(), testEvents.get(i).getDate().getValue());
            assertEquals(i + ".event: wrong fee", eventList.get(i).getFee(), testEvents.get(i).getFee());
            assertEquals(i + ".event: wrong location", eventList.get(i).getLocation(), testEvents.get(i).getLocation());
            assertNotNull(i + ".event: event description is null", testEvents.get(i).getDescription());
            assertEquals(i + ".event: wrong description", eventList.get(i).getDescription().getValue(),
                    testEvents.get(i).getDescription().getValue());
            assertNotNull(i + ".event: route is null", eventList.get(i).getRoute());
            assertEquals(i + ".event: wrong route name", eventList.get(i).getRoute().getName(), testEvents.get(i).getRoute().getName());
            assertEquals(i + ".event: wrong route length", eventList.get(i).getRoute().getLength(), testEvents.get(i).getRoute().getLength());
            assertNotNull(i + ".event: route data is null", eventList.get(i).getRoute().getRouteData());
            assertEquals(i + ".event: wrong route data", eventList.get(i).getRoute().getRouteData().getValue(),
                    testEvents.get(i).getRoute().getRouteData().getValue());
        }

    }
}
