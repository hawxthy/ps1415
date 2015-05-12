package ws1415.ps1415.authTest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import ws1415.common.net.ServiceProvider;
import ws1415.ps1415.Constants;
import ws1415.ps1415.activity.ShowInformationActivity;

/**
 * Diese Klasse wird dazu genutzt die Funktionalitäten des RoleControllers zu testen, die
 * Authorisierung benötigen.
 *
 * @author Martin Wrodarczyk
 */
public class RoleControllerAuthTest extends ActivityInstrumentationTestCase2<ShowInformationActivity> {
    public static String MY_MAIL = "";

    public RoleControllerAuthTest() {
        super(ShowInformationActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Nutzer einloggen
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        if (credential.getSelectedAccountName() == null) {
            credential.setSelectedAccountName(prefs.getString("accountName", null));
            MY_MAIL = credential.getSelectedAccountName();
            ServiceProvider.login(credential);
        } else {
            throw new Exception("Benutzer muss vorher ausgewählt werden");
        }
    }

    /**
     * Testet das ändern der globalen Rolle eines Benutzers.
     * TODO: Adminlösung finden
     * @throws InterruptedException
     */
    @SmallTest
    public void testChangeGlobalRole() throws InterruptedException {

    }
}