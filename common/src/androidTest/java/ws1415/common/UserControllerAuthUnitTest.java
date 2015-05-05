package ws1415.common;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.UserLocation;

import java.util.Date;

import ws1415.common.controller.UserController;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

/**
 * Created by Martin on 05.05.2015.
 */
public class UserControllerAuthUnitTest extends AndroidTestCase {
    // Um Tests zu Methoden zu schreiben, die Authentifizierung benötigen
    public static final String MY_MAIL = "martin.wrod@googlemail.com";
    public static final String WEB_CLIENT_ID = "1032268444653-7agre4q3eosqhlh92sq62hf5fan9jbv5.apps.googleusercontent.com";

    public static final double TEST_LONGITUDE = 7.626135;
    public static final double TEST_LATITUDE = 51.960665;
    public static final long TEST_CURRENT_EVENT_ID = 5L;

    public void setUp() throws Exception {
        System.out.println("setting up");
        SharedPreferences prefs;
        GoogleAccountCredential credential;

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        credential = GoogleAccountCredential.usingAudience(getContext(), "server:client_id:" + WEB_CLIENT_ID);

        // accountName aus SharedPreferences laden
        if (prefs.contains("accountName")) {
            credential.setSelectedAccountName(prefs.getString("accountName", null));
        }

        // Kein accountName gesetzt, also AccountPicker aufrufen
        if (!(credential.getSelectedAccountName() == null)) {
            ServiceProvider.login(credential);
        }
        super.setUp();
    }

    public void testUpdateUserLocation() throws InterruptedException {
        UserController.createUser(null, MY_MAIL);

        Thread.sleep(4000);

        UserController.updateUserLocation(null, MY_MAIL, TEST_LATITUDE, TEST_LONGITUDE,
                TEST_CURRENT_EVENT_ID);

        Thread.sleep(3000);

        UserController.getUserLocation(new ExtendedTaskDelegateAdapter<Void, UserLocation>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserLocation userLocation) {
                assertNotNull(userLocation);
                assertEquals(MY_MAIL, userLocation.getEmail());
                assertEquals(new Date().getTime(), userLocation.getUpdatedAt().getValue(), 10000);
                assertEquals(TEST_LONGITUDE, userLocation.getLongitude(), 0.001);
                assertEquals(TEST_LATITUDE, userLocation.getLatitude(), 0.001);
                assertEquals((Long) TEST_CURRENT_EVENT_ID, userLocation.getCurrentEventId());
            }
        }, MY_MAIL);

    }
}
