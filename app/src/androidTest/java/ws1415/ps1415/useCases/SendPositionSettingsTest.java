package ws1415.ps1415.useCases;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.UiThreadTest;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.method.Touch;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Field;
import com.skatenight.skatenightAPI.model.Member;
import com.skatenight.skatenightAPI.model.Route;

import ws1415.ps1415.Constants;
import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.Settings;
import ws1415.ps1415.activity.ShowEventsActivity;
import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.activity.ShowInformationActivity;
import ws1415.ps1415.activity.ShowRouteActivity;
import ws1415.ps1415.task.QueryMemberTask;
import ws1415.ps1415.task.UpdateLocationTask;
import ws1415.ps1415.util.EventUtils;

/**
 * Testet den Use Case "Übertragung der aktuellen Position an den Server".
 *
 * @author Tristan Rust
 */
public class SendPositionSettingsTest extends ActivityInstrumentationTestCase2<ShowEventsActivity> {

    private ShowEventsActivity mActivity;

    private final String TEST_EMAIL        = "tristan.rust@googlemail.com";
    private final LatLng TEST_POSITION     = new LatLng(51.9659371, 7.6032621);
    private final LatLng TEST_NEW_POSITION = new LatLng(51.9659847, 7.6033358);

    // ShowEventsActivty UI Elemente
    private ListView mList;
    private ListAdapter mListData;

    private Event testEvent;
    private Route testRoute;

    public SendPositionSettingsTest() {
        super(ShowEventsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        // Die ShowEventsActivity starten
        getInstrumentation().getTargetContext().getApplicationContext();
        mActivity = getActivity();
        // Löschen der Voreinstellungen
        // Clear everything in the SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();

        // Nutzer einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:"+ Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        Thread.sleep(5000);
        // Holt sich die Event Listen-Elemente
        mList = (ListView) mActivity.findViewById(R.id.activity_show_events_list_view);
        mListData = mList.getAdapter();
    }

    /**
     * Prüfen, ob die Einstellungen zurück gesetzt wurden.
     */
    public void testPreConditions() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        assertEquals("false", sharedPreferences.getString("prefSendLocation", "false"));
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
        if (mListData != null && mListData.getCount()>1) {
            ViewAsserts.assertOnScreen(mListData.getView(0, null, mList).getRootView(), mListData.getView(1, null, mList).getRootView());
            ViewAsserts.assertOnScreen(mListData.getView(1, null, mList).getRootView(), mListData.getView(0, null, mList).getRootView());
        }
    }

    /**
     * Prüft, ob die Einstellungen ausgewählt bleiben.
     *
     * @throws java.lang.InterruptedException
     */
    @SmallTest
    public void testSettingsUI() throws InterruptedException {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(Settings.class.getName(), null, false);

        // Klick auf die Menüoption
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.action_settings, 0);

        // UI Elemente lassen sich hier nicht ansprechen
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sharedPreferences.edit().putBoolean("prefSendLocation", true).apply();

        Activity am = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 2000);
        Thread.sleep(2500);

        am.finish();

        assertEquals(true, sharedPreferences.getBoolean("prefSendLocation", false));

    }

    /**
     * Testet die Activity beim Beenden und wieder Neustarten.
     * @throws java.lang.Exception
     */
    @SmallTest
    public void testSettingsUIStateDestroy() throws InterruptedException {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(Settings.class.getName(), null, false);

        // Klick auf die Menüoption
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.action_settings, 0);

        // UI Elemente lassen sich hier nicht ansprechen
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sharedPreferences.edit().putBoolean("prefSendLocation", true).apply();

        Activity am = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 2000);
        Thread.sleep(2500);

        am.finish();

        // Beenden & Neustarten der Activity
        mActivity.finish();
        mActivity = this.getActivity();

        assertEquals(true, sharedPreferences.getBoolean("prefSendLocation", false));
    }


    /**
     * Testet, ob die Activity weiter läuft, wenn diese pausiert hat. Test läuft auf dem UI Thread.
     * @throws java.lang.InterruptedException
     */
    @SmallTest
    public void testSettingsUIStatePause() throws InterruptedException {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(Settings.class.getName(), null, false);

        // Klick auf die Menüoption
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.action_settings, 0);

        // UI Elemente lassen sich hier nicht ansprechen
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sharedPreferences.edit().putBoolean("prefSendLocation", true).apply();

        Activity am = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 2000);
        Thread.sleep(2500);

        // Pausieren
        getInstrumentation().callActivityOnPause(am);

        // Fortsetzen
        getInstrumentation().callActivityOnResume(am);

        am.finish();

        assertEquals(true, sharedPreferences.getBoolean("prefSendLocation", false));
    }

    /**
     * Testet, ob die Position an den Server übertragen wird, wenn der User dies aktiviert hat.
     * <strong>GPS muss hierfür aktiviert sein</strong>
     *
     * @throws java.lang.Exception
     */
    @LargeTest
    public void testSendPosition() throws Exception {
        // Aktiviert die Funktion zum Senden
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sharedPreferences.edit().putBoolean("prefSendLocation", true).apply();

        assertEquals(true, sharedPreferences.getBoolean("prefSendLocation", false));

        // Testen, ob die Position an den Server gesendet wird bei aktiver Einstellung
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ShowRouteActivity.class.getName(), null, false);

        Thread.sleep(2000);
        ShowRouteActivity am = (ShowRouteActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 10000);
        assertNotNull("ShowRouteActivity didn't start!", am);

        // Setzen der Position auf den Server
        new UpdateLocationTask(TEST_EMAIL, TEST_POSITION.latitude, TEST_POSITION.longitude).execute();

        Member m = ServiceProvider.getService().skatenightServerEndpoint().getMember(TEST_EMAIL).execute();
        assertEquals(TEST_POSITION.latitude, m.getLatitude());
        assertEquals(TEST_POSITION.longitude, m.getLongitude());

        // Starten des Hintergrundservices zur Standortermittlung

        am.finish();



    }

}












