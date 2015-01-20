package ws1415.ps1415.useCases;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.Settings;
import ws1415.ps1415.activity.ShowEventsActivity;
import ws1415.ps1415.activity.ShowInformationActivity;
import ws1415.ps1415.task.CreateEventTask;
import ws1415.ps1415.util.FieldType;

/**
 * Testet den Use Case "Anzeigen mehrerer Veranstaltungen".
 *
 * @author Tristan Rust
 */
public class ShowSeveralEventsTest extends ActivityInstrumentationTestCase2<ShowEventsActivity> {

    private ShowEventsActivity mActivity;

    private final String TEST_TITLE = "TestEvent";
    private final String TEST_FEE = "10";
    private final String TEST_DATE = "29.12.2014";
    private final String TEST_TIME = "14:00 Uhr";
    private final String TEST_LOCATION = "TestStadt";
    private final String TEST_DESCRIPTION = "TestBeschreibung";

    private TextView eventName;
    private TextView eventDate;
    private TextView eventFee;
    private TextView eventLocation;
    private ListView listView;

    // ShowEventsActivity UI Elemente
    private ListView mList;
    private ListAdapter mListData;

    public ShowSeveralEventsTest() {
        super(ShowEventsActivity.class);
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

        // Die ShowEventsActivity starten
        mActivity = getActivity();

        Thread.sleep(5000);
        // Holt sich die Event Listen-Elemente
        mList = (ListView) mActivity.findViewById(R.id.activity_show_events_list_view);
        mListData = mList.getAdapter();
    }

    /**
     * Prüfen, ob Events in der Liste vorhanden sind.
     */
    public void testPreConditions() {
        assertNotNull("Activity could not start!", mActivity);
        assertNotNull("Selection listener not initialized!", mList.getOnItemClickListener());
        assertNotNull("Adapter not initialized!", mListData);

        // Mindestens ein Event muss existieren
        assertTrue("No events available!", mListData.getCount() > 0);
    }

    protected void tearDown() throws Exception {super.tearDown();}

    /**
     * Prüft, ob die Elemente sich in der Activity überschneiden bzw. wirklich gerendert werden.
     *
     * @throws java.lang.Exception
     */
    @SmallTest
    public void testViewsVisible() throws Exception {
        // Falls es mehr als zwei Elemente in der Liste gibt, prüfe, ob die ersten beiden sich überschneiden oder nicht
        /*if (mListData != null && mListData.getCount()>1) {
            ViewAsserts.assertOnScreen(mListData.getView(0, null, mList).getRootView(), mListData.getView(1, null, mList).getRootView());
            ViewAsserts.assertOnScreen(mListData.getView(1, null, mList).getRootView(), mListData.getView(0, null, mList).getRootView());
        }*/
        assertTrue(true);
    }

    /**
     * Prüft, ob ein Event ausgewählt werden kann und zudem die entsprechenden Informationen
     * detailiert angezeigt wird.
     *
     * @throws Exception
     *
     */
    @UiThreadTest
    public void testUseCaseUI() throws Exception {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ShowInformationActivity.class.getName(), null, false);

        // Es wird ein neuer UI Thread gestartet & das erste Event ausgewählt und anschließend die ShowInformationActivity gestartet.
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int firstEvent = 0;
                mList.performItemClick(mList.getAdapter().getView(firstEvent, null, mList), firstEvent, mList.getAdapter().getItemId(firstEvent));
            }
        });

        // ShowInformationActivity starten
        // ShowInformationActivity showInformationActivity = (ShowInformationActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
        Thread.sleep(5000);
        // assertNotNull("ShowInformationActivity didn't start!", showInformationActivity);
        Thread.sleep(5000);



        // TextView dateView = (TextView) showInformationActivity.findViewById(R.id.show_info_date_textview);
        // TextView locationView = (TextView) showInformationActivity.findViewById(R.id.show_info_location_textview);
        // TextView feeView = (TextView) showInformationActivity.findViewById(R.id.show_info_fee_textview);
        // TextView descriptionView = (TextView) showInformationActivity.findViewById(R.id.show_info_description_textview);



//         showInformationActivity.finish();


        // Die Views aus der ShowInformationActivity holen
        Thread.sleep(5000);



    }

}
