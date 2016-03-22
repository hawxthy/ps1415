package ws1415.ps1415.activity;

import org.junit.Before;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject2;
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

    @Override
    public void setUp() throws Exception{
        // Initialisiere die UiDevice Instanz
        mDevice = UiDevice.getInstance(getInstrumentation());

        // Starte vom Home Screen
        mDevice.pressHome();

        // Solange warten, bis das App Icon angezeigt wird
        mDevice.wait(Until.hasObject(By.desc("Apps")), 3000);

        // Auf den Anwedungen Button klicken
        UiObject2 appsButton = mDevice.findObject(By.desc("Anwendungen"));
        appsButton.click();

        // Erneut warten, bis die neue Oberfläche geladen ist
        mDevice.wait(Until.hasObject(By.text("Veranstaltungen")), 3000);

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
        mDevice.wait(Until.hasObject(By.text("tristan.rust@googlemail.com")), 3000);

        // Ersten Account auswählen (hier vorgegeben auf dem Testgerät
        UiObject2 account = mDevice.findObject(By.text("tristan.rust@googlemail.com"));
        account.click();

        mDevice.waitForIdle(5000);

        // Bestätigen um einzuloggen
        UiObject2 okButton = mDevice.findObject(By.text("OK"));
        okButton.click();

        // Warten bis die App nach dem Login idle wird, Daten müssen vom Server abgerufen werden
        mDevice.waitForIdle(5000);

        // Warte darauf, dass die Titelleiste geladen wird
        mDevice.wait(Until.hasObject(By.text("Veranstaltungen")), 5000);

        // Klicke auf das Hauptmenü
        UiObject2 eventTitleBar = mDevice.findObject(By.text("Veranstaltungen"));
        eventTitleBar.click();

        // Sollte das Hauptmenü geladen worden sein und das Hauptmenü ist anklickbar, gilt der
        // Nutzer als eingeloggt
        assertNotNull(eventTitleBar);
    }

}











