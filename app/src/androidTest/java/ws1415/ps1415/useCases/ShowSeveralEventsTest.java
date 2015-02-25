package ws1415.ps1415.useCases;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import ws1415.ps1415.Constants;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.ShowEventsActivity;
import ws1415.ps1415.activity.ShowInformationActivity;

/**
 * Testet den Use Case "Anzeigen mehrerer Veranstaltungen".
 * <p><strong>Hierfür muss zuerst der Test PublishNewInformationTest in der
 * VeranstalterApp ausgeführt worden sein.</strong></p>
 *
 * @author Tristan Rust
 */
public class ShowSeveralEventsTest extends ActivityInstrumentationTestCase2<ShowEventsActivity> {

    // Test Event-Daten
    private static final String TEST_TITLE = "TestEventEdit";
    private static final String TEST_FEE   = "5 €";
    private static final String TEST_LOCATION = "TestStadtEdit";
    // private static final String TEST_DATE = "24-02-2015 20:00";

    // Die ShowEventsActivity
    private ShowEventsActivity mActivity;

    // ShowEventActivity UI Elemente
    private ListView mList;
    private ListAdapter mListData;

    public ShowSeveralEventsTest() {
        super(ShowEventsActivity.class);
    }

    /**
     * Loggt den User ein und führt die ShowEventsActivity aus.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Nutzer verbinden, Nicht einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        // touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        Thread.sleep(5000); // Zeit zum initialisieren

        // Die ShowEventsActivity wird gestartet
        mActivity = getActivity();

        Thread.sleep(5000); // Zeit zum initialisieren
        // Holt sich die Event Listen-Elemente
        mList = (ListView) mActivity.findViewById(R.id.activity_show_events_list_view);
        mListData = mList.getAdapter();
        Thread.sleep(5000);

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
     * Prüft, ob diese Views wirklich in der Activity exisiteren.
     */
    @SmallTest
    public void testViews() {
        assertNotNull(mActivity);
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
        // Falls es mehr als ein Element in der Liste gibt, prüfe, ob die ersten beiden sich überschneiden oder nicht
        if (mListData.getCount()>1) {
            ViewAsserts.assertOnScreen(mListData.getView(0, null, mList).getRootView(), mListData.getView(1, null, mList).getRootView());
            ViewAsserts.assertOnScreen(mListData.getView(1, null, mList).getRootView(), mListData.getView(0, null, mList).getRootView());
        }
    }

    /**
     * UseCase der überprüft, ob das zuvor angelegte Element aus der Veranstalter App hier auch
     * wirklich sichtbar ist.
     *
     * @throws InterruptedException
     */
    public void testUseCase() throws InterruptedException {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ShowInformationActivity.class.getName(), null, false);

        // Es wird ein neuer UI Thread gestartet & das erste Event ausgewählt und anschließend die showInformationActivity gestartet
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int firstEvent = 0;
                mList.performItemClick(mList.getAdapter().getView(firstEvent, null, mList), firstEvent, mList.getAdapter().getItemId(firstEvent));
            }
        });

        // ShowInformationActivity wird gestartet
        ShowInformationActivity showInformationActivity = (ShowInformationActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 20000);
        assertNotNull("ShowInformationActivity not started", showInformationActivity);

        // Liste, die die TextView enthält
        ListView listView = (ListView) showInformationActivity.findViewById(R.id.activity_show_information_list_view);
        assertNotNull("listView is null!", listView);
        listView.setAdapter(showInformationActivity.getShowCursorAdapter());

        Thread.sleep(5000); // Zeit zum Initialisieren
        assertNotNull("listViewAdapter is null!", listView.getAdapter());

        // Enthält die jeweiligen Eventinformationen
        String temp = "";

        // Prüfen, ob die Eventinformationen mit denen des TestEvents übereinstimmen
        TextView title = (TextView) listView.getChildAt(0).findViewById(R.id.list_view_item_show_information_text_field_textView_content);
        temp = (String) title.getText();
        assertEquals(TEST_TITLE, temp);

        TextView fee = (TextView) listView.getChildAt(1).findViewById(R.id.list_view_item_show_information_text_field_textView_content);
        temp = (String) fee.getText();
        assertEquals(TEST_FEE, temp);

        // TextView date = (TextView) listView.getChildAt(3).findViewById(R.id.list_view_item_show_information_text_field_textView_content);
        // temp = (String) date.getText();
        // assertEquals(TEST_DATE, temp);

        TextView location = (TextView) listView.getChildAt(4).findViewById(R.id.list_view_item_show_information_text_field_textView_content);
        temp = (String) location.getText();
        assertEquals(TEST_LOCATION, temp);

        // Beenden der Activity
        showInformationActivity.finish();


    }



}




