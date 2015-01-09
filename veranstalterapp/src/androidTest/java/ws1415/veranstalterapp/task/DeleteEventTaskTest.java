/*
package ws1415.veranstalterapp.task;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import ws1415.veranstalterapp.fragment.ShowEventsFragment;
import ws1415.veranstalterapp.ServiceProvider;

*/
/**
 * Created by Martin on 21.11.2014.
 *//*

public class DeleteEventTaskTest extends AuthTaskTestCase {
    private Event event1, event2;
    private Route route1, route2;

    */
/**
     * Testdaten erstellen
     *//*

    public DeleteEventTaskTest() {
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
    }

    */
/**
     * Die Daten werden vor jedem Test neu auf den Server geschrieben.
     *
     * @throws Exception
     *//*

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

        ServiceProvider.getService().skatenightServerEndpoint().createEvent(event1).execute();
        ServiceProvider.getService().skatenightServerEndpoint().createEvent(event2).execute();

        for( Event e : ServiceProvider.getService().skatenightServerEndpoint().getAllEvents()
                .execute().getItems()){
            if(event1.getTitle().equals(e.getTitle())){
                event1 = e;
            } else if(event2.getTitle().equals(e.getTitle())){
                event2 = e;
            }
        }
    }

    */
/**
     * Das Löschen von Events testen.
     *//*

    public void testTask() throws IOException{
        new DeleteEventTask(new ShowEventsFragment(){
            @Override
            public void deleteEventFromList(Event e){
                // Methode mit leerem Rumpf überschreiben,
                // da der Callback für den Test nicht relevant ist
            }
        }).execute(event2);
        for(Event e : ServiceProvider.getService().skatenightServerEndpoint().getAllEvents()
                .execute().getItems()){
            assertNotSame("event2 not deleted", event2.getKey().getId(), e.getKey().getId());
        }
    }
}
*/
