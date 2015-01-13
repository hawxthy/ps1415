package ws1415.ps1415.useCases;

import android.test.ActivityInstrumentationTestCase2;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;
import com.skatenight.skatenightAPI.model.Route;

import com.skatenight.skatenightAPI.model.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ws1415.ps1415.Constants;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.ShowEventsActivity;
import ws1415.ps1415.util.FieldType;

/**
 * Testet den Use Case "Anzeigen mehrerer Veranstaltungen".
 *
 * @author Tristan Rust
 */
public class ShowSeveralEventsTest extends ActivityInstrumentationTestCase2<ShowEventsActivity> {

    private ShowEventsActivity mActivity;

    private Event testEvent;
    private Route testRoute;

    private final String TEST_TITLE = "testTitle";
    private final String TEST_FEE = "testFee";
    private final String TEST_DATE = "27.2.2015";
    private final String TEST_TIME = "20:00 Uhr";
    private final String TEST_LOCATION = "testOrt";
    private final String TEST_DESCRIPTION = "testBeschreibung";



    public ShowSeveralEventsTest() {
        super(ShowEventsActivity.class);

        testEvent = new Event();
        testEvent.setDynamicFields(new ArrayList<Field>());

        ArrayList<Field> eventData = new ArrayList<Field>();
        Field fieldData = new Field();

        fieldData.setValue(TEST_TITLE);
        fieldData.setType(8);
        eventData.add(fieldData);

        fieldData.setValue(TEST_DATE);
        fieldData.setType(3);
        eventData.add(fieldData);

        fieldData.setValue(TEST_TIME);
        fieldData.setType(4);
        eventData.add(fieldData);

        fieldData.setValue(TEST_LOCATION);
        fieldData.setType(9);
        eventData.add(fieldData);

        fieldData.setValue(TEST_DESCRIPTION);
        fieldData.setType(10);
        eventData.add(fieldData);

        fieldData.setValue(TEST_FEE);
        fieldData.setType(2);
        eventData.add(fieldData);

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

        testEvent.setDynamicFields(eventData);


    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Nutzer einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:"+ Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        // Touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        // Löschen aller Events
        List<Event> list = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents().execute().getItems();
        if (list != null) {
            for (Event e : list) {
                ServiceProvider.getService().skatenightServerEndpoint().deleteEvent(e.getKey().getId()).execute();
            }
        }

        // Neues Test Event anlegen
        ServiceProvider.getService().skatenightServerEndpoint().createEvent(testEvent).execute();

        // Die ShowEventsActivity starten
        mActivity = getActivity();

    }

}












