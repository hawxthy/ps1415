package ws1415.veranstalterapp.task;

import android.test.ActivityInstrumentationTestCase2;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import ws1415.veranstalterapp.Activities.LoginActivity;
import ws1415.veranstalterapp.Constants;
import ws1415.veranstalterapp.ServiceProvider;

/**
 * TestCase für einen Task, der Authentifizierung benötigt. Es wird dazu automatisch das erste
 * Konto auf dem Testgerät ausgewählt.
 * Ein auf dieser Klasse basierender Test muss in seiner setUp()-Methode die setUp()-Methode dieser
 * Klasse über <code>super.setUp()</code> aufrufen, damit die Authentifizierung stattfindet.
 * @author Richard
 */
public class AuthTaskTestCase extends ActivityInstrumentationTestCase2<LoginActivity> {

    public AuthTaskTestCase() {
        super(LoginActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(
                getActivity(),
                "server:client_id:"+ Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);
    }

}
