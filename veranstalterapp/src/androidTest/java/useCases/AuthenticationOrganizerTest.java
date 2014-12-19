package useCases;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import ws1415.veranstalterapp.Constants;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.activity.LoginActivity;

/**
 * Testet den Use Case "Authentifikation des Veranstalters".
 *
 * @author Tristan Rust
 */
public class AuthenticationOrganizerTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private LoginActivity mActivity;
    private Button mButton;

    private GoogleAccountCredential credential;

    public AuthenticationOrganizerTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        // Holt sich die LoginActiviy und startet diese
        mActivity = getActivity();
        mButton = (Button) mActivity.findViewById(R.id.login_button);

        // Ruft die aktuellen Account Daten ab, die null sein sollten
        // Nutzer einloggen
        credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:"+ Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

    }

    /**
     * Prüfen, ob auch keine Account Daten vorhanden sind, damit der Account Picker angezeigt wird.
     */
    public void testPreConditions() {
        // Nutzer einloggen
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);
        assertTrue(credential.getSelectedAccountName() == null);
        assertNotNull(mButton);
    }

    /**
     * Prüft, ob diese Views wirklich in der Activity existieren.
     */
    @SmallTest
    public void testViews() {
        assertNotNull(getActivity());
        assertNotNull(mButton);
    }

    /**
     * Testet, ob der Veranstalter über die UI eingeloggt werden kann.
     *
     * @throws InterruptedException
     */
    public void testLoginUI() throws InterruptedException {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(LoginActivity.class.getName(), null, false);

        // Es wird ein neuer UI Thread gestartet, um das Veranstalterkonto auswählen zu können
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButton.performClick();
            }
        });

        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);


    }


}









