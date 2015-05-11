package ws1415.ps1415.authTest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserLocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.common.controller.UserController;
import ws1415.common.model.Gender;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.Constants;
import ws1415.ps1415.activity.ImageStorageTestActivity;

/**
 * Diese Klasse wird dazu genutzt die Funktionalitäten des UserControllers zu testen, die
 * Authorisierung benötigen.
 *
 * @author Martin Wrodarczyk
 */
public class UserControllerAuthTest extends ActivityInstrumentationTestCase2<ImageStorageTestActivity> {
    public static boolean initialized = false;

    public static String MY_MAIL = "";
    public static final String TEST_MAIL = "test@gmail.com";

    public static final double TEST_LONGITUDE = 7.626135;
    public static final double TEST_LATITUDE = 51.960665;
    public static final Long TEST_CURRENT_EVENT_ID = 5L;

    public static final String TEST_FIRST_NAME = "Martin";
    public static final String TEST_LAST_NAME = "Müller";
    public static final String TEST_CITY = "Münster";
    public static final String TEST_DATE_OF_BIRTH = "1990-01-01";
    public static final String TEST_DESCRIPTION = "Neue Beschreibung";
    public static final Gender TEST_GENDER = Gender.MALE;
    public static final String TEST_POSTAL_CODE = "48159";

    public UserControllerAuthTest() {
        super(ImageStorageTestActivity.class);
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

        // Lösche alle Benutzer beim Start der Tests
        if(!initialized){
            // Alle Nutzer löschen
            deleteAllUsers();
            initialized = true;
        }

        // Eigenen EndUser und einen weiteren EndUser zum Testen erstellen
        final CountDownLatch createSignal = new CountDownLatch(1);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void voids) {
                createSignal.countDown();
            }
        }, MY_MAIL);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void voids) {
                createSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(createSignal.await(30, TimeUnit.SECONDS));
    }

    @Override
    public void tearDown() throws Exception {
        // Alle Nutzer löschen
        deleteAllUsers();
        super.tearDown();
    }

    private void deleteAllUsers() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        final List<UserInfo> userInfosFinal = new ArrayList<>();
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<UserInfo>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserInfo> userInfos) {
                if (userInfos != null) userInfosFinal.addAll(userInfos);
                signal.countDown();
            }
        }, "all");
        assertTrue(signal.await(30, TimeUnit.SECONDS));

        final CountDownLatch deleteAllSignal = new CountDownLatch(userInfosFinal.size());
        for (UserInfo userInfo : userInfosFinal) {
            UserController.deleteUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Void avoid) {
                    deleteAllSignal.countDown();
                }
            }, userInfo.getEmail());
        }
        assertTrue(deleteAllSignal.await(60, TimeUnit.SECONDS));
    }

    @SmallTest
    public void testUpdateUserLocation() throws InterruptedException {
        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.updateUserLocation(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid) {
                updateSignal.countDown();
            }
        }, MY_MAIL, TEST_LATITUDE, TEST_LONGITUDE, TEST_CURRENT_EVENT_ID);
        assertTrue(updateSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserLocation(new ExtendedTaskDelegateAdapter<Void, UserLocation>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserLocation userLocation) {
                assertNotNull(userLocation);
                assertEquals(MY_MAIL, userLocation.getEmail());
                assertEquals(new Date().getTime(), userLocation.getUpdatedAt().getValue(), 10000);
                assertEquals(TEST_LONGITUDE, userLocation.getLongitude(), 0.001);
                assertEquals(TEST_LATITUDE, userLocation.getLatitude(), 0.001);
                assertEquals(TEST_CURRENT_EVENT_ID, userLocation.getCurrentEventId());
                getSignal.countDown();
            }
        }, MY_MAIL);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die allgemeinen Informationen eines Benutzers richtig geändert werden.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testUpdateUserInfo() throws InterruptedException {
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setEmail(MY_MAIL);
        newUserInfo.setFirstName(TEST_FIRST_NAME);
        newUserInfo.setLastName(TEST_LAST_NAME);
        newUserInfo.setCity(TEST_CITY);
        newUserInfo.setDateOfBirth(TEST_DATE_OF_BIRTH);
        newUserInfo.setDescription(TEST_DESCRIPTION);
        newUserInfo.setGender(TEST_GENDER.getRepresentation());
        newUserInfo.setPostalCode(TEST_POSTAL_CODE);

        final CountDownLatch updateSignal = new CountDownLatch(1);
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
                updateSignal.countDown();
            }
        }, newUserInfo);
        assertTrue(updateSignal.await(30, TimeUnit.SECONDS));
    }

    // TODO: Profilbild ändern
    @SmallTest
    public void testUpdateUserPicture() {

    }

    @SmallTest
    public void testAddAndRemoveFriend() throws InterruptedException {
        final CountDownLatch addFriendSignal = new CountDownLatch(1);
        UserController.addFriend(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                assertTrue(result);
                addFriendSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(addFriendSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch getUserSignal = new CountDownLatch(1);
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser endUser) {
                assertNotNull(endUser.getMyFriends());
                assertTrue(endUser.getMyFriends().size() == 1);
                assertTrue(endUser.getMyFriends().get(0).equals(TEST_MAIL));
                getUserSignal.countDown();
            }
        }, MY_MAIL);
        assertTrue(getUserSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch removeFriendSignal = new CountDownLatch(1);
        UserController.removeFriend(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                assertTrue(result);
                removeFriendSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(removeFriendSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch getUserRemovedSignal = new CountDownLatch(1);
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser endUser) {
                assertNull(endUser.getMyFriends());
                getUserRemovedSignal.countDown();
            }
        }, MY_MAIL);
        assertTrue(getUserRemovedSignal.await(30, TimeUnit.SECONDS));
    }

    // TODO: Wenn Eventcontroller fertig
    @SmallTest
    public void testListEvents(){

    }

    // TODO: Wenn Groupcontroller fertig
    @SmallTest
    public void testListUserGroups() {

    }
}
