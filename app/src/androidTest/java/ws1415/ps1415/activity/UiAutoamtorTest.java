package ws1415.ps1415.activity;

import org.junit.Before;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Beispiel UI Automator Test für die Bachelorarbeit "Evaluation von Test Frameworks
 * für Android-Apps und ihre Integration zu einer Test-Infrastruktur".
 * Dies ist ein Beispiel für einen UI Test, welcher mit Hilfe des UI Automator Test
 * Frameworks realisiert wird. Die Klasse hat mit Absicht den Namen "UiAutomatorTest" und nicht
 * den eines Testsfalls, da diese nur exemplarisch für die Bachelorarbeit dient.
 *
 * @author Tristan Rust
 */
public class UiAutoamtorTest extends InstrumentationTestCase {

    private UiDevice mDevice;
    private static final int LAUNCH_TIMEOUT = 5000;
    private static final int LAUNCH_TIMEOUT_LONG = 20000;


    /**
     * Es wird auf dem Home Screen begonnen, auf die Anwendungen gewechselt und von dort aus die
     * App gestartet.
     * @throws Exception
     */
    @Override
    public void setUp() throws Exception{
        // Initialisiere die UiDevice Instanz
        mDevice = UiDevice.getInstance(getInstrumentation());

        // Starte vom Home Screen
        mDevice.pressHome();

        // Solange warten, bis das App Icon angezeigt wird
        mDevice.wait(Until.hasObject(By.desc("Apps")), LAUNCH_TIMEOUT);

        // Auf den Anwedungen Button klicken
        UiObject2 appsButton = mDevice.findObject(By.desc("Anwendungen"));
        appsButton.click();

        // Erneut warten, bis die neue Oberfläche geladen ist
        mDevice.wait(Until.hasObject(By.text("Veranstaltungen")), LAUNCH_TIMEOUT);

        // Die Skatenight App starten mit einem Klick
        UiObject2 skatenightApp = mDevice.findObject(By.text("Veranstaltungen"));
        skatenightApp.click();

    }

    /**
     * In diesem Test wird überprüft, ob der Nutzer sich einloggen lässt.
     * Anschließend wird ein vorgegebener Nutzer eingeloggt und zum Schluss wieder ausgeloggt.
     * <b>Dieser Test schlägt fehl, wenn für die anderen Frameworks (Robotium, Espresso, Robolectric)
     * der Loginprozess übersprungen wird bzw. der Login nicht mehr passieren muss. Sie UniversalUtil.</b>
     * @throws Exception
     */
    public void testLogin() throws Exception {
        // Warten bis die Loginansicht eingeblendet wird
        mDevice.wait(Until.hasObject(By.text("tristan.rust@googlemail.com")), LAUNCH_TIMEOUT);

        // Ersten Account auswählen (hier vorgegeben auf dem Testgerät
        UiObject2 account = mDevice.findObject(By.text("tristan.rust@googlemail.com"));
        account.click();

        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Bestätigen um einzuloggen
        UiObject2 okButton = mDevice.findObject(By.text("OK"));
        okButton.click();

        // Warten bis die App nach dem Login idle wird, Daten müssen vom Server abgerufen werden
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Warte darauf, dass die Titelleiste geladen wird
        mDevice.wait(Until.hasObject(By.text("Veranstaltungen")), LAUNCH_TIMEOUT);

        // Klicke auf das Hauptmenü
        UiObject2 eventTitleBar = mDevice.findObject(By.text("Veranstaltungen"));
        // eventTitleBar.click();

        // Sollte das Hauptmenü geladen worden sein und das Hauptmenü ist anklickbar, gilt der
        // Nutzer als eingeloggt
        assertNotNull(eventTitleBar);


        logout();
    }

    /**
     *  Loggt den Nutzer wieder aus.
     * @throws Exception
     */
    private void logout() throws Exception {
        mDevice.wait(Until.hasObject(By.res("android:id/action_bar_title")), LAUNCH_TIMEOUT);
        UiObject2 eventTitleBar = mDevice.findObject(By.res("android:id/action_bar_title"));
        eventTitleBar.click();

        // Im Menü runter scrollen da ansonsten nicht auf  Erstellen geklickt werden kann
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/list_slidermenu")), LAUNCH_TIMEOUT);
        UiObject2 listView = mDevice.findObject(By.res("ws1415.ps1415:id/list_slidermenu"));
        listView.scroll(Direction.DOWN, 1);

        // Logout anklicken
        mDevice.wait(Until.hasObject(By.text("Logout")), LAUNCH_TIMEOUT);
        UiObject2 logoutButton = mDevice.findObject(By.text("Logout"));
        logoutButton.click();

        // Zurücktaste betätigen, da ansonsten der Logindialog Systemdialog wieder angezeigt würde.
        mDevice.pressBack();

        // Auf Datenabgleich mit dem Server warten
        mDevice.waitForIdle(LAUNCH_TIMEOUT);
    }

    /**
     * Zuerst wird der Nutzer eingeloggt, dann eine Gruppe erstellt, die sofort wieder gelöscht wird.
     * Anschließend wird ein vorgegebener Nutzer eingeloggt und zum Schluss wieder ausgeloggt.
     * <b>Dieser Test schlägt fehl, wenn für die anderen Frameworks (Robotium, Espresso, Robolectric)
     * der Loginprozess übersprungen wird bzw. der Login nicht mehr passieren muss. Sie UniversalUtil.</b>
     * @throws Exception
     */
    public void testCreateAndDeleteGroup() throws Exception {
        // Warten bis die Loginansicht eingeblendet wird
        mDevice.wait(Until.hasObject(By.text("tristan.rust@googlemail.com")), LAUNCH_TIMEOUT);
        // Ersten Account auswählen (hier vorgegeben auf dem Testgerät
        UiObject2 account = mDevice.findObject(By.text("tristan.rust@googlemail.com"));
        account.click();

        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Bestätigen um einzuloggen
        UiObject2 okButton = mDevice.findObject(By.text("OK"));
        okButton.click();

        // Warten bis die App nach dem Login idle wird, Daten müssen vom Server abgerufen werden
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        mDevice.wait(Until.hasObject(By.text("Veranstaltungen")), LAUNCH_TIMEOUT);
        // Klicke auf das Hauptmenü
        UiObject2 eventTitleBar = mDevice.findObject(By.text("Veranstaltungen"));
        eventTitleBar.click();

        // Auf den Menüpunkt zum Erstellen einer Gruppe klicken
        mDevice.wait(Until.hasObject(By.text("Gruppe erstellen")), LAUNCH_TIMEOUT);
        UiObject2 createGroup = mDevice.findObject(By.text("Gruppe erstellen"));
        createGroup.click();

        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        mDevice.wait(Until.hasObject(By.text("Gruppenname")), LAUNCH_TIMEOUT);
        // Gruppennamen eingeben
        UiObject2 groupText = mDevice.findObject(By.text("Gruppenname"));
        groupText.setText("GruppenNameTest");

        mDevice.wait(Until.hasObject(By.text("PRÜFE")), LAUNCH_TIMEOUT);
        // Prufen, ob der Gruppenname noch nicht vergeben ist
        UiObject2 checkButton = mDevice.findObject(By.text("PRÜFE"));
        checkButton.click();

        // Auf Datenabgleich mit dem Server warten
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Prüfen, ob der Name nicht schon vergeben ist
        UiObject2 checkAvailable = mDevice.findObject(By.text("Der Name ist verfügbar"));
        assertEquals("Der Name ist verfügbar", checkAvailable.getText());

        // Im Menü runter scrollen da ansonsten nicht auf  Erstellen geklickt werden kann
        UiObject2 listView = mDevice.findObject(By.clazz("android.widget.ScrollView"));
        listView.scroll(Direction.DOWN, 100);

        mDevice.wait(Until.hasObject(By.text("ERSTELLEN")), LAUNCH_TIMEOUT);
        // Gruppe erstellen
        UiObject2 createButton = mDevice.findObject(By.text("ERSTELLEN"));
        createButton.click();

        // Darauf warten, dass die Gruppe auf dem Server angelegt wird
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Gruppe wieder löschen
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/group_profile_delete_group_button")), LAUNCH_TIMEOUT);
        UiObject2 deleteButton = mDevice.findObject(By.res("ws1415.ps1415:id/group_profile_delete_group_button"));
        deleteButton.click();
        assertNotNull(deleteButton);

        // Löschanfrage positiv beantworten
        mDevice.wait(Until.hasObject(By.text("Ja")), LAUNCH_TIMEOUT);
        UiObject2 yesButton = mDevice.findObject(By.text("Ja"));
        yesButton.click();

        // Nutzer ausloggen
        logout();
    }

    /**
     * Prüft, ob eine Veranstaltung angelegt und wieder gelöscht werden kann.
     * Damit eine Veranstaltung angelegt werden kann, muss der Veranstalter zuerst eine Route
     * erstellen. Anschließend wird die Veranstaltung angelegt und gelöscht. Danach wird die
     * Route gelöscht. Auf dem Testgeräte muss die App "Fotos" verfügbar sein, in der dass erste
     * Bild für das Veranstaltungs-Icon ausgewählt wird.
     *  <b>Dieser Test schlägt fehl, wenn für die anderen Frameworks (Robotium, Espresso, Robolectric)
     * der Loginprozess übersprungen wird bzw. der Login nicht mehr passieren muss. Sie UniversalUtil.</b>
     * @throws Exception
     */
    public void testCreateEvent() throws Exception {
        // Warten bis die Loginansicht eingeblendet wird
        mDevice.wait(Until.hasObject(By.text("tristan.rust@googlemail.com")), LAUNCH_TIMEOUT);
        // Ersten Account auswählen (hier vorgegeben auf dem Testgerät
        mDevice.findObject(By.text("tristan.rust@googlemail.com")).click();

        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Bestätigen um einzuloggen
        mDevice.findObject(By.text("OK")).click();

        // Warten bis die App nach dem Login idle wird, Daten müssen vom Server abgerufen werden
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        mDevice.wait(Until.hasObject(By.text("Veranstaltungen")), LAUNCH_TIMEOUT);
        // Klicke auf das Hauptmenü
        mDevice.findObject(By.text("Veranstaltungen")).click();

        // Auf den Menüpunkt zum Erstellen einer Gruppe klicken
        mDevice.wait(Until.hasObject(By.text("Routen erstellen/bearbeiten")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Routen erstellen/bearbeiten")).click();

        // Warten bis die Activity zum Routen erstellen geöffnet ist
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Auf den "+" Button zum erstellen einer Route klicken
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/action_create_route")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/action_create_route")).click();

        // Namen der Route eingeben
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/activity_add_route_routeName_edittext")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/activity_add_route_routeName_edittext")).setText("RoutenNameTest");

        // Bestätigen
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/activity_add_route_dialog_apply")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/activity_add_route_dialog_apply")).click();

        // Warten bis die Activity zum setzen der Wegpunkte erscheint
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Zwei Wegpunkte setzen und speichern
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/action_add_waypoint")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/action_add_waypoint")).click();
        mDevice.findObject(By.res("ws1415.ps1415:id/action_add_waypoint")).click();
        mDevice.pressBack();
        mDevice.wait(Until.hasObject(By.text("Ja")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Ja")).click();

        // Warten bis die Activity auf die vorherige Activity zurück gekehrt wurde
        mDevice.waitForIdle(LAUNCH_TIMEOUT_LONG);

        // Prüfen, ob die Route angelegt worden ist
        mDevice.wait(Until.hasObject(By.text("RoutenNameTest")), LAUNCH_TIMEOUT);
        assertEquals("RoutenNameTest", mDevice.findObject(By.text("RoutenNameTest")).getText());

        // Wechseln auf die Activity, um eine Veranstaltung anzulegen
        mDevice.wait(Until.hasObject(By.text("Routen erstellen/bearbeiten")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Routen erstellen/bearbeiten")).click();
        mDevice.wait(Until.hasObject(By.text("Veranstaltungen erstellen/bearbeiten")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Veranstaltungen erstellen/bearbeiten")).click();

        // Warten bis die Activity auf die vorherige für die Veranstaltungen gewechselt wurde
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Event anlegen
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/action_create_event")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/action_create_event")).click();
        // Titel angeben
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/title")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/title")).setText("TitelTest");
        mDevice.pressBack(); // Damit die Tastatur geschlossen wird.
        // Beschreibung angeben
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/description")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/description")).setText("BeschreibungTest");
        // Icon laden
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/eventData")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/eventData")).scroll(Direction.UP, 1);
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/icon")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/icon")).click();
        // Galerie auswählen
        mDevice.wait(Until.hasObject(By.text("Fotos")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Fotos")).click();
        // Warten bis die Fotos App geöffnet ist und Bild auswählen für das Icon
        mDevice.waitForIdle(LAUNCH_TIMEOUT);
        // Erstes Fotos auswählen
        mDevice.wait(Until.hasObject(By.res("com.google.android.apps.plus:id/tile_row")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("com.google.android.apps.plus:id/tile_row")).click();
        // Zurück auf die Skatenight App
        mDevice.waitForIdle(LAUNCH_TIMEOUT);
        // An das Ende runter scrollen
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/eventData")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/eventData")).scroll(Direction.DOWN, 1);
        // Treffpunkt eintragen
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/meeting_place")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/meeting_place")).click();
        mDevice.findObject(By.res("ws1415.ps1415:id/meeting_place")).setText("TreffpunktTest");
        mDevice.pressBack();
        // An das Ende runter scrollen
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/eventData")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/eventData")).scroll(Direction.DOWN, 1);
        // Route auswählen
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/route")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/route")).click();
        mDevice.wait(Until.hasObject(By.text("RoutenNameTest")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("RoutenNameTest")).click();
        // Speichern
        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/action_save_event")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("ws1415.ps1415:id/action_save_event")).click();
        mDevice.wait(Until.hasObject(By.text("Ja")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Ja")).click();

        // Warten bis das Event auf dem Server angelegt worden ist
        mDevice.waitForIdle(LAUNCH_TIMEOUT_LONG);
        Thread.sleep(2000); // Da die programmierte Refreshfunktion extrem langsam für die UI ist...
        // Prüfen, ob das Event angelegt worden ist
        mDevice.wait(Until.hasObject(By.text("TitelTest")), LAUNCH_TIMEOUT_LONG);
        assertEquals("TitelTest", mDevice.findObject(By.text("TitelTest")).getText());

        mDevice.waitForIdle(LAUNCH_TIMEOUT_LONG);

        // Wechseln auf die Activity, um eine Veranstaltung anzulegen
        mDevice.wait(Until.hasObject(By.res("android:id/action_bar_title")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("android:id/action_bar_title")).click();
        mDevice.wait(Until.hasObject(By.text("Veranstaltungen erstellen/bearbeiten")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Veranstaltungen erstellen/bearbeiten")).click();

        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Event wieder löschen
        mDevice.wait(Until.hasObject(By.text("TitelTest")), LAUNCH_TIMEOUT);
        // Da der Programmierer vergessen hat, dem Element eine long-clickable Eigenschaft
        // hinzuzufügen, musste dies über einen swipe gelöst werden
        UiObject2 testEvent = mDevice.findObject(By.text("TitelTest"));
        Rect rectEventButton = testEvent.getVisibleBounds();
        mDevice.swipe(rectEventButton.centerX(), rectEventButton.centerY(), rectEventButton.centerX(), rectEventButton.centerY(), 300);
        mDevice.waitForIdle(LAUNCH_TIMEOUT);
        mDevice.wait(Until.hasObject(By.text("Löschen")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Löschen")).click();
        mDevice.wait(Until.hasObject(By.text("Ja")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Ja")).click();
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Prüfen, ob Event gelöscht wurde
        assertNull(mDevice.findObject(By.text("TitelTest")));

        // Zurück auf die Routenübersicht wechseln
        mDevice.wait(Until.hasObject(By.res("android:id/action_bar_title")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.res("android:id/action_bar_title")).click();
        mDevice.wait(Until.hasObject(By.text("Routen erstellen/bearbeiten")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Routen erstellen/bearbeiten")).click();

        // Route löschen
        mDevice.wait(Until.hasObject(By.text("RoutenNameTest")), LAUNCH_TIMEOUT);
        // Da der Programmierer vergessen hat, dem Element eine long-clickable Eigenschaft
        // hinzuzufügen, musste dies über einen swipe gelöst werden
        UiObject2 testRoute = mDevice.findObject(By.text("RoutenNameTest"));
        Rect rectRouteButton = testRoute.getVisibleBounds();
        mDevice.swipe(rectRouteButton.centerX(), rectRouteButton.centerY(), rectRouteButton.centerX(), rectRouteButton.centerY(), 300);
        mDevice.waitForIdle(LAUNCH_TIMEOUT);
        mDevice.wait(Until.hasObject(By.text("Löschen")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Löschen")).click();
        mDevice.wait(Until.hasObject(By.text("Ja")), LAUNCH_TIMEOUT);
        mDevice.findObject(By.text("Ja")).click();
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        // Prüfen, ob Event gelöscht wurde
        assertNull(mDevice.findObject(By.text("RoutenNameTest")));

        // Nutzer ausloggen
        logout();
    }
}

