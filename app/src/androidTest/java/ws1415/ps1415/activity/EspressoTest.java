package ws1415.ps1415.activity;

import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ws1415.ps1415.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertTrue;

/**
 * Beispiel Espresso Test für die Bachelorarbeit "Evaluation von Test Frameworks
 * für Android-Apps und ihre Integration zu einer Test-Infrastruktur".
 * Dies ist ein Beispiel für einen UI Test, welcher mit Hilfe des Espresso Test
 * Frameworks realisiert wird. Die Klasse hat mit Absicht den Namen "Espresso" und nicht
 * den eines Testsfalls, da diese nur exemplarisch für die Bachelorarbeit dient.
 *
 * @author Tristan Rust
 */
@RunWith(AndroidJUnit4.class)
public class EspressoTest {

    /**
     * Startet die Activity automatisch
     */
    @Rule
    public ActivityTestRule<EditProfileActivity> mActivityRule =
            new ActivityTestRule<>(EditProfileActivity.class);

    /**
     * Diese Methode testet, ob sich der Text im Vornamensfeld ändern lässt.
     */
    @Test
    public void testEditFristNameTest() {
        // Text für das Feld
        String firstNameText = "VornameTest";

        // Text in das Vornamensfeld schreiben
        onView(withId(R.id.activity_edit_profile_first_name)).perform(typeText(firstNameText));

        // Prüfen, ob der Text im Feld steht
        onView(withId(R.id.activity_edit_profile_first_name)).check(ViewAssertions.matches(withText("VornameTest")));

        // Klicke den Button
        onView(withId(R.id.list_slidermenu)).perform(click());

        // Speichern Button klicken
        onView(withId(R.id.action_save)).perform(click());
    }


}
