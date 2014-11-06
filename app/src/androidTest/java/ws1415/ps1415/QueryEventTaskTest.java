package ws1415.ps1415;

import android.accounts.Account;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.util.Log;

import com.appspot.skatenight_ms.skatenightAPI.model.Event;
import com.appspot.skatenight_ms.skatenightAPI.model.Route;
import com.appspot.skatenight_ms.skatenightAPI.model.Text;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * TestCase für QueryEventTask, der die ShowInformationActivity für einen gültigen ApplicationContext
 * nutzt.
 * Created by Richard on 06.11.2014.
 */
public class QueryEventTaskTest extends ActivityInstrumentationTestCase2<ShowInformationActivity> {
    private ShowInformationActivity activity;
    private Event testEvent;
    private Route testRoute;

    public QueryEventTaskTest() throws IOException {
        super(ShowInformationActivity.class);

        // Testdaten erstellen
        testEvent = new Event();
        testEvent.setTitle("Testtitel");
        testEvent.setDate(new DateTime(new Date()));
        testEvent.setFee("3,-- €");
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

    public void setUp() throws Exception {
        super.setUp();

        ServiceProvider.setupTestServerConnection(null);
        ServiceProvider.getService().skatenightServerEndpoint().addHost("example@example.com").execute();

        // Test-Event setzen
        ServiceProvider.getService().skatenightServerEndpoint().setEvent(testEvent).execute();
    }

    public void testTask() throws ExecutionException, InterruptedException {
        QueryEventTask task = new QueryEventTask();
        Event event = task.execute(new ShowInformationActivity()).get();
        assertNotNull("event is null", event);
        assertEquals("wrong title", testEvent.getTitle(), event.getTitle());
        assertEquals("wrong date", testEvent.getDate(), event.getDate());
        assertEquals("wrong fee", testEvent.getFee(), event.getFee());
        assertEquals("wrong location", testEvent.getLocation(), event.getLocation());
        assertEquals("wrong description", testEvent.getDescription().getValue(),
                event.getDescription().getValue());
        assertEquals("wrong route name", testRoute.getName(), event.getRoute().getName());
        assertEquals("wrong route length", testRoute.getLength(), event.getRoute().getLength());
        assertEquals("wrong route data", testRoute.getRouteData(), event.getRoute().getRouteData());
    }
}
