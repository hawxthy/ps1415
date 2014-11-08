package ws1415.ps1415.task;

import android.test.AndroidTestCase;

import com.appspot.skatenight_ms.skatenightAPI.model.Event;
import com.appspot.skatenight_ms.skatenightAPI.model.Route;
import com.appspot.skatenight_ms.skatenightAPI.model.Text;
import com.google.api.client.util.DateTime;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.ShowInformationActivity;
import ws1415.ps1415.task.QueryEventTask;

/**
 * TestCase für QueryEventTask.
 * Created by Richard on 06.11.2014.
 */
public class QueryEventTaskTest extends AndroidTestCase {
    private Event testEvent;
    private Route testRoute;

    /**
     * Stellt eine Verbindung zum Testserver her und bereitet die Testdaten vor.
     */
    public QueryEventTaskTest() {
        ServiceProvider.setupTestServerConnection();

        // Testdaten erstellen
        testEvent = new Event();
        testEvent.setTitle("Testtitel");
        testEvent.setDate(new DateTime(new Date()));
        testEvent.setFee("3");
        testEvent.setLocation("Testort");
        testEvent.setDescription(new Text().setValue("Lorem ipsum dolor sit amet, consetetur " +
                "sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore " +
                "magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo " +
                "dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est " +
                "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing " +
                "elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna " +
                "aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores " +
                "et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum " +
                "dolor sit amet."));
        testRoute = new Route();
        testRoute.setName("Testroute");
        testRoute.setLength("10 km");
        testRoute.setRouteData(new Text().setValue("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
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
        testEvent.setRoute(testRoute);
    }

    /**
     * Überträgt die Testdaten auf den Server.
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();

        // Test-Event setzen
        ServiceProvider.getService().skatenightServerEndpoint().setEventTestMethod(testEvent).execute();
    }

    /**
     * Prüft, ob das durch den QueryEventTask abgerufene Event die korrekten Daten enthält.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void testTask() throws ExecutionException, InterruptedException {
        QueryEventTask task = new QueryEventTask();
        Event event = task.execute(new ShowInformationActivity() {
            @Override
            public void setEventInformation(Event e) {
                // Methode mit leerem Rumpf überschreiben,
                // da der Callback für den Test nicht relevant ist
            }
        }).get();
        assertNotNull("event is null", event);
        assertEquals("wrong title", testEvent.getTitle(), event.getTitle());
        assertEquals("wrong date", testEvent.getDate(), event.getDate());
        assertEquals("wrong fee", testEvent.getFee(), event.getFee());
        assertEquals("wrong location", testEvent.getLocation(), event.getLocation());
        assertNotNull("event description is null", event.getDescription());
        assertEquals("wrong description", testEvent.getDescription().getValue(),
                event.getDescription().getValue());
        assertNotNull("route is null", event.getRoute());
        assertEquals("wrong route name", testRoute.getName(), event.getRoute().getName());
        assertEquals("wrong route length", testRoute.getLength(), event.getRoute().getLength());
        assertNotNull("route data is null", event.getRoute().getRouteData());
        assertEquals("wrong route data", testRoute.getRouteData().getValue(),
                event.getRoute().getRouteData().getValue());
    }
}
