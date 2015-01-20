
package ws1415.veranstalterapp.task;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ws1415.common.util.EventUtil;
import ws1415.veranstalterapp.fragment.ShowEventsFragment;
import ws1415.veranstalterapp.ServiceProvider;



import ws1415.veranstalterapp.task.AuthTaskTestCase;
import ws1415.veranstalterapp.util.EventUtils;
import ws1415.veranstalterapp.util.FieldType;

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
        event1.setDynamicFields(new ArrayList<Field>());

        Field tmpField = new Field();
        tmpField.setType(FieldType.TITLE.getId());
        tmpField.setTitle("Title");
        tmpField.setValue("TestEvent");
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
        tmpField7.setTitle("Describition");
        tmpField7.setValue("Martin stinkt");
        event1.getDynamicFields().add(6, tmpField7);
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
        eventList = (ArrayList<Event>)ServiceProvider.getService().skatenightServerEndpoint().getAllEvents()
                .execute().getItems();
        if (eventList != null) {
            for (Event e : eventList) {
                ServiceProvider.getService().skatenightServerEndpoint().deleteEvent(e.getKey().getId())
                        .execute();
            }
        }

        ServiceProvider.getService().skatenightServerEndpoint().createEvent(event1).execute();
        ServiceProvider.getService().skatenightServerEndpoint().createEvent(event2).execute();

        for( Event e : ServiceProvider.getService().skatenightServerEndpoint().getAllEvents()
                .execute().getItems()){
            if(event1.getDynamicFields().get(0).getValue().equals(e.getDynamicFields().get(0).getValue())){
                event1 = e;
            }
        }
    }


    /**
     * Das Löschen von Events testen.
     */

    public void testTask() throws Exception{
        new DeleteEventTask(new ShowEventsFragment(){
            @Override
            public void deleteEventFromList(Event e){
                // Methode mit leerem Rumpf überschreiben,
                // da der Callback für den Test nicht relevant ist
            }
        }).execute(event1).get();
        for(Event e : ServiceProvider.getService().skatenightServerEndpoint().getAllEvents()
                .execute().getItems()){
            assertNotSame("event1 not existent", event1.getKey().getId(), e.getKey().getId());
        }
    }
}

