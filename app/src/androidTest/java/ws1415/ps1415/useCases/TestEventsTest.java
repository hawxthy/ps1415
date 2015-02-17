package ws1415.ps1415.useCases;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import ws1415.ps1415.Constants;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.AddUserGroupActivity;
import ws1415.ps1415.activity.ShowEventsActivity;
import ws1415.ps1415.activity.ShowInformationActivity;
import ws1415.ps1415.adapter.ShowCursorAdapter;

/**
 * Created by thy on 13.02.2015.
 */
public class TestEventsTest extends ActivityInstrumentationTestCase2<ShowEventsActivity> {

    private ShowEventsActivity mActivity;

    private ListView mList;
    private ListAdapter mListData;

    public TestEventsTest() {
        super(ShowEventsActivity.class);
    }

    /**
     * Loggt den User ein und wechselt auf die ShowEventsActivity.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        // Die ShowEventsActivity wird gestartet
        mActivity = getActivity();

        // Nutzer verbinden, Nicht einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        Thread.sleep(5000); // Zeit zum initialisieren
        // Holt sich die Event Listen-Elemente
        mList = (ListView) mActivity.findViewById(R.id.activity_show_events_list_view);
        mListData = mList.getAdapter();

    }

    /**
     * Prüft, ob diese Views wirklich in der Activity exisiteren.
     */
    @SmallTest
    public void testViews() {
        assertNotNull(mList);
    }

    public void test() {
        assertEquals(1, 1);
    }

    public void testUseCase() {
        // Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(ShowInformationActivity.class.getName(), null, false);
        assertEquals(1,1);


    }



}






























