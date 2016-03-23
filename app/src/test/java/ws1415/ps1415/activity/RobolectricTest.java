package ws1415.ps1415.activity;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import ws1415.ps1415.BuildConfig;
import ws1415.ps1415.R;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Beispiel Robolectric Test für die Bachelorarbeit "Evaluation von Test Frameworks
 * für Android-Apps und ihre Integration zu einer Test-Infrastruktur".
 * Dies ist ein Beispiel für einen UI Test, welcher mit Hilfe des Robolectric Test
 * Frameworks realisiert wird. Die Klasse hat mit Absicht den Namen "RobolectricTest" und nicht
 * den eines Testsfalls, da diese nur exemplarisch für die Bachelorarbeit dient.
 *
 * @author Tristan Rust
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class RobolectricTest {

    private RegisterActivity mActivity;

    @Before
    public void setUp() {
        mActivity = Robolectric.buildActivity(RegisterActivity.class).create().get();
    }

    @Test
    public void checkActivityNotNull() throws Exception {
        assertNotNull(mActivity);
    }

    /**
     * Diese Methode testet, ob sich der Text im Vornamensfeld ändern lässt.
     */
    @Test
    public void testWhatever() throws Exception {
        // Shadowobjekt erzeugen
        ShadowActivity shadowActivity = Shadows.shadowOf(mActivity);

        // Text für das Feld
        CharSequence firstNameText = "VornameTest";

        EditText firstNameEditText = (EditText) mActivity.findViewById(R.id.first_name);

        firstNameEditText.setText(firstNameText);

        // Verifizieren, ob der Text korrekt ausgelesen wird
        assertEquals("VornameTest", firstNameEditText.getText().toString());

        assertTrue(true);
    }

}




















