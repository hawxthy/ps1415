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
        //ServiceProvider.login(credential);

    }

    /**
     * Prüfen, ob auch keine Account Daten vorhanden sind, damit der Account Picker angezeigt wird.
     */
    @SmallTest
    public void testPreConditions() {
        assertEquals(true, credential != null);
        // Prüft die Existenz eines Benutzeraccounts
        assertEquals(true, credential.getSelectedAccountName() != null);
    }

    /**
     * Prüft, ob diese Views wirklich in der Activity existieren.
     */
    @SmallTest
    public void testViews() {
        assertNotNull(mActivity);
        assertNotNull(mButton);
    }

    /**
     * Testet, ob der ausgewählte Benutzeraccount ein Veranstalter ist.
     *
     * @throws IOException
     */
  //  public void testLoginUI() throws IOException {
  //      assertEquals(new Boolean(true), ServiceProvider.getService().skatenightServerEndpoint().isHost(credential.getSelectedAccountName()).execute().getValue());
  //  }


}









