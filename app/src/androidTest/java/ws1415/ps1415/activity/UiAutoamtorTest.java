package ws1415.ps1415.activity;

import org.junit.Before;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.Intent;
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
 * Frameworks realisiert wird.
 *
 * @author Tristan Rust
 */
public class UiAutoamtorTest extends InstrumentationTestCase {

    private UiDevice mDevice;
    private static final int LAUNCH_TIMEOUT = 5000;


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
     * Dazu wird der Test auf dem Home Screen begonnen, auf Anwendungen gewechselt und von dort aus
     * die App gestartet. Anschließend wird ein vorgegebener Nutzer eingeloggt.
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

    private void logout() throws Exception {
        mDevice.wait(Until.hasObject(By.text("Veranstaltungen")), LAUNCH_TIMEOUT);
        UiObject2 eventTitleBar = mDevice.findObject(By.text("Veranstaltungen"));
        eventTitleBar.click();

        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/list_slidermenu")), LAUNCH_TIMEOUT);
        UiObject2 listView = mDevice.findObject(By.res("ws1415.ps1415:id/list_slidermenu"));
        listView.scroll(Direction.DOWN, 100);

        mDevice.wait(Until.hasObject(By.text("Logout")), LAUNCH_TIMEOUT);
        UiObject2 logoutButton = mDevice.findObject(By.text("Logout"));
        logoutButton.click();

        mDevice.pressBack();

        // Auf Datenabgleich mit dem Server warten
        mDevice.waitForIdle(LAUNCH_TIMEOUT);
    }

    public void testWhatever() throws Exception {
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

        UiObject2 checkAvailable = mDevice.findObject(By.text("Der Name ist verfügbar"));
        assertEquals("Der Name ist verfügbar", checkAvailable.getText());

        UiObject2 listView = mDevice.findObject(By.clazz("android.widget.ScrollView"));
        listView.scroll(Direction.DOWN, 100);

        mDevice.wait(Until.hasObject(By.text("ERSTELLEN")), LAUNCH_TIMEOUT);

        // Gruppe erstellen
        UiObject2 createButton = mDevice.findObject(By.text("ERSTELLEN"));
        createButton.click();

        // Darauf warten, dass die Gruppe auf dem Server angelegt wird
        mDevice.waitForIdle(LAUNCH_TIMEOUT);

        mDevice.wait(Until.hasObject(By.res("ws1415.ps1415:id/group_profile_delete_group_button")), LAUNCH_TIMEOUT);

        UiObject2 deleteButton = mDevice.findObject(By.res("ws1415.ps1415:id/group_profile_delete_group_button"));
        deleteButton.click();
        assertNotNull(deleteButton);

        mDevice.wait(Until.hasObject(By.text("Ja")), LAUNCH_TIMEOUT);
        UiObject2 yesButton = mDevice.findObject(By.text("Ja"));
        yesButton.click();

        logout();

    }

}











