package useCases;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Instrumentation;
import android.graphics.Point;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.UiThreadTest;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import ws1415.veranstalterapp.Constants;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.activity.HoldTabsActivity;
import ws1415.veranstalterapp.activity.LoginActivity;
import ws1415.veranstalterapp.fragment.AnnounceInformationFragment;

/**
 * Testet den Use Case "Veröffentichen neuer Informationen".
 *
 * @author Tristan Rust
 */
public class PublishNewInformationTest extends ActivityInstrumentationTestCase2<HoldTabsActivity> {

    private static final String TEST_STATE_DESTROY_TITLE = "TestEventDestroy";
    private static final String TEST_STATE_DESTROY_FEE = "500";
    private static final String TEST_STATE_DESTROY_LOCATION = "Whatever";
    private static final String TEST_STATE_DESTROY_DESCRIPTION = "Beschreibung";
    private static final String TEST_STATE_DESTROY_DATE = "31.12.2014";
    private static final String TEST_STATE_DESTROY_TIME = "12:00 Uhr";
    private static final String TEST_STATE_DESTROY_ROUTE = "test";

    private HoldTabsActivity mActivity;
    private ActionBar mActionBar;

    // Wird zum Swipen nach links oder rechts verwendet
    public enum Direction {
        Left, Right;
    }

    public PublishNewInformationTest() {
        super(HoldTabsActivity.class);
    }

    /**
     * Loggt den Veranstalter ein und wechselt auf das VeranstaltungErstellenTab.
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Nutzer einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        // Touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        // Die HoldTabsActivity wird gestartet
        mActivity = getActivity();
        // ActionBar der TabActivity holen
        mActionBar = mActivity.getActionBar();
        // Swipe zum "Veranstaltung erstellen" Tab
        swipe(Direction.Right);
    }

    /**
     * Swipt auf dem Bildschirm.
     * @param direction Die Richtung (links oder recths)
     */
    protected void swipe(Direction direction) {
        Instrumentation inst = getInstrumentation();
        Point size = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;

        // Festellen in welche Richtung
        long downTime = SystemClock.uptimeMillis();
        float xStart = ((direction == Direction.Left) ? (width - 10) : 10);
        float xEnd = ((direction == Direction.Left) ? 10 : (width - 10));

        // Der Wert für die y verändert sich nicht
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, xStart, size.y / 2, 0));
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE, xEnd, size.y / 2, 0));
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis() + 1000,
                MotionEvent.ACTION_UP, xEnd, size.y / 2, 0));
    }

    /**
     * Prüfen, ob alle Tabs vorhanden sind
     */
    @SmallTest
    public void testPreConditions() {
        assertEquals(ActionBar.NAVIGATION_MODE_TABS, mActionBar.getNavigationMode());
        assertEquals(3, mActionBar.getNavigationItemCount());
    }

    /**
     * Prüft, ob diese Views wirklich in der Activity existieren.
     */
    @SmallTest
    public void testViews() {
        assertNotNull(mActivity);

        // Initialisiere die View Elemente aus dem AnnounceInformationFramgent
        final EditText editTextTitle          = (EditText) mActivity.findViewById(R.id.announce_info_title_edittext);
        final EditText editTextFee            = (EditText) mActivity.findViewById(R.id.announce_info_fee_edittext);
        final EditText editTextLocation       = (EditText) mActivity.findViewById(R.id.announce_info_location_edittext);
        final EditText editTextDescription    = (EditText) mActivity.findViewById(R.id.announce_info_description_edittext);

        // Initialisiere die Buttons aus dem AnncounceInformationFragment
        final Button timePickerButton     = (Button) mActivity.findViewById(R.id.announce_info_time_button);
        final Button datePickerButton     = (Button) mActivity.findViewById(R.id.announce_info_date_button);
        Button applyButton                = (Button) mActivity.findViewById(R.id.announce_info_apply_button);
        Button cancelButton               = (Button) mActivity.findViewById(R.id.announce_info_cancel_button);
        final Button routePickerButton    = (Button) mActivity.findViewById(R.id.announce_info_choose_route);

        assertNotNull(editTextTitle);
        assertNotNull(editTextFee);
        assertNotNull(editTextLocation);
        assertNotNull(editTextDescription);
        assertNotNull(timePickerButton);
        assertNotNull(datePickerButton);
        assertNotNull(applyButton);
        assertNotNull(cancelButton);
        assertNotNull(routePickerButton);
    }

    /**
     * Prüft, ob die Elemente in der Activity wirklich gerendert werden.
     *
     * @throws java.lang.Exception
     */
    @SmallTest
    public void testViewsVisible() throws Exception {
        // Initialisiere die View Elemente aus dem AnnounceInformationFramgent
        final EditText editTextTitle          = (EditText) mActivity.findViewById(R.id.announce_info_title_edittext);
        final EditText editTextFee            = (EditText) mActivity.findViewById(R.id.announce_info_fee_edittext);
        final EditText editTextLocation       = (EditText) mActivity.findViewById(R.id.announce_info_location_edittext);
        final EditText editTextDescription    = (EditText) mActivity.findViewById(R.id.announce_info_description_edittext);

        // Initialisiere die Buttons aus dem AnncounceInformationFragment
        final Button timePickerButton     = (Button) mActivity.findViewById(R.id.announce_info_time_button);
        final Button datePickerButton     = (Button) mActivity.findViewById(R.id.announce_info_date_button);
        Button applyButton                = (Button) mActivity.findViewById(R.id.announce_info_apply_button);
        Button cancelButton               = (Button) mActivity.findViewById(R.id.announce_info_cancel_button);
        final Button routePickerButton    = (Button) mActivity.findViewById(R.id.announce_info_choose_route);

        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), editTextTitle);
        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), editTextFee);
        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), editTextLocation);
        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), editTextDescription);

        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), timePickerButton);
        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), datePickerButton);
        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), applyButton);
        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), cancelButton);
        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), routePickerButton);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Testet den UseCase.
     * @throws Exception
     */
    @UiThreadTest
    public void testUseCase() throws Exception {
        // Prüfen, ob alle Tabs vorhanden sind
        assertEquals(ActionBar.NAVIGATION_MODE_TABS, mActionBar.getNavigationMode());
        assertEquals(3, mActionBar.getNavigationItemCount());

        // Prüfen, ob Tab0 richtig angelegt wurde
        ActionBar.Tab tab0 = mActionBar.getTabAt(0);
        assertNotNull(tab0);
        assertNotNull(tab0.getText());
        assertEquals(mActivity.getBaseContext().getString(R.string.title_fragment_show_events), tab0.getText());

        // Prüfen, ob Tab1 richtig angelegt wurde
        ActionBar.Tab tab1 = mActionBar.getTabAt(1);
        assertNotNull(tab1);
        assertNotNull(tab1.getText());
        assertEquals(mActivity.getBaseContext().getString(R.string.title_fragment_announce_information), tab1.getText());

        // Prüfen, ob Tab2 richtig angelegt wurde
        ActionBar.Tab tab2 = mActionBar.getTabAt(2);
        assertNotNull(tab2);
        assertNotNull(tab2.getText());
        assertEquals(mActivity.getBaseContext().getString(R.string.title_fragment_manage_routes), tab2.getText());

        // Wechsle für den UseCase auf den zweiten Tab (tab1)
        mActionBar.selectTab(tab1);
        assertEquals(1, mActionBar.getSelectedNavigationIndex());

        // Initialisiere die View Elemente aus dem AnnounceInformationFragment
        final EditText editTextTitle          = (EditText) mActivity.findViewById(R.id.announce_info_title_edittext);
        final EditText editTextFee            = (EditText) mActivity.findViewById(R.id.announce_info_fee_edittext);
        final EditText editTextLocation       = (EditText) mActivity.findViewById(R.id.announce_info_location_edittext);
        final EditText editTextDescription    = (EditText) mActivity.findViewById(R.id.announce_info_description_edittext);

        // Initialisiere die Buttons aus dem AnncounceInformationFragment
        final Button timePickerButton     = (Button) mActivity.findViewById(R.id.announce_info_time_button);
        final Button datePickerButton     = (Button) mActivity.findViewById(R.id.announce_info_date_button);
        final Button applyButton                = (Button) mActivity.findViewById(R.id.announce_info_apply_button);
        Button cancelButton               = (Button) mActivity.findViewById(R.id.announce_info_cancel_button);
        final Button routePickerButton    = (Button) mActivity.findViewById(R.id.announce_info_choose_route);

        // Testdaten
        final String textTitle       = "TestEvent";
        final String textFee         = "10";
        final String textLocation    = "TestStadt";
        final String textDescription = "TestBeschreibung";
        final String textDate        = "29.12.2014";
        final String textTime        = "14:00 Uhr";
        final String textRoute       = "test";

        // TEST ABBRECHEN
        // Es wird ein neuer UI Thread gestartet & die Textfelder werden ausgefüllt
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitle.setText(textTitle);
                editTextFee.setText(textFee);
                editTextLocation.setText(textLocation);
                editTextDescription.setText(textDescription);

                datePickerButton.setText(textDate);
                timePickerButton.setText(textTime);
                routePickerButton.setText(textRoute);
            }
        });

        // Prüfen, ob die Daten jetzt eingefügt wurden
        assertEquals(textTitle, editTextTitle.getText().toString());
        assertEquals(textFee, editTextFee.getText().toString());
        assertEquals(textLocation, editTextLocation.getText().toString());
        assertEquals(textDescription, editTextDescription.getText().toString());
        assertEquals(textDate, datePickerButton.getText().toString());
        assertEquals(textTime, timePickerButton.getText().toString());
        assertEquals(textRoute, routePickerButton.getText().toString());

        // Abbrechen wodurch alle Felder zurück gesetzt werden sollten
        cancelButton.performClick();

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Prüfen, ob die Daten jetzt wieder resettet wurden
        assertEquals("", editTextTitle.getText().toString());
        assertEquals("", editTextFee.getText().toString());
        assertEquals("", editTextLocation.getText().toString());
        assertEquals("", editTextDescription.getText().toString());
        assertEquals(day + "." + (month + 1) + "." + year, datePickerButton.getText().toString());
        assertEquals("Wähle die Uhrzeit", timePickerButton.getText());
        assertEquals("Wähle Route", routePickerButton.getText().toString());

        // TEST ERSTELLEN
        // Fülle die Textfelder erneut aus
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitle.setText(textTitle);
                editTextFee.setText(textFee);
                editTextLocation.setText(textLocation);
                editTextDescription.setText(textDescription);

                datePickerButton.setText(textDate);
                timePickerButton.setText(textTime);
                routePickerButton.setText(textRoute);
            }
        });

        // Prüfen, ob die Daten jetzt eingefügt wurden
        assertEquals(textTitle, editTextTitle.getText().toString());
        assertEquals(textFee, editTextFee.getText().toString());
        assertEquals(textLocation, editTextLocation.getText().toString());
        assertEquals(textDescription, editTextDescription.getText().toString());
        assertEquals(textDate, datePickerButton.getText().toString());
        assertEquals(textTime, timePickerButton.getText().toString());
        assertEquals(textRoute, routePickerButton.getText().toString());

        // Erstellen des Events
        applyButton.performClick();


        // Neuer Thread, da es im UiThread ansonsten zu einem DeadLock kommen könnte
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Suche das neu angelegte Event
                List<Event> list = null;
                try {
                    list = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents().execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                boolean found = false;
                if (list != null) {
                    for (Event e : list) {
                        if (e.getKey().getName() == textTime) {
                            found = true;
                            break;
                        }
                    }
                    assertTrue(found);
                } else {
                    assertTrue("Couldn't find any events!", found);
                }
            }
        }).start();
    }

    /**
     * Testet die Activity beim Beenden und wieder Neustarten.
     */
    @SmallTest
    public void testUseCaseStateDestroy() {
        // Initialisiere die View Elemente aus dem AnnounceInformationFramgent
        final EditText editTextTitle          = (EditText) mActivity.findViewById(R.id.announce_info_title_edittext);
        final EditText editTextFee            = (EditText) mActivity.findViewById(R.id.announce_info_fee_edittext);
        final EditText editTextLocation       = (EditText) mActivity.findViewById(R.id.announce_info_location_edittext);
        final EditText editTextDescription    = (EditText) mActivity.findViewById(R.id.announce_info_description_edittext);

        // Initialisiere die Buttons aus dem AnncounceInformationFragment
        final Button timePickerButton     = (Button) mActivity.findViewById(R.id.announce_info_time_button);
        final Button datePickerButton     = (Button) mActivity.findViewById(R.id.announce_info_date_button);
        Button applyButton                = (Button) mActivity.findViewById(R.id.announce_info_apply_button);
        Button cancelButton               = (Button) mActivity.findViewById(R.id.announce_info_cancel_button);
        final Button routePickerButton    = (Button) mActivity.findViewById(R.id.announce_info_choose_route);

        // Testdaten
        final String textTitle      = "TestEventDestroy";
        final String textFee        = "500";
        final String textLocation   = "Whatever";
        final String textDescription = "Beschreibung";
        final String textDate       = "31.12.2014";
        final String textTime       = "12:00 Uhr";
        final String textRoute      = "test";

        // Setzen der Testwerte
        editTextTitle.setText(textTitle);
        editTextFee.setText(textFee);
        editTextLocation.setText(textLocation);
        editTextDescription.setText(textDescription);
        datePickerButton.setText(textDate);
        timePickerButton.setText(textTime);
        routePickerButton.setText(textRoute);

        // Beenden & Neustarten der Activity
        mActivity.finish();
        mActivity = this.getActivity();

        // Abrufen der aktuellen Werte
        String currentTitle         = editTextTitle.getText().toString();
        String currentFee           = editTextFee.getText().toString();
        String currentLocation      = editTextLocation.getText().toString();
        String currentDescription   = editTextDescription.getText().toString();
        String currentDate          = datePickerButton.getText().toString();
        String currentTime          = timePickerButton.getText().toString();
        String currentRoute         = routePickerButton.getText().toString();

        // Testen der aktuellen Werte mit den Testwerten
        assertEquals("Current title incorrect!", TEST_STATE_DESTROY_TITLE, currentTitle);
        assertEquals("Current fee incorrect!", TEST_STATE_DESTROY_FEE, currentFee);
        assertEquals("Current location incorrect!", TEST_STATE_DESTROY_LOCATION, currentLocation);
        assertEquals("Current description incorrect!", TEST_STATE_DESTROY_DESCRIPTION, currentDescription);
        assertEquals("Current date incorrect!", TEST_STATE_DESTROY_DATE, currentDate);
        assertEquals("Current time incorrect!", TEST_STATE_DESTROY_TIME, currentTime);
        assertEquals("Current route incorrect!", TEST_STATE_DESTROY_ROUTE, currentRoute);

    }

    /**
     * Testet, ob die Activity weiter läuft, wenn diese pausiert hat. Test läuft auf dem UI Thread.
     */
    @UiThreadTest
    public void testUseCaseStatePauseResume() {
        // Instrumentation Objekt, das die Anwendung während des Tests kontrolliert
        Instrumentation instrumentation = this.getInstrumentation();

        // Prüfen, ob alle Tabs vorhanden sind
        assertEquals(ActionBar.NAVIGATION_MODE_TABS, mActionBar.getNavigationMode());
        assertEquals(3, mActionBar.getNavigationItemCount());

        // Prüfen, ob Tab0 richtig angelegt wurde
        ActionBar.Tab tab0 = mActionBar.getTabAt(0);
        assertNotNull(tab0);
        assertNotNull(tab0.getText());
        assertEquals(mActivity.getBaseContext().getString(R.string.title_fragment_show_events), tab0.getText());

        // Prüfen, ob Tab1 richtig angelegt wurde
        ActionBar.Tab tab1 = mActionBar.getTabAt(1);
        assertNotNull(tab1);
        assertNotNull(tab1.getText());
        assertEquals(mActivity.getBaseContext().getString(R.string.title_fragment_announce_information), tab1.getText());

        // Prüfen, ob Tab2 richtig angelegt wurde
        ActionBar.Tab tab2 = mActionBar.getTabAt(2);
        assertNotNull(tab2);
        assertNotNull(tab2.getText());
        assertEquals(mActivity.getBaseContext().getString(R.string.title_fragment_manage_routes), tab2.getText());

        // Wechsle für den UseCase auf den zweiten Tab (tab1)
        mActionBar.selectTab(tab1);
        assertEquals(1, mActionBar.getSelectedNavigationIndex());

        // Initialisiere die View Elemente aus dem AnnounceInformationFramgent
        final EditText editTextTitle          = (EditText) mActivity.findViewById(R.id.announce_info_title_edittext);
        final EditText editTextFee            = (EditText) mActivity.findViewById(R.id.announce_info_fee_edittext);
        final EditText editTextLocation       = (EditText) mActivity.findViewById(R.id.announce_info_location_edittext);
        final EditText editTextDescription    = (EditText) mActivity.findViewById(R.id.announce_info_description_edittext);

        // Initialisiere die Buttons aus dem AnncounceInformationFragment
        final Button timePickerButton     = (Button) mActivity.findViewById(R.id.announce_info_time_button);
        final Button datePickerButton     = (Button) mActivity.findViewById(R.id.announce_info_date_button);
        Button applyButton          = (Button) mActivity.findViewById(R.id.announce_info_apply_button);
        Button cancelButton         = (Button) mActivity.findViewById(R.id.announce_info_cancel_button);
        final Button routePickerButton    = (Button) mActivity.findViewById(R.id.announce_info_choose_route);

        // Testdaten
        final String textTitle      = "TestEvent";
        final String textFee        = "10";
        final String textLocation   = "TestStadt";
        final String textDescription = "TestBeschreibung";
        final String textDate       = "29.12.2014";
        final String textTime       = "14:00 Uhr";
        final String textRoute      = "test";

        // TEST DATEN EINFÜGEN
        // Es wird ein neuer UI Thread gestartet & fülle die Textfelder aus
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitle.setText(textTitle);
                editTextFee.setText(textFee);
                editTextLocation.setText(textLocation);
                editTextDescription.setText(textDescription);

                datePickerButton.setText(textDate);
                timePickerButton.setText(textTime);
                routePickerButton.setText(textRoute);
            }
        });

        // Prüfen, ob die Daten jetzt eingefügt wurden
        assertEquals("Title not added!", textTitle, editTextTitle.getText().toString());
        assertEquals("Fee not added!", textFee, editTextFee.getText().toString());
        assertEquals("Location not added!", textLocation, editTextLocation.getText().toString());
        assertEquals("Description not added!", textDescription, editTextDescription.getText().toString());
        assertEquals("Date not added!", textDate, datePickerButton.getText().toString());
        assertEquals("Time not added!", textTime, timePickerButton.getText().toString());
        assertEquals("Route not added!", textRoute, routePickerButton.getText().toString());

        // Pausieren
        instrumentation.callActivityOnPause(mActivity);

        // Fortsetzen
        instrumentation.callActivityOnResume(mActivity);

        // Prüfen, ob die gleichen Daten noch in den Feldern/Buttons stehen wie vor dem pausieren
        assertEquals("Title not equal!", textTitle, editTextTitle.getText().toString());
        assertEquals("Fee not equal!", textFee, editTextFee.getText().toString());
        assertEquals("Location not equal!", textLocation, editTextLocation.getText().toString());
        assertEquals("Description not equal!", textDescription, editTextDescription.getText().toString());
        assertEquals("Date not equal!", textDate, datePickerButton.getText().toString());
        assertEquals("Time not equal!", textTime, timePickerButton.getText().toString());
        assertEquals("Route not equal!", textRoute, routePickerButton.getText().toString());

    }

}

















