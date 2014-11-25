package ws1415.veranstalterapp.task;

import android.test.AndroidTestCase;

import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ws1415.veranstalterapp.fragment.ManageRoutesFragment;
import ws1415.veranstalterapp.ServiceProvider;

/**
 * Created by Richard Schulze on 10.11.2014.
 */
public class QueryRouteTaskTest extends AndroidTestCase {
    private Route testRoute1, testRoute2, testRoute3;

    /**
     * Testdaten erstellen.
     */
    public QueryRouteTaskTest() {
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

        testRoute2 = new Route();
        testRoute2.setName("Route 2");
        testRoute2.setLength("10,3 km");
        testRoute2.setRouteData(new Text().setValue("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
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

        testRoute3 = new Route();
        testRoute3.setName("Route 3");
        testRoute3.setLength("9,05 km");
        testRoute3.setRouteData(new Text().setValue("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
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
     * Die Daten werden vor jedem Test neu auf den Server geschrieben.
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();

        // Bestehende Routen löschen
        for (Route r : ServiceProvider.getService().skatenightServerEndpoint().getRoutes().execute()
                .getItems()) {
            ServiceProvider.getService().skatenightServerEndpoint().deleteRoute(r.getKey().getId())
                    .execute();
        }
        // Testrouten auf den Server schreiben
        ServiceProvider.getService().skatenightServerEndpoint().addRoute(testRoute1).execute();
        ServiceProvider.getService().skatenightServerEndpoint().addRoute(testRoute2).execute();
        ServiceProvider.getService().skatenightServerEndpoint().addRoute(testRoute3).execute();
    }

    /**
     * Testet, ob die Routen vollständig und korrekt abgerufen werden.
     */
    public void testTask() throws ExecutionException, InterruptedException {
        QueryRouteTask task = new QueryRouteTask();
        List<Route> routes = task.execute(new ManageRoutesFragment() {
            public void setRoutesToListView(ArrayList<Route> results) {
                // Methode mit leerem Rumpf überschreiben,
                // da der Callback für den Test nicht relevant ist
            }
        }).get();
        // Abgerufene Routen den Testrouten zuordnen
        Route route1 = null, route2 = null, route3 = null;
        for (Route r : routes) {
            if (testRoute1.getName().equals(r.getName())) {
                route1 = r;
            } else if (testRoute2.getName().equals(r.getName())) {
                route2 = r;
            } else if (testRoute3.getName().equals(r.getName())) {
                route3 = r;
            }
        }
        // Route 1 testen
        assertNotNull("route1 not found", route1);
        assertEquals("route1: wrong name", testRoute1.getName(), route1.getName());
        assertEquals("route1: wrong length", testRoute1.getLength(), route1.getLength());
        assertNotNull("route1: no route data", route1.getRouteData());
        assertEquals("route1: wrong route data", testRoute1.getRouteData().getValue(),
                route1.getRouteData().getValue());
        // Route 2 testen
        assertNotNull("route2 not found", route2);
        assertEquals("route2: wrong name", testRoute2.getName(), route2.getName());
        assertEquals("route2: wrong length", testRoute2.getLength(), route2.getLength());
        assertNotNull("route2: no route data", route2.getRouteData());
        assertEquals("route2: wrong route data", testRoute2.getRouteData().getValue(),
                route2.getRouteData().getValue());
        // Route 3 testen
        assertNotNull("route3 not found", route3);
        assertEquals("route3: wrong name", testRoute3.getName(), route3.getName());
        assertEquals("route3: wrong length", testRoute3.getLength(), route3.getLength());
        assertNotNull("route3: no route data", route3.getRouteData());
        assertEquals("route3: wrong route data", testRoute3.getRouteData().getValue(),
                route3.getRouteData().getValue());
    }

}


