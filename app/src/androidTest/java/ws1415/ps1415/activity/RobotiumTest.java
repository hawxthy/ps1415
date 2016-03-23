package ws1415.ps1415.activity;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;
import com.robotium.solo.Solo;
import ws1415.ps1415.R;


/**
 * Beispiel Robotium Test für die Bachelorarbeit "Evaluation von Test Frameworks
 * für Android-Apps und ihre Integration zu einer Test-Infrastruktur".
 * Dies ist ein Beispiel für einen UI Test, welcher mit Hilfe des Robotium Test
 * Frameworks realisiert wird. Die Klasse hat mit Absicht den Namen "RobotiumTest" und nicht
 * den eines Testsfalls, da diese nur exemplarisch für die Bachelorarbeit dient.
 *
 * @author Tristan Rust
 */
public class RobotiumTest extends ActivityInstrumentationTestCase2<ListEventsActivity> {

    private Solo solo;

    public RobotiumTest() {
        super(ListEventsActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Zeit zum einloggen
        Thread.sleep(2000);

        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

    /**
     * Diese Methode testet, ob sich der Text im Vornamensfeld ändern und anschließend speichern
     * lässt.
     */
    public void testEditText() throws Exception {
        // Wechseln von der EventsActivity auf die ProfilEditActivity
        solo.clickOnText("Veranstaltungen");
        solo.clickOnText("Mein Profil");
        View editButton = solo.getView("ws1415.ps1415:id/action_edit_profile");
        solo.clickOnView(editButton);

        // Abrufzeit der Daten vom Server
        Thread.sleep(2000);

        // Textfeld holen
        EditText firstNameEditText = (EditText) solo.getView(R.id.activity_edit_profile_first_name);

        // Textfeldinhalt löschen
        solo.clearEditText(firstNameEditText);
        // Neuen Namen eintragen
        solo.enterText(firstNameEditText, "VornameTest");
        // Prüfen, ob das Textfeld sichtbar ist
        assertEquals(View.VISIBLE, firstNameEditText.getVisibility());
        // Neuen Wert des Textfelds überprüfen
        assertEquals("VornameTest", firstNameEditText.getText().toString());

        View saveButton = solo.getView("ws1415.ps1415:id/action_save");
        solo.clickOnView(saveButton);
        // Prüfen, ob der richtige Text angezeigt wird
        assertTrue(solo.waitForText("Profilinformationen wurden geändert"));

        Thread.sleep(2000);
        // Textfeld holen
        assertTrue(true);
    }


}
