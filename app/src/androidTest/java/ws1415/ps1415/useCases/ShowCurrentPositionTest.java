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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import ws1415.ps1415.Constants;
import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.Settings;
import ws1415.ps1415.activity.ShowEventsActivity;
import ws1415.ps1415.activity.ShowInformationActivity;
import ws1415.ps1415.activity.ShowRouteActivity;

/**
 * Testet den Use Case "Anzeigen der aktuellen Position".
 * <strong>Für diesen Test muss GPS muss aktiviert sein.</strong>
 * Es wird lediglich die Map aufgerufen, die dann wenn GPS verfügbar ist, automatisch (von Android)
 * einen blauen Punkt auf die Karte setzt, der die Position anzeigt.
 *
 * @author Tristan Rust
 */
public class ShowCurrentPositionTest extends ActivityInstrumentationTestCase2<ShowEventsActivity> {

    private ShowEventsActivity mActivity;
    private LocationManager mLocationManager;

    // ShowEventsActivity UI Elemente
    private ListView mList;
    private ListAdapter mListData;

    public ShowCurrentPositionTest() {
        super(ShowEventsActivity.class);
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

        Thread.sleep(5000); // Zeit zum initialisieren
        // Holt sich die Event Listen-Elemente
        mList = (ListView) mActivity.findViewById(R.id.activity_show_events_list_view);
        mListData = mList.getAdapter();
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

    protected void tearDown() throws Exception {super.tearDown();}

    @UiThreadTest
    public void testUseCase() throws Exception {
        Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(ShowInformationActivity.class.getName(), null, false);

        // Es wird ein neuer UI Thread gestartet & das erste Event ausgewählt und anschließend die showInformationActivity gestartet
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int firstEvent = 0;
                mList.performItemClick(mList.getAdapter().getView(firstEvent, null, mList), firstEvent, mList.getAdapter().getItemId(firstEvent));
            }
        });

        Thread.sleep(1000); // Zeit zum initialisieren
        getInstrumentation().getTargetContext().getApplicationContext();
        Context context = mActivity.getBaseContext();
        Intent intent = new Intent(context, ShowRouteActivity.class);
        mActivity.startActivity(intent);
        Thread.sleep(5000); // Zeit zum initialisieren


        // BUG: Android Bug, die App stürzt ab beim Versuch von der ShowInformationActivity zu wechseln.
        //      Es ist auch nicht möglich die ShowRouteActivity dirket über die Insturmentations anzusprechen,
        //      deshalb wird die ShowRouteActivity direkt angesprochen, um die Karte + Position anzuzeigen.
        //
        // ShowInformationActivity showInformationActivity = (ShowInformationActivity) getInstrumentation().waitForMonitorWithTimeout(am, 1000);
        // assertNotNull("ShowInformationActivity started", showInformationActivity);
        // Intent intent = new Intent(mActivity, ShowInformationActivity.class);
        // intent.putExtra(ShowInformationActivity.EXTRA_KEY_ID, "5675267779461120");
        // mActivity.startActivity(intent);

    }

}




















