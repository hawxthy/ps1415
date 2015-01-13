/*
package ws1415.veranstalterapp.task;

import android.test.AndroidTestCase;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import ws1415.veranstalterapp.ServiceProvider;

*/
/**
 * Created by Bernd Eissing on 21.11.2014.
 *//*

public class GetEventTaskTest extends AndroidTestCase {
    private Route route;
    private Event event;

    */
/**
     * Erstellt 2 Routen und ein Event. Wobei dem Event eine der beiden Routen
     * zugeordnet wird. Dient zum Initialisieren der Objekte
     *//*

    public GetEventTaskTest(){
        // Erstellle die Route für das Event
        route = new Route();
        route.setName("testroute");
        route.setLength("7km");
        route.setRouteData(new Text().setValue(("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
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
        event = new Event();
        event.setTitle("Testevent");
        event.setFee("7");
        event.setLocation("Münster");
        event.setDate(new DateTime(new Date()));
        event.setRoute(route);
        event.setDescription(new Text().setValue("Das ist ein Testevent"));
    }

    */
/**
     * Löscht alle Events vom Server und speichert dann das TestEvent
     *
     * @throws Exception
     *//*

    public void setUp() throws Exception{
        super.setUp();

        // Das oben erstellte Event auf dem Server speichern und wieder abrufen, damit
        // der Key für das Even generiert wird.
        List<Event> events = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents()
                .execute().getItems();
        if (events != null) {
            for (Event e : events) {
                ServiceProvider.getService().skatenightServerEndpoint().deleteEvent(e.getKey().getId())
                        .execute();
            }
        }

        // Speichern des einen Events um den Key zu generieren
        ServiceProvider.getService().skatenightServerEndpoint().createEvent(event).execute();
        // Event vom Server abrufen
        event = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents().execute().getItems().get(0);
    }

    */
/**
     * Tested, ob die Attribute von event mit testEvent übereinstimmen
     *
     * @throws IOException
     *//*

    public void testTask() throws IOException{
        // Event mit getEvent vom Server abrufen und mit event vergleichen
        Event testEvent = ServiceProvider.getService().skatenightServerEndpoint().getEvent(event.getKey().getId()).execute();

        assertEquals("Der Titel stimmt nicht überein", event.getTitle(), testEvent.getTitle());
        assertEquals("Die Fee stimmt nicht überein", event.getFee(), testEvent.getFee());
        assertEquals("Die Location stimmt nicht überein", event.getLocation(), testEvent.getLocation());
        assertEquals("Der Routenaname stimmt nicht überein", event.getRoute().getName(), testEvent.getRoute().getName());
        assertEquals("Die Länge der Route stimmt nicht überein", event.getRoute().getLength(), testEvent.getRoute().getLength());
        assertEquals("Die Description stimmt nicht überein", event.getDescription(), testEvent.getDescription());
    }
}
*/
