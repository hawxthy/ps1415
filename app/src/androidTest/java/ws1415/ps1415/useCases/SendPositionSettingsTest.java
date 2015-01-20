package ws1415.ps1415.useCases;

import android.app.Activity;
import android.app.ActivityManager;
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
import android.widget.Toast;

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
 * Testet den Use Case "Handhabung der aktuellen Position".
 * <strong>GPS muss aktiviert sein sowie eine Internetverbindung bestehen.</strong>
 *
 * @author Tristan Rust
 */
public class SendPositionSettingsTest extends ActivityInstrumentationTestCase2<ShowEventsActivity> {

    private ShowEventsActivity mActivity;

    // Test Daten zur Positionsübertragung
    private final String TEST_EMAIL        = "tristan.rust@googlemail.com";
    private final LatLng TEST_POSITION     = new LatLng(35.660866, 108.808594); // Somewhere in China

    // ShowEventsActivty UI Elemente
    private ListView mList;
    private ListAdapter mListData;

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
        // Alles in den SharedPreferences löschen
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();

        // Nutzer einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:"+ Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        Thread.sleep(5000); // Zeit zum initialisieren
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
     * Testet, ob die Activity weiter läuft, wenn diese pausiert hat. Large Test, da auf Ressourcen eines Servers, bzw.
     * anderen Netzwerkes abgerufen werden. <strong>GPS muss hierfür aktiviert sein.</strong>
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
     * Large Test, da auf Ressourcen eines Servers, bzw.
     * anderen Netzwerkes abgerufen werden.
     * <strong>GPS muss hierfür aktiviert sein.</strong>
     *
     * @throws java.lang.Exception
     */
    @LargeTest
    public void testSendPosition() throws Exception {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(Settings.class.getName(), null, false);

        // Klick auf die Menüoption
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.action_settings, 0);

        // UI Elemente lassen sich hier nicht ansprechen
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sharedPreferences.edit().putBoolean("prefSendLocation", true).apply();

        // Acitivty Monitor starten
        Activity am = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 2000);
        Thread.sleep(2500); // Zeit zum initialisieren

        // Setzen der Position auf den Server
        new UpdateLocationTask(TEST_EMAIL, TEST_POSITION.latitude, TEST_POSITION.longitude).execute();
        Thread.sleep(2000); // Zeit zum initialisieren

        // Teilnehmer, der seine Position an den Server senden wird
        Member m = ServiceProvider.getService().skatenightServerEndpoint().getMember(TEST_EMAIL).execute();
        assertEquals(TEST_POSITION.latitude, m.getLatitude());
        assertEquals(TEST_POSITION.longitude, m.getLongitude());

        // Prüfen, ob das Senden aktiviert ist
        boolean active = sharedPreferences.getBoolean("prefSendLocation", true);
        assertTrue(active);

        // Starten des Hintergrundservices zur Standortermittlung, GPS muss aktiv sein
        Intent service;
        service = new Intent(mActivity.getBaseContext(), LocationTransmitterService.class);
        service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startService(service);
        Thread.sleep(10000); // Zeit zum Senden

        // Prüfen, ob die neuen Positionen auf dem Server übernommen worden sind
        assertNotSame("Position not updated!", TEST_POSITION.latitude, m.getLatitude());
        assertNotSame("Position not updated!", TEST_POSITION.longitude, m.getLongitude());

        // Service wieder deaktivieren
        sharedPreferences.edit().putBoolean("prefSendLocation", false).apply();
        active = sharedPreferences.getBoolean("prefSendLocation", true);
        assertFalse(active);

        // Übertragenden Service stoppen
        mActivity.stopService(service);

        // Gibt an, ob der Service noch läuft
        boolean running = false;

        // Prüfen, ob der Service noch läuft
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(mActivity.getApplication().ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo services : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("LocationTransmitterService".equals(services.service.getClassName())) {
                running = true;
            }
        }

        assertFalse("Service should be stopped!", running);

        am.finish();

    }


}












