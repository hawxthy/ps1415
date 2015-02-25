package useCases;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.graphics.Point;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Route;

import java.util.List;

import ws1415.veranstalterapp.dialog.AddRouteDialog;
import ws1415.veranstalterapp.Constants;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.activity.HoldTabsActivity;
import ws1415.veranstalterapp.activity.RouteEditorActivity;
import ws1415.veranstalterapp.fragment.ManageRoutesFragment;

/**
 * Prüft das Anlegen und Löschen einer Route.
 *
 * @author Richard
 */
public class CreateAndDeleteRouteTest extends ActivityInstrumentationTestCase2<HoldTabsActivity> {
    private HoldTabsActivity activity;
    private ManageRoutesFragment manageRoutesFragment;

    // Testdaten für die Route
    private static final String ROUTE_NAME = "Testroute: " + System.currentTimeMillis();

    // Wird zum Swipen nach links oder rechts verwendet
    public enum Direction {
        Left, Right;
    }

    public CreateAndDeleteRouteTest() {
        super(HoldTabsActivity.class);
    }


    /**
     * Loggt den Veranstalter ein und wechselt auf den "Route erstellen"-Tab.
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Nutzer verbinden
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        // Touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        // Die HoldTabsActivity wird gestartet
        activity = getActivity();
        // ManagesRoutesFragment abrufen
        manageRoutesFragment = (ManageRoutesFragment) activity.getAdapter().getItem(2);

        // Zwei mal nach links swipen, um den "Strecke erstellen"-Tab aufzurufen
        swipe(Direction.Left, 10);
        swipe(Direction.Left, 10);
    }

    /**
     * Swipt auf dem Bildschirm.
     *
     * @param direction Die Richtung (links oder recths)
     * @param offset Das offset vom Rand, bei dem das Swipen starten soll
     */
    private void swipe(Direction direction, int offset) {
        Instrumentation inst = getInstrumentation();
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;

        // Festellen in welche Richtung
        long downTime = SystemClock.uptimeMillis();
        float xStart = ((direction == Direction.Left) ? (width - offset) : offset);
        float xEnd = ((direction == Direction.Left) ? offset : (width - offset));

        // Der Wert für die y verändert sich nicht
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, xStart, size.y / 2, 0));
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE, xEnd, size.y / 2, 0));
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis() + 1000,
                MotionEvent.ACTION_UP, xEnd, size.y / 2, 0));
    }

    /**
     * Testet das Erstellen einer neuen Strecke.
     * @throws Exception
     */
    public void testUseCase() throws Exception {
        // Kurz warten, damit die ActionBar aktualisiert wurde
        Thread.sleep(1000);

        // Auf den Plus-Button im ManageRoutesFragment drücken
        Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(AddRouteDialog.class.getName(), null, false);
        final MenuItem addRoute = manageRoutesFragment.getMenuItemAddRoute();
        assertNotNull("AddRouteButton wurde nicht gefunden", addRoute);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.onOptionsItemSelected(addRoute);
            }
        });
        AddRouteDialog addRouteDialog = (AddRouteDialog) am.waitForActivityWithTimeout(20000);
        assertNotNull(addRouteDialog);

        // Einen Namen für die Route vergeben
        EditText routeNameEditText = (EditText) addRouteDialog.findViewById(R.id.activity_add_route_routeName_edittext);
        routeNameEditText.setText(ROUTE_NAME);
        assertEquals("Name wurde nicht in den EditText geschrieben", ROUTE_NAME, routeNameEditText.getText().toString());

        // Mit OK den RouteEditor aufrufen
        am = getInstrumentation().addMonitor(RouteEditorActivity.class.getName(), null, false);
        Button addRouteDialogOKButton = (Button) addRouteDialog.findViewById(R.id.activity_add_route_dialog_apply);
        addRouteDialogOKButton.performClick();
        final RouteEditorActivity routeEditorActivity = (RouteEditorActivity) am.waitForActivityWithTimeout(20000);
        assertNotNull("RouteEditorActivity wurde nicht gestartet", routeEditorActivity);

        // Warten, bis die Karte vollständig geladen wurde
        Thread.sleep(5000);

        // Button zum Erstellen von Wegpunkten abrufen
        final MenuItem addWaypoint = routeEditorActivity.getMenuItemAddWaypoint();
        assertNotNull("Button zum Hinzufügen von Wegpunkten wurde nicht gefunden", addWaypoint);

        // Ersten Wegpunkt erstellen
        routeEditorActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                routeEditorActivity.onOptionsItemSelected(addWaypoint);
            }
        });
        // Warten, bis der erste Wegpunkt erstellt wurde
        int timeout = 100;
        while (routeEditorActivity.getArrayAdapter().getCount() < 1 && timeout > 0) {
            Thread.sleep(100);
            timeout--;
        }
        assertFalse("Timeout beim Erstellen des ersten Wegpunkts erreicht", timeout == 0);
        // Position auf der Karte ändern
        swipe(Direction.Left, 300);
        // Zweiten Wegpunkt erstellen
        routeEditorActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                routeEditorActivity.onOptionsItemSelected(addWaypoint);
            }
        });
        // Warten, bis der zweite Wegpunkt erstellt wurde
        timeout = 100;
        while (routeEditorActivity.getArrayAdapter().getCount() < 2 && timeout > 0) {
            Thread.sleep(100);
            timeout--;
        }
        assertFalse("Timeout beim Erstellen des zweiten Wegpunkts erreicht", timeout == 0);
        // Warten, bis die Route erstellt wurde
        timeout = 200;
        while (routeEditorActivity.getRoute() == null) {
            Thread.sleep(100);
            timeout--;
        }

        // Route speichern
        routeEditorActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                routeEditorActivity.onBackPressed();
            }
        });
        Thread.sleep(3000);
        final AlertDialog saveDialog = routeEditorActivity.getLastDialog();
        assertNotNull("Speicherdialog konnte nicht abgerufen werden", saveDialog);
        final Button yesButton = saveDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        assertEquals(routeEditorActivity.getString(R.string.yes), yesButton.getText());
        routeEditorActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                yesButton.performClick();
            }
        });
        // Warten, dass die RouteEditorActivity geschlossen wurde
        timeout = 100;
        while (saveDialog.isShowing() && timeout > 0) {
            Thread.sleep(100);
            timeout--;
        }
        assertFalse("Timeout beim warten auf das Schließen des Speicheridalogs", timeout == 0);

        // Prüfen, ob die Route auf dem Server erstellt wurde und der Titel übereinstimmt
        timeout = 20;
        Route createdRoute = null;
        while (timeout > 0 && createdRoute == null) {
            List<Route> routes = manageRoutesFragment.getRouteList();
            if (routes != null) {
                for (Route r : routes) {
                    if (r != null && ROUTE_NAME.equals(r.getName())) {
                        createdRoute = r;
                        break;
                    }
                }
            }
            Thread.sleep(1000);
            timeout--;
        }
        assertNotNull("Neu angelegte Gruppe konnte nicht vom Server abgerufen werden", createdRoute);

        // Route löschen
        manageRoutesFragment.deleteRoute(createdRoute);
        Thread.sleep(5000);
    }

}
