
package ws1415.veranstalterapp.task;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.EventData;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import ws1415.common.net.ServiceProvider;

//import com.skatenight.skatenightAPI.model.Field;


/**
 * Created by Bernd Eissing on 21.11.2014.
 */

public class GetEventTaskTest extends AuthTaskTestCase {
    private Route route1;
    private Event event1;
    private long time;


    /**
     * Erstellt 2 Routen und ein Event. Wobei dem Event eine der beiden Routen
     * zugeordnet wird. Dient zum Initialisieren der Objekte
     */

    public GetEventTaskTest(){
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

        // Erstelle das zu testende Event
        event1 = new Event();
        event1.setTitle("Testevent");
        event1.setFee(700);
        event1.setDate(new DateTime(time));
        event1.setMeetingPlace("Münster");
        event1.setRoute(route1);
        event1.setDescription(new Text().setValue("Das ist ein Testevent"));
    }


/**
     * Löscht alle Events vom Server und speichert dann das TestEvent
     *
     * @throws Exception
     */

    public void setUp() throws Exception{
        super.setUp();

        // Das oben erstellte Event auf dem Server speichern und wieder abrufen, damit
        // der Key für das Even generiert wird.
        List<Event> events = ServiceProvider.getService().eventEndpoint().getAllEvents()
                .execute().getItems();
        if (events != null) {
            for (Event e : events) {
                ServiceProvider.getService().eventEndpoint().deleteEvent(e.getId())
                        .execute();
            }
        }

        // Speichern des einen Events um den Key zu generieren
        ServiceProvider.getService().eventEndpoint().createEvent(event1).execute();
        // Event vom Server abrufen
        event1 = ServiceProvider.getService().eventEndpoint().getAllEvents().execute().getItems().get(0);
    }


    /**
     * Tested, ob die Attribute von event mit testEvent übereinstimmen
     *
     * @throws IOException
     */

    public void testTask() throws IOException{
        // Event mit getEvent vom Server abrufen und mit event vergleichen
        EventData testEvent = ServiceProvider.getService().eventEndpoint().getEvent(event1.getId()).execute();

        assertEquals("Der Title stimmt nicht überein", event1.getTitle(), testEvent.getTitle());
        assertEquals("Die Fee stimmt nicht überein", event1.getFee(), testEvent.getFee());
        assertEquals("Die Location stimmt nicht überein", event1.getMeetingPlace(), testEvent.getMeetingPlace());
        assertEquals("Die Beschreibung stimmt nicht überein", event1.getDescription().getValue(), testEvent.getDescription());
        assertEquals("Der Routenname stimmt nicht überein", event1.getRoute().getName(), testEvent.getRoute().getName());

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
        for (Route r : ServiceProvider.getService().routeEndpoint().getRoutes().execute()
                .getItems()) {
            ServiceProvider.getService().routeEndpoint().deleteRoute(r.getId())
                    .execute();
        }
    }
}

