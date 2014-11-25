package useCases;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import ws1415.veranstalterapp.Constants;
import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.activity.LoginActivity;

/**
 * Testet den Use Case "Authentifikation des Veranstalters".
 *
 * @author Tristan Rust
 */
public class AuthenticationOrganizerTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private LoginActivity mActivity;

    public AuthenticationOrganizerTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Nutzer einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(
                getActivity(),
                "server:client_id:"+ Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        setActivityInitialTouchMode(false);

        // Holt sich die LoginActiviy und startet diese
        mActivity = getActivity();

    }

    // Loggt den Nutzer ein
    public void testPreConditions() throws InterruptedException {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(LoginActivity.class.getName(), null, false);
    }



}









