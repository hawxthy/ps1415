
package useCases;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.AddRouteTask;
import ws1415.veranstalterapp.Constants;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.activity.EditEventActivity;
import ws1415.veranstalterapp.activity.HoldTabsActivity;
import ws1415.veranstalterapp.adapter.EventsCursorAdapter;
import ws1415.veranstalterapp.fragment.AnnounceInformationFragment;
import ws1415.veranstalterapp.fragment.ShowEventsFragment;


/**
 * Testet den Use Case "Veröffentichen neuer Informationen". Dazu wird ein neues Event erstellt
 * und nach dem Erstellen bearbeitet. <strong> Es muss eine Internetverbingung bestehen.</strong>
 *
 * @author Tristan Rust
 */
public class PublishNewInformationTest extends ActivityInstrumentationTestCase2<HoldTabsActivity> {
    // UI Elemente
    private volatile boolean click;
    private EditText editTextTitle;
    private EditText editTextFee;
    private EditText editTextLocation;
    private EditText editTextDescription;

    private Button timePickerButton;
    private Button datePickerButton;
    private Button applyButton;
    private Button cancelButton;
    private Button routePickerButton;

    // Test Daten die beim Erstellen eines Events verwendet werden
    private static final String TEST_TITLE = "TestEvent";
    private static final String TEST_FEE = "10";
    private static final String TEST_LOCATION = "TestStadt";
    private static final String TEST_DESCRIPTION = "TestBeschreibung";
    // Hohes Datum, damit sichergestellt ist, dass das Event noch nicht begonnen hat. Das ist notwendig,
    // damit bei den Tests der User-App nach einem Klick auf Teilnehmer immer noch die ShowEventsActivity gezeigt wird
    private static final String TEST_DATE = "29.12.2016";
    private static final String TEST_TIME = "14:00 Uhr";
    private static final String TEST_ROUTE = "test";

    // Test Daten für die Bearbeitung eines Events
    private static final String TEST_TITLE_EDIT = "TestEventEdit";
    private static final String TEST_FEE_EDIT = "5";
    private static final String TEST_LOCATION_EDIT = "TestStadtEdit";
    private static final String TEST_DESCRIPTION_EDIT = "TestBeschreibungEdit";
    private static final String TEST_DATE_EDIT = "29.11.2014";
    private static final String TEST_TIME_EDIT = "15:00 Uhr";
    private static final String TEST_ROUTE_EDIT = "test";

    // Test Daten für den Test State Destroy
    private static final String TEST_STATE_DESTROY_TITLE = "TestEventDestroy";
    private static final String TEST_STATE_DESTROY_FEE = "500";
    private static final String TEST_STATE_DESTROY_LOCATION = "Whatever";
    private static final String TEST_STATE_DESTROY_DESCRIPTION = "Beschreibung";
    private static final String TEST_STATE_DESTROY_DATE = "31.12.2014";
    private static final String TEST_STATE_DESTROY_TIME = "12:00 Uhr";
    private static final String TEST_STATE_DESTROY_ROUTE = "test";

    private HoldTabsActivity mActivity;
    private ActionBar mActionBar;
    private ListView listView;

    // Wird zum Swipen nach links oder rechts verwendet
    public enum Direction {
        Left, Right;
    }

    public PublishNewInformationTest() {
        super(HoldTabsActivity.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Loggt den Veranstalter ein und wechselt auf das VeranstaltungErstellenTab.
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Nutzer verbinden, NICHT einloggen
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
        swipe(Direction.Left);

        // Initialisiere die ListView
        listView = ((AnnounceInformationFragment) mActivity.getAdapter().getItem(1)).getListView();

        // Initialisiere die View Elemente aus dem AnnounceInformationFramgent
        editTextTitle = (EditText) listView.getChildAt(0).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        editTextFee = (EditText) listView.getChildAt(1).findViewById(R.id.list_view_item_announce_information_fee_editText);
        editTextLocation = (EditText) listView.getChildAt(4).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        editTextDescription = (EditText) listView.getChildAt(6).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);

        // Initialisiere die Buttons aus dem AnncounceInformationFragment
        timePickerButton = (Button) listView.getChildAt(3).findViewById(R.id.list_view_item_announce_information_button_button);
        datePickerButton = (Button) listView.getChildAt(2).findViewById(R.id.list_view_item_announce_information_button_button);
        applyButton = (Button) mActivity.findViewById(R.id.announce_info_apply_button);
        cancelButton = (Button) mActivity.findViewById(R.id.announce_info_cancel_button);
        routePickerButton = (Button) listView.getChildAt(5).findViewById(R.id.list_view_item_announce_information_button_button);
    }

    /**
     * Swipt auf dem Bildschirm in die angegebenen Richtung.
     *
     * @param direction Die Richtung (links oder recths)
     */
    protected void swipe(Direction direction) {
        Instrumentation inst = getInstrumentation();
        Point size = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(size);
        // Bildschirmbreite
        int width = size.x;

        // Festellen in welche Richtung geswipt wird
        long downTime = SystemClock.uptimeMillis();
        float xStart = ((direction == Direction.Left) ? (width - 10) : 10);
        float xEnd = ((direction == Direction.Left) ? 10 : (width - 10));

        // Der Wert für die y verändert sich nicht. Swipe über die Instrumentation ausführen
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                             MotionEvent.ACTION_DOWN, xStart, size.y / 2, 0));
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                             MotionEvent.ACTION_MOVE, xEnd, size.y / 2, 0));
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis() + 1000,
                             MotionEvent.ACTION_UP, xEnd, size.y / 2, 0));
    }

    /**
     * Prüfen, ob alle Tabs vorhanden sind.
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

    /**
     * Testet den Use Case "Veröffentichen neuer Informationen". Dazu wird ein neues Event erstellt
     * und nach dem Erstellen bearbeitet. Large Test, da auf Ressourcen eines Servers, bzw.
     * anderen Netzwerkes zugegriffen wird.
     *
     * @throws Exception
     */
    @LargeTest
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

        final EditText editTextTitle = (EditText) listView.getChildAt(0).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextFee = (EditText) listView.getChildAt(1).findViewById(R.id.list_view_item_announce_information_fee_editText);
        final EditText editTextLocation = (EditText) listView.getChildAt(4).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextDescription = (EditText) listView.getChildAt(6).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);

        // Initialisiere die Buttons aus dem AnncounceInformationFragment
        final Button timePickerButton = (Button) listView.getChildAt(3).findViewById(R.id.list_view_item_announce_information_button_button);
        final Button datePickerButton = (Button) listView.getChildAt(2).findViewById(R.id.list_view_item_announce_information_button_button);
        final Button applyButton = (Button) mActivity.findViewById(R.id.announce_info_apply_button);
        final Button cancelButton = (Button) mActivity.findViewById(R.id.announce_info_cancel_button);
        final Button routePickerButton = (Button) listView.getChildAt(5).findViewById(R.id.list_view_item_announce_information_button_button);

        // TEST: ABBRECHEN
        // Es wird ein neuer UI Thread gestartet & die Textfelder werden ausgefüllt
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitle.setText(TEST_TITLE);
                editTextFee.setText(TEST_FEE);
                editTextLocation.setText(TEST_LOCATION);
                editTextDescription.setText(TEST_DESCRIPTION);
                datePickerButton.setText(TEST_DATE);
                timePickerButton.setText(TEST_TIME);
                routePickerButton.setText(TEST_ROUTE);
            }
        });
        Thread.sleep(5000); // Zeit zum initialisieren

        // Prüfen, ob die Daten jetzt eingefügt wurden
        assertEquals(TEST_TITLE, editTextTitle.getText().toString());
        assertEquals(TEST_FEE, editTextFee.getText().toString());
        assertEquals(TEST_LOCATION, editTextLocation.getText().toString());
        assertEquals(TEST_DESCRIPTION, editTextDescription.getText().toString());
        assertEquals(TEST_DATE, datePickerButton.getText().toString());
        assertEquals(TEST_TIME, timePickerButton.getText().toString());
        assertEquals(TEST_ROUTE, routePickerButton.getText().toString());

        click = false;
        // Abbrechen wodurch alle Felder zurück gesetzt werden sollten
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelButton.performClick();
                click = true;

            }
        });
        int time = 0;
        // Warten auf den Click
        while(!click && time < 50){
            Thread.sleep(100);
            time++;
            // exception machen
        }
        assertTrue("Timeout", time < 50);
        Thread.sleep(3000); // Zeit zum initialisieren

        final EditText editTextTitle2 = (EditText) listView.getChildAt(0).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextFee2 = (EditText) listView.getChildAt(1).findViewById(R.id.list_view_item_announce_information_fee_editText);
        final EditText editTextLocation2 = (EditText) listView.getChildAt(4).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextDescription2 = (EditText) listView.getChildAt(6).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);

        // Initialisiere die Buttons aus dem AnncounceInformationFragment
        final Button timePickerButton2 = (Button) listView.getChildAt(3).findViewById(R.id.list_view_item_announce_information_button_button);
        final Button datePickerButton2 = (Button) listView.getChildAt(2).findViewById(R.id.list_view_item_announce_information_button_button);
        final Button routePickerButton2 = (Button) listView.getChildAt(5).findViewById(R.id.list_view_item_announce_information_button_button);

        // Verwendet für den Test das aktuelle Datum & die Zeit
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Prüfen, ob die Daten jetzt wieder resettet wurden, nach dem der Test abgebrochen wurde
        assertEquals("", editTextTitle2.getText().toString());
        assertEquals("", editTextFee2.getText().toString());
        assertEquals("", editTextLocation2.getText().toString());
        assertEquals("", editTextDescription2.getText().toString());
        assertEquals(day + "" + (month + 1) + "." + year, datePickerButton2.getText().toString());
        assertEquals("20:00 Uhr", timePickerButton2.getText());
        assertEquals("Wähle Route", routePickerButton2.getText().toString());

        // TEST: ERSTELLEN
        // Fülle die Textfelder erneut aus
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitle2.setText(TEST_TITLE);
                editTextFee2.setText(TEST_FEE);
                editTextLocation2.setText(TEST_LOCATION);
                editTextDescription2.setText(TEST_DESCRIPTION);

                datePickerButton2.setText(TEST_DATE);
                timePickerButton2.setText(TEST_TIME);
                routePickerButton2.setText(TEST_ROUTE);

                // Hohes Datum für das Event setzen, damit es auf jeden Fall in der Zukunft beginnt
                // ((AnnounceInformationFragment) mActivity.getAdapter().getItem(1)).getAdapter().setYear(2016);
            }
        });
        Thread.sleep(5000); // Zeit zum initialisieren

        // Prüfen, ob die Daten jetzt eingefügt wurden
        assertEquals(TEST_TITLE, editTextTitle2.getText().toString());
        assertEquals(TEST_FEE, editTextFee2.getText().toString());
        assertEquals(TEST_LOCATION, editTextLocation2.getText().toString());
        assertEquals(TEST_DESCRIPTION, editTextDescription2.getText().toString());
        assertEquals(TEST_DATE, datePickerButton2.getText().toString());
        assertEquals(TEST_TIME, timePickerButton2.getText().toString());
        assertEquals(TEST_ROUTE, routePickerButton2.getText().toString());

        // Erstellt eine vordefinierte Testroute
        Route testRoute1;
        String routeName = Long.toString(System.currentTimeMillis());
        testRoute1 = new Route();
        testRoute1.setName(routeName);
        testRoute1.setLength("5 km");
        testRoute1.setRouteData(new Text().setValue("{sb|Hyamm@Ma@So@EKIWuFyPa@wAESYaAYmA]gB??LWLa@J]Fe@Fg@Bk@NcB@SBO@QDa@@K?K@I?KFoABi@FiALmDFcADiAJmC@Y???_@y@QC?OCOCI?ODYBUDa@Jc@Nc@RKFMJQPOTa@d@_BvB_@d@UXSPSLSJOFG@SDKB_@Dw@FQGs@DqBNK@QBO@OAWC_AMMC]Gg@IQCOA[CWAa@?m@@e@@mBJI@SBO@c@FSD_@HC@g@L_@L[LOHC@_@PKFWNMHOLIJGFGJEFGJEJA?CFENKd@Qp@CHIXK\\[v@Yr@OXaAtA[f@_@f@qCfEUZ??_@RYZWZGJMJKFIBIBUDsCCo@?y@?}@AI?U?aBDaBFcELoADiABi@B??GsEKmEEiDAi@SiMGsDGuBEoBCoEGmE?iCAsB@y@??SCIAc@C]@]FUBMDMFC@KFIFKHEDGHORWd@KPGLQ\\_@p@ABQPIFUNy@n@UPuB|A??IOGGk@SSG{C_A]I_@C_@EICQEi@Qe@M_@IKCGAG?S@Q@SBQBE?s@J}ARI@"));

        // Testroute auf dem Server speichern
        new AddRouteTask(null).execute(testRoute1).get();
        List<Route> list = null;
        try {
            list = ServiceProvider.getService().routeEndpoint().getRoutes().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < list.size(); i++) {
            if (routeName.equals(list.get(i).getName())) {
                testRoute1 = list.get(i);
                break;
            }
        }
        // Route muss final sein im UI Thread
        final Route testRoute2 = testRoute1;

        // Erstellen des Events
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //((AnnounceInformationFragment) mActivity.getAdapter().getItem(1)).getAdapter().setRouteAndText(testRoute2);
                applyButton.performClick();
            }
        });
        Thread.sleep(5000); // Zeit zum initialisieren

        final Button okButton = ((AnnounceInformationFragment) mActivity.getAdapter().getItem(1)).getLastDialog().getButton(AlertDialog.BUTTON_POSITIVE);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                okButton.performClick();
            }
        });
        Thread.sleep(5000); // Zeit zum initialisieren

        // Suche das neu angelegte Event
        List<Event> list2 = null;
        try {
            list2 = ServiceProvider.getService().eventEndpoint().getAllEvents().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean found = false;
        if (list2 != null) {
            for (Event e : list2) {
                if (e.getTitle().equals(TEST_TITLE)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        } else {
            assertTrue("Couldn't find any events!", found);
        }

        // TEST: EVENT Editieren
        swipe(Direction.Right);
        ShowEventsFragment mFragment = (ShowEventsFragment)mActivity.getAdapter().getItem(0);
        EventsCursorAdapter mAdapter = mFragment.getmAdapter();
        List<Event> eventList = mAdapter.getEventList();
        int timeout = 100;
        while (timeout > 0 && eventList == null) {
            eventList = mAdapter.getEventList();
            timeout--;
            Thread.sleep(100);
        }
        assertTrue("timeout reached", timeout > 0);

        // Eben erstelltes Event suchen
        int pos = -1;
        found = false;
        for(Event e : eventList){
            pos++;
            if(TEST_TITLE.equals(e.getTitle())){
                found = true;
                break;
            }

        }
        assertTrue("Couldn't find the event!", found);

        // Editiere das gefundene Event
        mFragment.editEvent(eventList.get(pos));

        // Auf die EditEventActivity warten und initialisieren der Instanz
        Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(EditEventActivity.class.getName(), null, false);
        EditEventActivity editEventActivity = (EditEventActivity) am.waitForActivityWithTimeout(20000);

        Thread.sleep(5000); // Zeit zum initalisieren

        final ListView eventFields = editEventActivity.getListView();

        final EditText editTextTitleEdit = (EditText) eventFields.getChildAt(0).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextFeeEdit = (EditText) eventFields.getChildAt(1).findViewById(R.id.list_view_item_announce_information_fee_editText);
        final EditText editTextLocationEdit = (EditText) eventFields.getChildAt(4).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextDescriptionEdit = (EditText) eventFields.getChildAt(6).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);

        // Fülle die Textfelder erneut aus
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitleEdit.setText(TEST_TITLE_EDIT);
                editTextFeeEdit.setText(TEST_FEE_EDIT);
                editTextLocationEdit.setText(TEST_LOCATION_EDIT);
                editTextDescriptionEdit.setText(TEST_DESCRIPTION_EDIT);
            }
        });
        Thread.sleep(3000);

        // Prüfen, ob die Daten verändert wurden
        assertEquals(TEST_TITLE_EDIT, editTextTitleEdit.getText().toString());
        assertEquals(TEST_FEE_EDIT, editTextFeeEdit.getText().toString());
        assertEquals(TEST_LOCATION_EDIT, editTextLocationEdit.getText().toString());
        assertEquals(TEST_DESCRIPTION_EDIT, editTextDescriptionEdit.getText().toString());

        final Button applyEditButton = (Button) editEventActivity.findViewById(R.id.edit_event_info_apply_button);

        click = false;
        // Abbrechen wodurch alle Felder zurück gesetzt werden sollten
        editEventActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                applyEditButton.performClick();
                click = true;

            }
        });
        time = 0;
        // Warten auf den Click
        while(!click && time < 50){
            Thread.sleep(100);
            time++;
            // exception machen
        }
        assertTrue("Timeout", time < 50);

        AlertDialog dialog = editEventActivity.getLastDialog();
        final Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

        click = false;
        // Bestätigen des Editierens des Events
        editEventActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                positiveButton.performClick();
                click = true;
            }
        });
        time = 0;
        // Warten auf den Click
        while(!click && time < 50){
            Thread.sleep(100);
            time++;
        }
        assertTrue("Timeout", time < 50);
        Thread.sleep(7000);

        mAdapter = mFragment.getmAdapter();
        eventList = mAdapter.getEventList();

        // Suche nach dem eben editierten Event
        pos = -1;
        found = false;
        for(Event e : eventList){
            pos++;
            if(TEST_TITLE_EDIT.equals(e.getTitle())){
                found = true;
                break;
            }

        }
        assertTrue("Couldn't find the event!", found);

        // Prüfen, ob die Daten verändert wurden
        assertEquals(TEST_TITLE_EDIT, eventList.get(pos).getTitle());
        assertEquals(TEST_FEE_EDIT, eventList.get(pos).getFee());
        assertEquals(TEST_LOCATION_EDIT, eventList.get(pos).getMeetingPlace());
        assertEquals(TEST_DESCRIPTION_EDIT, eventList.get(pos).getDescription().getValue());

        swipe(Direction.Left);

        // Auskommentiert, damit das hier angelegte Event in der App auch sichtbar ist
        // mFragment.deleteEvent(eventList.get(pos));
        // ManageRoutesFragment fragment = (ManageRoutesFragment)mActivity.getAdapter().getItem(2);
        // fragment.deleteRoute(testRoute2);
    }

    /**
     * Testet die Activity beim Beenden und wieder Neustarten.
     */
    @SmallTest
    public void testUseCaseStateDestroy() throws Exception{
        final EditText editTextTitle = (EditText) listView.getChildAt(0).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextFee = (EditText) listView.getChildAt(1).findViewById(R.id.list_view_item_announce_information_fee_editText);
        final EditText editTextLocation = (EditText) listView.getChildAt(4).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextDescription = (EditText) listView.getChildAt(6).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);

        // Initialisiere die Buttons aus dem AnncounceInformationFragment
        final Button timePickerButton = (Button) listView.getChildAt(3).findViewById(R.id.list_view_item_announce_information_button_button);
        final Button datePickerButton = (Button) listView.getChildAt(2).findViewById(R.id.list_view_item_announce_information_button_button);
        final Button routePickerButton = (Button) listView.getChildAt(5).findViewById(R.id.list_view_item_announce_information_button_button);

        // Setzen der Testwerte
        mActivity = this.getActivity();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitle.setText(TEST_STATE_DESTROY_TITLE);
                editTextFee.setText(TEST_STATE_DESTROY_FEE);
                editTextLocation.setText(TEST_STATE_DESTROY_LOCATION);
                editTextDescription.setText(TEST_STATE_DESTROY_DESCRIPTION);
                datePickerButton.setText(TEST_STATE_DESTROY_DATE);
                timePickerButton.setText(TEST_STATE_DESTROY_TIME);
                routePickerButton.setText(TEST_STATE_DESTROY_ROUTE);
            }
        });
        Thread.sleep(3000); // Zeit zum initalisieren

        // Beenden & Neustarten der Activity
        mActivity.finish();
        mActivity = this.getActivity();

        // Abrufen der aktuellen Werte
        String currentTitle = editTextTitle.getText().toString();
        String currentFee = editTextFee.getText().toString();
        String currentLocation = editTextLocation.getText().toString();
        String currentDescription = editTextDescription.getText().toString();
        String currentDate = datePickerButton.getText().toString();
        String currentTime = timePickerButton.getText().toString();
        String currentRoute = routePickerButton.getText().toString();

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
     * Testet, ob die Activity weiter läuft, wenn diese pausiert hat.
     * Test läuft auf dem UI Thread.
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

        // TEST: DATEN EINFÜGEN & DIE APP PAUSIEREN
        // Es wird ein neuer UI Thread gestartet & fülle die Textfelder aus
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitle.setText(TEST_TITLE);
                editTextFee.setText(TEST_FEE);
                editTextLocation.setText(TEST_LOCATION);
                editTextDescription.setText(TEST_DESCRIPTION);

                datePickerButton.setText(TEST_DATE);
                timePickerButton.setText(TEST_TIME);
                routePickerButton.setText(TEST_ROUTE);
            }
        });

        // Prüfen, ob die Daten jetzt eingefügt wurden
        assertEquals("Title not added!", TEST_TITLE, editTextTitle.getText().toString());
        assertEquals("Fee not added!", TEST_FEE, editTextFee.getText().toString());
        assertEquals("Location not added!", TEST_LOCATION, editTextLocation.getText().toString());
        assertEquals("Description not added!", TEST_DESCRIPTION, editTextDescription.getText().toString());
        assertEquals("Date not added!", TEST_DATE, datePickerButton.getText().toString());
        assertEquals("Time not added!", TEST_TIME, timePickerButton.getText().toString());
        assertEquals("Route not added!", TEST_ROUTE, routePickerButton.getText().toString());

        // Pausieren
        instrumentation.callActivityOnPause(mActivity);

        // Fortsetzen
        instrumentation.callActivityOnResume(mActivity);

        // Prüfen, ob die gleichen Daten noch in den Feldern/Buttons stehen wie vor dem pausieren
        assertEquals("Title not equal!", TEST_TITLE, editTextTitle.getText().toString());
        assertEquals("Fee not equal!", TEST_FEE, editTextFee.getText().toString());
        assertEquals("Location not equal!", TEST_LOCATION, editTextLocation.getText().toString());
        assertEquals("Description not equal!", TEST_DESCRIPTION, editTextDescription.getText().toString());
        assertEquals("Date not equal!", TEST_DATE, datePickerButton.getText().toString());
        assertEquals("Time not equal!", TEST_TIME, timePickerButton.getText().toString());
        assertEquals("Route not equal!", TEST_ROUTE, routePickerButton.getText().toString());
    }
}


















