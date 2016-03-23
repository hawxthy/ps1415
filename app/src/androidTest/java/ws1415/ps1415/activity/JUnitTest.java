package ws1415.ps1415.activity;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

import org.junit.Test;

import ws1415.ps1415.R;

/**
 * Beispiel instrumentalisierter JUnit Test für die Bachelorarbeit "Evaluation von Test Frameworks
 * für Android-Apps und ihre Integration zu einer Test-Infrastruktur".
 * Dies ist ein Beispiel für einen UI Test, welcher mit Hilfe des JUnit Test
 * Frameworks realisiert wird. Die Klasse hat mit Absicht den Namen "JUnitTest" und nicht
 * den eines Testsfalls, da diese nur exemplarisch für die Bachelorarbeit dient.
 *
 * @author Tristan Rust
 */
public class JUnitTest extends ActivityInstrumentationTestCase2<EditProfileActivity> {
    // Die EditProfileActivity
    private EditProfileActivity mEditProfileActivity;
    // Die UI Komponenten
    private EditText mEditTextFirstName;

    public JUnitTest() {super(EditProfileActivity.class);}

    protected void setUp() throws Exception {
        super.setUp();

        mEditProfileActivity = getActivity();
        // Initalisierung des Edit Feldes
        mEditTextFirstName = (EditText) mEditProfileActivity.findViewById(R.id.activity_edit_profile_first_name);
    }

    @Test
    public void testEditFirstNameText() {
        // Text für das Feld
        CharSequence firstNameText = "VornameTest";

        // Text in das Vornamensfeld schreiben
        mEditTextFirstName.setText(firstNameText);

        // Verifizieren, ob der Text korrekt ausgelesen wird
        assertEquals("VornameTest", mEditTextFirstName.getText().toString());

        assertTrue(true);
    }


}
