package ws1415.ps1415.useCases;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import ws1415.ps1415.Constants;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.Settings;
import ws1415.ps1415.activity.ShowEventsActivity;
import ws1415.ps1415.activity.ShowInformationActivity;
import ws1415.ps1415.activity.ShowRouteActivity;

/**
 * Testet den Use Case "Anzeigen der aktuellen Position".
 * <strong>Für diesen Test muss GPS muss aktiviert sein.</strong>
 *
 * @author Tristan Rust
 */
public class ShowCurrentPositionTest extends ActivityInstrumentationTestCase2<ShowRouteActivity> {

    private ShowRouteActivity mActivity;
    private LocationManager mLocationManager;

    // ShowEventsActivity UI Elemente
    private ListView mList;
    private ListAdapter mListData;

    public ShowCurrentPositionTest() {
        super(ShowRouteActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        // Nutzer einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:"+ Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        // Die ShowRouteActivity starten
        mActivity = getActivity();
        assertNotNull("Activity didn't start!", mActivity);
        // mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

        Thread.sleep(5000); // Zeit zum initialisieren
        // Holt sich die Event Listen-Elemente
        // mList = (ListView) mActivity.findViewById(R.id.activity_show_events_list_view);
        // mListData = mList.getAdapter();
    }

    /**
     * Testet, ob die Acitivy gestartet worden ist.
     */
    public void testPreConditions() {
        assertNotNull("Activity didn't start!", mActivity);
        assertNotNull("selection listener on events list initialized", mList.getOnItemClickListener());
        assertNotNull("adapter for events initialized", mListData);

        // Mindestens ein Event muss existieren
        assertTrue("at least one event exists", mListData.getCount() > 0);
    }

    /**
     * Prüft, ob die Elemente sich in der Activity überschneiden bzw. wirklich gerendert werden.
     *
     * @throws java.lang.Exception
     */
    @SmallTest
    public void testViewsVisible() {
        // Falls es mehr als zwei Elemente in der Liste gibt, prüfe, ob die ersten beiden sich überschneiden oder nicht
        /*if (mListData != null && mListData.getCount()>1) {
           ViewAsserts.assertOnScreen(mListData.getView(0, null, mList).getRootView(), mListData.getView(1, null, mList).getRootView());
           ViewAsserts.assertOnScreen(mListData.getView(1, null, mList).getRootView(), mListData.getView(0, null, mList).getRootView());
        }*/
        assertTrue(true);
    }

    protected void tearDown() throws Exception {super.tearDown();}

    @UiThreadTest
    public void testUseCase() throws Exception {
        Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(ShowRouteActivity.class.getName(), null, false);

        // Es wird ein neuer UI Thread gestartet & das erste Event ausgewählt und anschließend die showInformationActivity gestartet
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int firstEvent = 0;
                // mList.performItemClick(mList.getAdapter().getView(firstEvent, null, mList), firstEvent, mList.getAdapter().getItemId(firstEvent));
            }
        });

        Thread.sleep(5000);

        // ShowInformationActivity showInformationActivity = (ShowInformationActivity) getInstrumentation().waitForMonitorWithTimeout(am, 1000);
        // assertNotNull("ShowInformationActivity started", showInformationActivity);


        /*Intent intent = new Intent(context, ShowRouteActivity.class);
        intent.putExtra(ShowRouteActivity.EXTRA_ROUTE, event.getRoute().getRouteData().getValue());
        intent.putExtra(ShowRouteActivity.EXTRA_ROUTE_FIELD_FIRST, event.getRouteFieldFirst());
        intent.putExtra(ShowRouteActivity.EXTRA_ROUTE_FIELD_LAST, event.getRouteFieldLast());
        context.startActivity(intent);
        */




    }

    /**
     * Prüft, ob die Position, die mit Hilfe von GPS ermittelt wurde angezeigt wird.
     * <strong>GPS muss hierfür aktiviert sein.</strong>
     *
     * @exception java.lang.InterruptedException
     */
    /*
    public void testUseCase() throws InterruptedException {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ShowRouteActivity.class.getName(), null, false);

        Criteria criteria = new Criteria();
        Location location = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(criteria, false));




        Thread.sleep(5000);
        assertNotNull("location could not be requested", location);
    }*/

}




















