package ws1415.ps1415.useCases;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.UserLocation;

import ws1415.ps1415.Constants;
import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.ListEventsActivity;
import ws1415.ps1415.activity.SettingsActivity;
import ws1415.ps1415.controller.UserController;

/**
 * Testet den User Case "Übertragung der aktuellen Position an den Server".
 * <strong>Für diesen Test muss GPS aktiviert sein sowie eine Internetverbindung bestehen.</strong>
 *
 * @author Tristan Rust
 */
public class SendCurrentPositionTest extends ActivityInstrumentationTestCase2<ListEventsActivity> {

    private ListEventsActivity mActivity;

    // ListEventsActivity UI Elemente
    private ListView mList;
    private ListAdapter mListData;

    // Das zu testende Event
    private Event mEvent;

    // Test Daten zur positionsübertragung
    private final String TEST_EMAIL        = "tristan.rust@googlemail.com";
    private final LatLng TEST_POSITION     = new LatLng(35.660866, 108.808594); // Somewhere in China

    public SendCurrentPositionTest() {
        super(ListEventsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        // Die ListEventsActivity starten
        mActivity = getActivity();
        // Löschen der Voreinstellungen
        // Alles in den SharedPreferences löschen
        /*
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();*/
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sharedPreferences.edit().putBoolean("prefSendLocation", true).apply();

        // Nutzer einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:"+ Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        Thread.sleep(5000); // Zeit zum initialisieren
        // Holt sich die Event Listen-Elemente
//        mList = (ListView) mActivity.findViewById(R.id.eventList);
        mListData = mList.getAdapter();
        Thread.sleep(2000); // Zeit zum initialisieren

        mEvent = (Event) mListData.getItem(0);
    }

    /**
     * Prüfen, ob die Events in der Liste vorhanden sind, bzw. die UI Elemente initialisiert wurden.
     */
    @SmallTest
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
     * Prüft, ob die Position an den Server gesendet wird.
     * Der Test läuft nicht richtig über die UI, da das Senden und abrufen der Position an
     * den Server getestet werden sollen. Es wird lediglich noch überprüft, ob die Werte in den
     * Einstellungen zum Senden aktiviert sind. Large Test, da auf Ressourcen eines Servers, bzw.
     * anderen Netzwerkes abgerufen werden. <strong>GPS muss hierfür aktiviert sein.</strong>
     *
     * @throws java.lang.Exception
     */
    @LargeTest
    public void testUseCase() throws Exception {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(SettingsActivity.class.getName(), null, false);

        // Klick auf die Menüoption
        // Die Acitivity wird geändert, damit der Monitor für die Activity laufen kann, für die gleiche
        // kann kein Neuer Monitor erezeugt werden.
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.action_settings, 0);

        // Die Einstellungen setzen zum Senden, hat jedoch auf das Folgende keinen Einfluss, da die UI nicht getestet wird
        // Wird nur vollständigkeitshalber/konsistenz gesetzt
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sharedPreferences.edit().putBoolean("prefSendLocation", true).apply();

        // Acitivty Monitor starten
        Activity am = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 2000);
        Thread.sleep(2500); // Zeit zum initialisieren

        // Setzen der Position auf den Server
        UserController.updateUserLocation(null, TEST_EMAIL,  TEST_POSITION.latitude, TEST_POSITION.longitude, mEvent.getId());
        Thread.sleep(2000); // Zeit zum initialisieren

        // Teilnehmer, der seine Position an den Server senden wird
        UserLocation userLocation = ServiceProvider.getService().userEndpoint().getUserLocation(TEST_EMAIL).execute();
        assertEquals(TEST_POSITION.latitude, userLocation.getLatitude());
        assertEquals(TEST_POSITION.longitude, userLocation.getLongitude());

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
        assertNotSame("Position not updated!", TEST_POSITION.latitude, userLocation.getLatitude());
        assertNotSame("Position not updated!", TEST_POSITION.longitude, userLocation.getLongitude());

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

    /**
     * Testet, ob die Activity weiter läuft, wenn diese pausiert hat. Der Service sollte weiterhin
     * senden. Large Test, da auf Ressourcen eines Servers, bzw.
     * anderen Netzwerkes abgerufen werden. <strong>GPS muss hierfür aktiviert sein.</strong>
     *
     * @throws java.lang.InterruptedException
     */
    @LargeTest
    public void testUseCaseStatePause() throws InterruptedException {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(SettingsActivity.class.getName(), null, false);

        // Klick auf die Menüoption
        // Die Acitivity wird geändert, damit der Monitor für die Activity laufen kann, für die gleiche
        // kann kein Neuer Monitor erezeugt werden.
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.action_settings, 0);

        // Die Einstellungen setzen zum Senden, hat jedoch auf das Folgende keinen Einfluss, da die UI nicht getestet wird
        // Wird nur vollständigkeitshalber/konsistenz gesetzt
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        sharedPreferences.edit().putBoolean("prefSendLocation", true).apply();

        // Acitivty Monitor starten
        Activity am = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 2000);
        Thread.sleep(2500); // Zeit zum initialisieren

        // Prüfen, ob das Senden aktiviert ist
        boolean active = sharedPreferences.getBoolean("prefSendLocation", true);
        assertTrue(active);

        // Starten des Hintergrundservices zur Standortermittlung, GPS muss aktiv sein
        Intent service;
        service = new Intent(mActivity.getBaseContext(), LocationTransmitterService.class);
        service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startService(service);
        Thread.sleep(5000); // Zeit zum Senden

        // Gibt an, ob der Service noch läuft
        boolean running = false;

        // Prüfen, ob der Service noch läuft
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(mActivity.getApplication().ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo services : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (services.service.getClassName().contains("LocationTransmitterService")) {
                running = true;
            }
        }

        assertTrue("Service shouldn't be stopped!", running);

        // Pausieren
        getInstrumentation().callActivityOnPause(am);

        running = false;

        // Prüfen, ob der Service noch läuft
        manager = (ActivityManager) mActivity.getSystemService(mActivity.getApplication().ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo services : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (services.service.getClassName().contains("LocationTransmitterService")) {
                running = true;
            }
        }

        // Service sollte beim Pausieren trotzdem noch senden
        assertTrue("Service shouldn't be stopped during pause!", running);

        // Fortsetzen
        getInstrumentation().callActivityOnResume(am);

        am.finish();

    }

}





























