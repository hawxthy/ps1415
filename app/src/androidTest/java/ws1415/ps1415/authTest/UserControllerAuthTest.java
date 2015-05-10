package ws1415.ps1415.authTest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserLocation;

import java.util.Date;

import ws1415.common.controller.UserController;
import ws1415.common.model.Gender;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.Constants;
import ws1415.ps1415.activity.ShowInformationActivity;

/**
 * Diese Klasse wird dazu genutzt die Funktionalitäten des UserControllers zu testen, die
 * Authorisierung benötigen.
 *
 * @author Martin Wrodarczyk
 */
public class UserControllerAuthTest extends ActivityInstrumentationTestCase2<ShowInformationActivity> {
    public static String MY_MAIL = "";

    public static final double TEST_LONGITUDE = 7.626135;
    public static final double TEST_LATITUDE = 51.960665;
    public static final long TEST_CURRENT_EVENT_ID = 5L;

    public static final String TEST_FIRST_NAME = "Martin";
    public static final String TEST_LAST_NAME = "Müller";
    public static final String TEST_CITY = "Münster";
    public static final String TEST_DATE_OF_BIRTH = "1990-01-01";
    public static final String TEST_DESCRIPTION = "Neue Beschreibung";
    public static final Gender TEST_GENDER = Gender.MALE;
    public static final String TEST_POSTAL_CODE= "48159";

    public UserControllerAuthTest() {
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

    @SmallTest
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

        Thread.sleep(3000);

        UserController.deleteUser(null, MY_MAIL);
    }

    /**
     * Prüft, ob die allgemeinen Informationen eines Benutzers richtig geändert werden.
     * @throws InterruptedException
     */
    @SmallTest
    public void testUpdateUserInfo() throws InterruptedException {
        Thread.sleep(4000);

        UserController.createUser(null, MY_MAIL);

        Thread.sleep(4000);

        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setEmail(MY_MAIL);
        newUserInfo.setFirstName(TEST_FIRST_NAME);
        newUserInfo.setLastName(TEST_LAST_NAME);
        newUserInfo.setCity(TEST_CITY);
        newUserInfo.setDateOfBirth(TEST_DATE_OF_BIRTH);
        newUserInfo.setDescription(TEST_DESCRIPTION);
        newUserInfo.setGender(TEST_GENDER.getRepresentation());
        newUserInfo.setPostalCode(TEST_POSTAL_CODE);

        UserController.updateUserInfo(new ExtendedTaskDelegateAdapter<Void, UserInfo>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserInfo userInfo) {
                assertNotNull(userInfo);
                assertEquals(MY_MAIL, userInfo.getEmail());
                assertEquals(TEST_FIRST_NAME, userInfo.getFirstName());
                assertEquals(TEST_LAST_NAME, userInfo.getLastName());
                assertEquals(TEST_CITY, userInfo.getCity());
                assertEquals(TEST_DATE_OF_BIRTH, userInfo.getDateOfBirth());
                assertEquals(TEST_DESCRIPTION, userInfo.getDescription());
                assertEquals(TEST_GENDER.getRepresentation(), userInfo.getGender());
                assertEquals(TEST_POSTAL_CODE, userInfo.getPostalCode());
            }
        }, newUserInfo);

        Thread.sleep(3000);

        UserController.deleteUser(null, MY_MAIL);
    }

    // TODO: Profilbild ändern
    @SmallTest
    public void testUpdateUserPicture(){

    }
}
