package ws1415.veranstalterapp.task;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.AddRouteTask;

/**
 * Created by Richard Schulze on 10.11.2014.
 */
public class AddRouteTaskTest extends AuthTaskTestCase {
    private Route testRoute1;

    /**
     * Testdaten erstellen und einloggen.
     */
    public AddRouteTaskTest() {
        testRoute1 = new Route();
        testRoute1.setName("Route 1");
        testRoute1.setLength("5 km");
        testRoute1.setRouteData(new Text().setValue("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
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
    }

    /**
     * Verbindung zum Testserver aufbauen und bestehende Routen löschen.
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();

        // Bestehende Events löschen
        List<Event> events = ServiceProvider.getService().eventEndpoint().getAllEvents()
                .execute().getItems();
        if (events != null) {
            for (Event e : events) {
                ServiceProvider.getService().eventEndpoint().deleteEvent(e.getId())
                        .execute();
            }
        }

        // Bestehende Routen löschen
        List<Route> routes = ServiceProvider.getService().routeEndpoint().getRoutes()
                .execute().getItems();
        if (routes != null) {
            for (Route r : routes) {
                ServiceProvider.getService().routeEndpoint().deleteRoute(r.getId()).execute();
            }
        }
    }

    /**
     * Testet, ob die Route vollständig und korrekt auf dem Server gespeichert wird.
     */
    public void testTask() throws IOException, ExecutionException, InterruptedException {
        new AddRouteTask(null).execute(testRoute1).get();
        // Route wieder abrufen und mit lokaler Route vergleichen
        List<Route> routes = ServiceProvider.getService().routeEndpoint().getRoutes()
                .execute().getItems();
        assertNotNull("no routes on server", routes);
        assertEquals("wrong route count", 1, routes.size());
        Route r = routes.get(0);
        assertNotNull("route is null", r);
        assertEquals("wrong name", testRoute1.getName(), r.getName());
        assertEquals("wrong length", testRoute1.getLength(), r.getLength());
        assertNotNull("no route data", r.getRouteData());
        assertEquals("wrong route data", testRoute1.getRouteData().getValue(),
                r.getRouteData().getValue());
    }

}
