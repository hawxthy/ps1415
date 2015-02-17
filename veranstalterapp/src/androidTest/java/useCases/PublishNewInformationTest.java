
package useCases;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import ws1415.veranstalterapp.Constants;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.activity.AddHostDialog;
import ws1415.veranstalterapp.activity.EditEventActivity;
import ws1415.veranstalterapp.activity.HoldTabsActivity;
import ws1415.veranstalterapp.adapter.EventsCursorAdapter;
import ws1415.veranstalterapp.fragment.AnnounceInformationFragment;
import ws1415.veranstalterapp.fragment.ManageRoutesFragment;
import ws1415.veranstalterapp.fragment.ShowEventsFragment;
import ws1415.veranstalterapp.task.AddRouteTask;
import ws1415.veranstalterapp.task.QueryRouteTask;
import ws1415.veranstalterapp.util.EventUtils;
import ws1415.veranstalterapp.util.FieldType;


/**
 * Testet den Use Case "Veröffentichen neuer Informationen".
 *
 * @author Tristan Rust
 */

public class PublishNewInformationTest extends ActivityInstrumentationTestCase2<HoldTabsActivity> {
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
     * Swipt auf dem Bildschirm.
     *
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

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Testet den UseCase.
     *
     * @throws Exception
     */
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

        // Testdaten
        final String textTitle = "TestEvent";
        final String textFee = "10";
        final String textLocation = "TestStadt";
        final String textDescription = "TestBeschreibung";
        final String textDate = "29.12.2014";
        final String textTime = "14:00 Uhr";
        final String textRoute = "test";

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
        Thread.sleep(3000);

        // Prüfen, ob die Daten jetzt eingefügt wurden
        assertEquals(textTitle, editTextTitle.getText().toString());
        assertEquals(textFee, editTextFee.getText().toString());
        assertEquals(textLocation, editTextLocation.getText().toString());
        assertEquals(textDescription, editTextDescription.getText().toString());
        assertEquals(textDate, datePickerButton.getText().toString());
        assertEquals(textTime, timePickerButton.getText().toString());
        assertEquals(textRoute, routePickerButton.getText().toString());

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
        Thread.sleep(3000);


        final EditText editTextTitle2 = (EditText) listView.getChildAt(0).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextFee2 = (EditText) listView.getChildAt(1).findViewById(R.id.list_view_item_announce_information_fee_editText);
        final EditText editTextLocation2 = (EditText) listView.getChildAt(4).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextDescription2 = (EditText) listView.getChildAt(6).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);

        // Initialisiere die Buttons aus dem AnncounceInformationFragment
        final Button timePickerButton2 = (Button) listView.getChildAt(3).findViewById(R.id.list_view_item_announce_information_button_button);
        final Button datePickerButton2 = (Button) listView.getChildAt(2).findViewById(R.id.list_view_item_announce_information_button_button);
        final Button routePickerButton2 = (Button) listView.getChildAt(5).findViewById(R.id.list_view_item_announce_information_button_button);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Prüfen, ob die Daten jetzt wieder resettet wurden
        assertEquals("", editTextTitle2.getText().toString());
        assertEquals("", editTextFee2.getText().toString());
        assertEquals("", editTextLocation2.getText().toString());
        assertEquals("", editTextDescription2.getText().toString());
        assertEquals(day + "." + (month + 1) + "." + year, datePickerButton2.getText().toString());
        assertEquals("20:00 Uhr", timePickerButton2.getText());
        assertEquals("Wähle Route", routePickerButton2.getText().toString());

        // TEST ERSTELLEN
        // Fülle die Textfelder erneut aus
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitle2.setText(textTitle);
                editTextFee2.setText(textFee);
                editTextLocation2.setText(textLocation);
                editTextDescription2.setText(textDescription);

                datePickerButton2.setText(textDate);
                timePickerButton2.setText(textTime);
                routePickerButton2.setText(textRoute);
            }
        });
        Thread.sleep(3000);

        // Prüfen, ob die Daten jetzt eingefügt wurden
        assertEquals(textTitle, editTextTitle2.getText().toString());
        assertEquals(textFee, editTextFee2.getText().toString());
        assertEquals(textLocation, editTextLocation2.getText().toString());
        assertEquals(textDescription, editTextDescription2.getText().toString());
        assertEquals(textDate, datePickerButton2.getText().toString());
        assertEquals(textTime, timePickerButton2.getText().toString());
        assertEquals(textRoute, routePickerButton2.getText().toString());

        // Route bauen
        Route testRoute1;
        String routename = Long.toString(System.currentTimeMillis());
        testRoute1 = new Route();
        testRoute1.setName(routename);
        testRoute1.setLength("5 km");
        testRoute1.setRouteData(new Text().setValue("{sb|Hyamm@Ma@So@EKIWuFyPa@wAESYaAYmA]gB??LWLa@J]Fe@Fg@Bk@NcB@SBO@QDa@@K?K@I?KFoABi@FiALmDFcADiAJmC@Y???_@y@QC?OCOCI?ODYBUDa@Jc@Nc@RKFMJQPOTa@d@_BvB_@d@UXSPSLSJOFG@SDKB_@Dw@FQGs@DqBNK@QBO@OAWC_AMMC]Gg@IQCOA[CWAa@?m@@e@@mBJI@SBO@c@FSD_@HC@g@L_@L[LOHC@_@PKFWNMHOLIJGFGJEFGJEJA?CFENKd@Qp@CHIXK\\[v@Yr@OXaAtA[f@_@f@qCfEUZ??_@RYZWZGJMJKFIBIBUDsCCo@?y@?}@AI?U?aBDaBFcELoADiABi@B??GsEKmEEiDAi@SiMGsDGuBEoBCoEGmE?iCAsB@y@??SCIAc@C]@]FUBMDMFC@KFIFKHEDGHORWd@KPGLQ\\_@p@ABQPIFUNy@n@UPuB|A??IOGGk@SSG{C_A]I_@C_@EICQEi@Qe@M_@IKCGAG?S@Q@SBQBE?s@J}ARI@"));
        new AddRouteTask().execute(testRoute1).get();
        List<Route> list = null;
        try {
            list = ServiceProvider.getService().skatenightServerEndpoint().getRoutes().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(routename)) {
                testRoute1 = list.get(i);
                break;
            }
        }
        final Route testRoute2 = testRoute1;
        // Erstellen des Events
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((AnnounceInformationFragment) mActivity.getAdapter().getItem(1)).getAdapter().setRouteAndText(testRoute2);
                applyButton.performClick();
            }
        });
        Thread.sleep(3000);
        final Button okButton = ((AnnounceInformationFragment) mActivity.getAdapter().getItem(1)).getLastDialog().getButton(AlertDialog.BUTTON_POSITIVE);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                okButton.performClick();
            }
        });
        Thread.sleep(3000);


        // Suche das neu angelegte Event
        List<Event> list2 = null;
        try {
            list2 = ServiceProvider.getService().skatenightServerEndpoint().getAllEvents().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean found = false;
        if (list2 != null) {
            for (Event e : list2) {
                if (EventUtils.getInstance(mActivity).getUniqueField(FieldType.TITLE.getId(), e).getValue().equals(textTitle)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        } else {
            assertTrue("Couldn't find any events!", found);
        }

        // Nun wird das Event editiert
        swipe(Direction.Right);
        ShowEventsFragment mFragment = (ShowEventsFragment)mActivity.getAdapter().getItem(0);
        EventsCursorAdapter mAdapter = mFragment.getmAdapter();
        List<Event> eventList = mAdapter.getEventList();

        // Suche nach dem eben erstellten Event
        int pos = -1;
        found = false;
        for(Event e : eventList){
            pos++;
            if(textTitle.equals(EventUtils.getInstance(mActivity).getUniqueField(FieldType.TITLE.getId(), e).getValue())){
                found = true;
                break;
            }

        }
        assertTrue("Event wurde nicht gefunden", found);

        // Editiere das gefundene Event
        mFragment.editEvent(eventList.get(pos));

        // Auf die EditEventActivity warten und initialisieren der Instanz
        Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(EditEventActivity.class.getName(), null, false);
        EditEventActivity editEventActivity = (EditEventActivity) am.waitForActivityWithTimeout(20000);

        Thread.sleep(5000);

        final ListView eventFields = editEventActivity.getListView();

        final EditText editTextTitleEdit = (EditText) eventFields.getChildAt(0).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextFeeEdit = (EditText) eventFields.getChildAt(1).findViewById(R.id.list_view_item_announce_information_fee_editText);
        final EditText editTextLocationEdit = (EditText) eventFields.getChildAt(4).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
        final EditText editTextDescriptionEdit = (EditText) eventFields.getChildAt(6).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);

        final String textTitleEdit = "TestEventEdit";
        final String textFeeEdit = "5";
        final String textLocationEdit = "TestStadtEdit";
        final String textDescriptionEdit = "TestBeschreibungEdit";
        final String textDateEdit = "29.11.2014";
        final String textTimeEdit = "15:00 Uhr";


        // Fülle die Textfelder erneut aus
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitleEdit.setText(textTitleEdit);
                editTextFeeEdit.setText(textFeeEdit);
                editTextLocationEdit.setText(textLocationEdit);
                editTextDescriptionEdit.setText(textDescriptionEdit);
            }
        });
        Thread.sleep(3000);

        // Prüfen, ob die Daten verändert wurden
        assertEquals(textTitleEdit, editTextTitleEdit.getText().toString());
        assertEquals(textFeeEdit, editTextFeeEdit.getText().toString());
        assertEquals(textLocationEdit, editTextLocationEdit.getText().toString());
        assertEquals(textDescriptionEdit, editTextDescriptionEdit.getText().toString());

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
        Thread.sleep(3000);

        mAdapter = mFragment.getmAdapter();
        eventList = mAdapter.getEventList();

        // Suche nach dem eben editierten Event
        pos = -1;
        found = false;
        for(Event e : eventList){
            pos++;
            if(textTitleEdit.equals(EventUtils.getInstance(mActivity).getUniqueField(FieldType.TITLE.getId(), e).getValue())){
                found = true;
                break;
            }

        }
        assertTrue("Event wurde nicht gefunden", found);

        // Prüfen, ob die Daten verändert wurden
        assertEquals(textTitleEdit, EventUtils.getInstance(mActivity).getUniqueField(FieldType.TITLE.getId(), eventList.get(pos)).getValue());
        assertEquals(textFeeEdit, EventUtils.getInstance(mActivity).getUniqueField(FieldType.FEE.getId(), eventList.get(pos)).getValue());
        assertEquals(textLocationEdit, EventUtils.getInstance(mActivity).getUniqueField(FieldType.LOCATION.getId(), eventList.get(pos)).getValue());
        assertEquals(textDescriptionEdit, EventUtils.getInstance(mActivity).getUniqueField(FieldType.DESCRIPTION.getId(), eventList.get(pos)).getValue());

        swipe(Direction.Left);

        // Das Event und die Route löschen
        mFragment.deleteEvent(eventList.get(pos));
        ManageRoutesFragment fragment = (ManageRoutesFragment)mActivity.getAdapter().getItem(2);
        fragment.deleteRoute(testRoute2);
    }

    /**
     * Testet die Activity beim Beenden und wieder Neustarten.
     */

    @SmallTest
    public void testUseCaseStateDestroy() throws Exception{

        // Testdaten
        final String textTitle = "TestEventDestroy";
        final String textFee = "500";
        final String textLocation = "Whatever";
        final String textDescription = "Beschreibung";
        final String textDate = "31.12.2014";
        final String textTime = "12:00 Uhr";
        final String textRoute = "test";

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
                editTextTitle.setText(textTitle);
                editTextFee.setText(textFee);
                editTextLocation.setText(textLocation);
                editTextDescription.setText(textDescription);
                datePickerButton.setText(textDate);
                timePickerButton.setText(textTime);
                routePickerButton.setText(textRoute);
            }
        });
        Thread.sleep(3000);
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

        // Testdaten
        final String textTitle = "TestEvent";
        final String textFee = "10";
        final String textLocation = "TestStadt";
        final String textDescription = "TestBeschreibung";
        final String textDate = "29.12.2014";
        final String textTime = "14:00 Uhr";
        final String textRoute = "test";

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


















