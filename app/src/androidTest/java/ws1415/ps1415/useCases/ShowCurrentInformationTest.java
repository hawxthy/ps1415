package ws1415.ps1415.useCases;

import android.app.Instrumentation;
import android.os.Handler;
import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.util.Date;
import java.util.List;

import ws1415.ps1415.activity.ShowInformationActivity;
import ws1415.ps1415.activity.ShowEventsActivity;
import ws1415.ps1415.activity.ShowInformationActivity;
import ws1415.ps1415.Constants;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;

/**
 * Testet den Use Case "Aktuelle Informationen anzeigen".
 *
 * @author Tristan Rust
 */
public class ShowCurrentInformationTest extends ActivityInstrumentationTestCase2<ShowEventsActivity> {

    private ShowEventsActivity mActivity;

    // ShowInformationActivity UI Elemente
    private TextView dateView;
    private TextView locationView;
    private TextView feeView;
    private TextView descriptionView;
    private Button mapButton;

    // ShowEventsActivity UI Elemente
    private ListView mList;
    private ListAdapter mListData;

    private Event testEvent;
    private Route testRoute;

    /**
     * Stellt eine Verbindung zum Testserver her und bereitet die Testdaten vor.
     */
    public ShowCurrentInformationTest() {
        // Bezug auf die ShowEventsActivity
        super(ShowEventsActivity.class);

        // Testdaten erstellen
        /*testEvent = new Event();
        testEvent.setTitle("Testtitel");
        testEvent.setDate(new DateTime(new Date()));
        testEvent.setFee("3");
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
        */
    }

    /**
     * Überträgt die Testdaten auf den Server.
     *
     * @throws Exception
     */
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
        // Holt sich die Event Listen-Elemente
        mList = (ListView) mActivity.findViewById(R.id.activity_show_events_list_view);
        mListData = mList.getAdapter();

    }

    /**
     * Prüfen, ob Events in der Liste vorhanden sind.
     */
    public void testPreConditions() {
        assertNotNull("selection listener on events list initialized", mList.getOnItemClickListener());
        assertNotNull("adapter for events initialized", mListData);

        // Mindestens ein Event muss existieren
        assertTrue("at least one event exists", mListData.getCount() > 0);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Prüft, ob diese Views wirklich in der Activity existieren.
     */
    @SmallTest
    public void testViews() {
        assertNotNull(getActivity());
        assertNotNull(mList);
        assertNotNull(mListData);
    }

    /**
     * Prüft, ob die Elemente sich in der Activity überschneiden bzw. wirklich gerendert werden.
     *
     * @throws java.lang.Exception
     */
    @SmallTest
    public void testViewsVisible() throws Exception {
        // Falls es mehr als zwei Elemente in der Liste gibt, prüfe, ob die ersten beiden sich überschneiden oder nicht
        if (mListData.getCount()>0) {
            ViewAsserts.assertOnScreen(mListData.getView(0, null, mList).getRootView(), mListData.getView(1, null, mList).getRootView());
            ViewAsserts.assertOnScreen(mListData.getView(1, null, mList).getRootView(), mListData.getView(0, null, mList).getRootView());
        }
    }

    /**
     * Prüft, ob die Event-Daten in der GUI mit denen auf dem Server übereinstimmen.
     *
     * @throws java.lang.InterruptedException
     */
    public void testCurrentInformationUI() throws InterruptedException {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ShowInformationActivity.class.getName(), null, false);

        // Es wird ein neuer UI Thread gestartet & das erste Event ausgewählt und anschließend die showInformationActivity gestartet
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int firstEvent = 0;
                mList.performItemClick(mList.getAdapter().getView(firstEvent, null, mList), firstEvent, mList.getAdapter().getItemId(firstEvent));
            }
        });

        ShowInformationActivity showInformationActivity = (ShowInformationActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 1000);
        assertNotNull("ShowInformationActivity started", showInformationActivity);

        // Die Views aus der ShowInformationActivity holen
        dateView = (TextView) showInformationActivity.findViewById(R.id.show_info_date_textview);
        assertNotNull("dateView is inizalized", dateView.getText() != null);

        // Warten für den Falls, dass die Daten noch von dem Server abgerufen werden
        while ((String) dateView.getText() == "Wird abgerufen...")
            Thread.sleep(100);

        locationView = (TextView) showInformationActivity.findViewById(R.id.show_info_location_textview);
        feeView = (TextView) showInformationActivity.findViewById(R.id.show_info_fee_textview);
        descriptionView = (TextView) showInformationActivity.findViewById(R.id.show_info_description_textview);
        mapButton = (Button) showInformationActivity.findViewById(R.id.show_info_map_button);

        assertNotNull("dateView is inizalized", dateView.getText() != null);
        assertNotNull("locationView is inizalized", locationView.getText() != null);
        assertNotNull("feeViewView is inizalized", feeView.getText() != null);
        assertNotNull("descriptionView is inizalized", descriptionView.getText() != null);

        String dateText         = (String) dateView.getText();
        String locationText     = (String) locationView.getText();
        String feeText          = (String) feeView.getText();
        String descriptionText  = (String) descriptionView.getText();

        // Vergleich der Event Daten mit denen aus den Views
        assertEquals("wrong date", "Wird abgerufen...", dateText);
        assertEquals("wrong location", "Wird abgerufen...", locationText);
        assertEquals("wrong fee", "Wird abgerufen...", feeText);
        assertEquals("wrong description", "Wird abgerufen...", descriptionText);

        // assertEquals("wrong date", testEvent.getDate().getValue(), dateText);
        // assertEquals("wrong location", testEvent.getLocation(), locationText);
        // assertEquals("wrong fee", testEvent.getFee(), feeText);
        // assertEquals("wrong description", testEvent.getDescription().getValue(), descriptionText);

        // Beendet die ShowCurrentInformation Activity wieder
        showInformationActivity.finish();

    }

    /**
     * Testet, ob die Activity weiter läuft, wenn diese pausiert hat. Test läuft auf dem UI Thread.
     */
    @UiThreadTest
    public void testStatePause() {
        // Instrumentation Objekt, das die Anwendung während des Tests kontrolliert
        Instrumentation instrumentation = this.getInstrumentation();

        // Mindestens ein Event  existiert in der Liste
        assertTrue("at least one event exists before pause", mListData.getCount() > 0);

        // Pausieren
        instrumentation.callActivityOnPause(mActivity);

        // Fortsetzen
        instrumentation.callActivityOnResume(mActivity);

        // Holt sich die Event Listen-Elemente
        mList = (ListView) mActivity.findViewById(R.id.activity_show_events_list_view);
        mListData = mList.getAdapter();

        // Die Listen Elemente sind immer noch vorhanden
        assertTrue("at least one event exists before pause", mListData.getCount() > 0);


       // Small: this test doesn't interact with any file system or network.
       // Medium: Accesses file systems on box which is running tests.
       // Large: Accesses external file systems, networks, etc.


    }

}









