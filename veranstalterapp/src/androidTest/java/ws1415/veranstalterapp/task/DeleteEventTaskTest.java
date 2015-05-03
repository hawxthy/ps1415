
package ws1415.veranstalterapp.task;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.util.ArrayList;
import java.util.Date;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.DeleteEventTask;

/**
 * Created by Martin on 21.11.2014.
 */

public class DeleteEventTaskTest extends AuthTaskTestCase {
    private Event event1, event2;
    private Route route1;
    private long time;
    private ArrayList<Event> eventList;


    /**
     * Testdaten erstellen
     */

    public DeleteEventTaskTest() {
        Date date = new Date();
        time = date.getTime();
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


        event1 = new Event();

        event1.getMetaData().setTitle("TestEvent");
        event1.setFee(200);
        event1.getMetaData().setDate(new DateTime(time));
        event1.setMeetingPlace("MS");
        event1.setRoute(route1);
        event1.setDescription(new Text().setValue("Martin"));
        event2 = event1;
    }


    /**
     * Die Daten werden vor jedem Test neu auf den Server geschrieben.
     *
     * @throws Exception
     */

    public void setUp() throws Exception {
        super.setUp();

        // Bestehende Events löschen
        eventList = (ArrayList<Event>) ServiceProvider.getService().eventEndpoint().getAllEvents()
                .execute().getItems();
        if (eventList != null) {
            for (Event e : eventList) {
                ServiceProvider.getService().eventEndpoint().deleteEvent(e.getKey().getId())
                        .execute();
            }
        }

        // Zwei Events erstellen
        ServiceProvider.getService().eventEndpoint().createEvent(event1).execute();
        ServiceProvider.getService().eventEndpoint().createEvent(event2).execute();

        for( Event e : ServiceProvider.getService().eventEndpoint().getAllEvents()
                .execute().getItems()){
            if(event1.getMetaData().getTitle().equals(e.getMetaData().getTitle())){
                event1 = e;
            }
        }
    }


    /**
     * Das Löschen von Events testen.
     */

    public void testTask() throws Exception{
        new DeleteEventTask(null).execute(event1).get();
        for(Event e : ServiceProvider.getService().eventEndpoint().getAllEvents()
                .execute().getItems()){
            assertNotSame("event1 not existent", event1.getKey().getId(), e.getKey().getId());
        }
    }
}

