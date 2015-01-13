package useCases;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

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
     * Testet die LoginActivity.
     *
     * @throws IOException
     */
    public void testLogin() throws IOException {
        // Testet, ob die Activity und der Button initialisiert wurden.
        assertNotNull(mActivity);
        assertNotNull(mButton);

        // Testet, ob der ausgewählte Account nicht null ist.
        String tmp = credential.getSelectedAccountName();
        assertEquals(tmp != null, true);

        // Testet, ob der ausgewählte Account ein Veranstalter ist.
        assertEquals(new Boolean(true), ServiceProvider.getService().skatenightServerEndpoint().isHost(credential.getSelectedAccountName()).execute().getValue());
    }
}









