package ws1415.veranstalterapp.task;

import android.util.Log;

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

import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.util.EventUtils;
import ws1415.veranstalterapp.util.FieldType;

/**
 * Created by Bernd Eissing on 21.11.2014.
 */
public class EditEventTaskTest extends AuthTaskTestCase {
    //Test deaktiviert, da Events noch angepasst werden
    private Route route1;
    private Route route2;
    private Event event1;
    private long time;

    /**
     * Erstellt 2 Routen und ein Event. Wobei dem Event eine der beiden Routen
     * zugeordnet wird. Dient zum Initialisieren der Objekte
     */
    public EditEventTaskTest() {
        Date date = new Date();
        time = date.getTime();
        // Erstellle die Route für das Event
        route1 = new Route();
        route1.setName("testroute");
        route1.setLength("7km");
        route1.setRouteData(new Text().setValue(("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
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
                "j@t@FLXh@Vp@BF??b@vANh@d@fBh@pB@HDXBRBTB^Dj@?BBVCZ")));
        route2 = new Route();
        route2.setName("testroute2");
        route2.setLength("5km");
        route2.setRouteData(new Text().setValue(("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
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
                "j@t@FLXh@Vp@BF??b@vANh@d@fBh@pB@HDXBRBTB^Dj@?BBVCZ")));

        // Erstelle das zu testende Event
        event1 = new Event();
        event1.setDynamicFields(new ArrayList<Field>());

        Field tmpField = new Field();
        tmpField.setType(FieldType.TITLE.getId());
        tmpField.setTitle("Title");
        tmpField.setValue("Testevent");
        event1.getDynamicFields().add(0, tmpField);

        Field tmpField2 = new Field();
        tmpField2.setType(FieldType.FEE.getId());
        tmpField2.setTitle("Fee");
        tmpField2.setValue("7");
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
        tmpField5.setValue("Münster");
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
        tmpField7.setValue("Das ist ein Testevent");
        event1.getDynamicFields().add(6, tmpField7);
    }

    /**
     * Stellt die Verdindung zum Server her und speichert dort das Testevent
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();

        // Alle Events löschen
        List<Event> events = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents()
                .execute().getItems();
        if (events != null) {
            for (Event e : events) {
                ServiceProvider.getService().skatenightServerEndpoint().deleteEvent(e.getKey().getId())
                        .execute();
            }
        }

        // Das oben erstellte Event auf dem Server speichern
        ServiceProvider.getService().skatenightServerEndpoint().createEvent(event1).execute();
        event1 = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents().execute().getItems().get(0);
        // Ggfs. warten, bis die Datenbank auf dem Server aktualisiert wurde
        while (event1 == null) {
            event1 = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents().execute().getItems().get(0);
            Thread.sleep(500);
        }
    }

    /**
     * Verändert alle Attribute von Event, ruft dann den EditEventTask auf und
     * überprüft anschließend auf nicht oder falsch übernommene Attribute.
     *
     * @throws IOException
     */
    public void testTask() throws IOException, ExecutionException, InterruptedException {
        // Ändere alle Daten vom event
        for (int i = 0; i < event1.getDynamicFields().size(); i++) {
            if (event1.getDynamicFields().get(i).getType() == FieldType.TITLE.getId()) {
                event1.getDynamicFields().get(i).setValue("EditEvent");
            } else if (event1.getDynamicFields().get(i).getType() == FieldType.FEE.getId()) {
                event1.getDynamicFields().get(i).setValue("2");
            } else if (event1.getDynamicFields().get(i).getType() == FieldType.LOCATION.getId()) {
                event1.getDynamicFields().get(i).setValue("MS");
            } else if (event1.getDynamicFields().get(i).getType() == FieldType.DESCRIPTION.getId()) {
                event1.getDynamicFields().get(i).setValue("Das ist das veränderte Testevent");
            } else if (event1.getDynamicFields().get(i).getType() == FieldType.ROUTE.getId()) {
                event1.getDynamicFields().get(i).setValue(route2.getName());
                event1.setRoute(route2);
            }
        }

        Log.d("TESTS", "" + new EditEventTask().execute(event1).get());

        // Überprüfen
        Log.d("TESTS", event1.getKey().toPrettyString());
        // Event testEvent = ServiceProvider.getService().skatenightServerEndpoint().getEvent(event.getKey().getId()).execute();
        Event testEvent = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents().execute().getItems().get(0);

        for (int i = 0; i < event1.getDynamicFields().size(); i++) {
            if (event1.getDynamicFields().get(i).getType() == FieldType.TITLE.getId()) {
                for (int j = 0; j < testEvent.getDynamicFields().size(); j++) {
                    if (testEvent.getDynamicFields().get(j).getType() == FieldType.TITLE.getId()) {
                        assertEquals("Der Title stimmt nicht überein", event1.getDynamicFields().get(i).getValue(), testEvent.getDynamicFields().get(j).getValue());
                    }
                }
            } else if (event1.getDynamicFields().get(i).getType() == FieldType.FEE.getId()) {
                for (int j = 0; j < testEvent.getDynamicFields().size(); j++) {
                    if (testEvent.getDynamicFields().get(j).getType() == FieldType.FEE.getId()) {
                        assertEquals("Die Fee stimmt nicht überein", event1.getDynamicFields().get(i).getValue(), testEvent.getDynamicFields().get(j).getValue());
                    }
                }
            } else if (event1.getDynamicFields().get(i).getType() == FieldType.LOCATION.getId()) {
                for (int j = 0; j < testEvent.getDynamicFields().size(); j++) {
                    if (testEvent.getDynamicFields().get(j).getType() == FieldType.LOCATION.getId()) {
                        assertEquals("Die Location stimmt nicht überein", event1.getDynamicFields().get(i).getValue(), testEvent.getDynamicFields().get(j).getValue());
                    }
                }
            } else if (event1.getDynamicFields().get(i).getType() == FieldType.DESCRIPTION.getId()) {
                for (int j = 0; j < testEvent.getDynamicFields().size(); j++) {
                    if (testEvent.getDynamicFields().get(j).getType() == FieldType.DESCRIPTION.getId()) {
                        assertEquals("Die Beschreibung stimmt nicht überein", event1.getDynamicFields().get(i).getValue(), testEvent.getDynamicFields().get(j).getValue());
                    }
                }
            } else if (event1.getDynamicFields().get(i).getType() == FieldType.ROUTE.getId()) {
                for (int j = 0; j < testEvent.getDynamicFields().size(); j++) {
                    if (testEvent.getDynamicFields().get(j).getType() == FieldType.ROUTE.getId()) {
                        assertEquals("Der Routenname stimmt nicht überein", event1.getDynamicFields().get(i), testEvent.getDynamicFields().get(j));
                    }
                }
            }
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
        for (Route r : ServiceProvider.getService().skatenightServerEndpoint().getRoutes().execute()
                .getItems()) {
            ServiceProvider.getService().skatenightServerEndpoint().deleteRoute(r.getKey().getId())
                    .execute();
        }
    }
}
