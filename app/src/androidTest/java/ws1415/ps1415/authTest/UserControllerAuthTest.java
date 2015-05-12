package ws1415.ps1415.authTest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserInfoPicture;
import com.skatenight.skatenightAPI.model.UserLocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.common.controller.UserController;
import ws1415.common.model.Gender;
import ws1415.common.model.Visibility;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.Constants;
import ws1415.ps1415.activity.ImageStorageTestActivity;

/**
 * Diese Klasse wird dazu genutzt die Funktionalitäten des UserControllers zu testen, die
 * Authorisierung benötigen. Eigener Benutzer wird dabei neu erstellt und anschließend gelöscht.
 *
 * @author Martin Wrodarczyk
 */
public class UserControllerAuthTest extends ActivityInstrumentationTestCase2<ImageStorageTestActivity> {
    public static final String TEST_MAIL = "test@gmail.com";
    public static final String TEST_MAIL_2 = "test2@gmail.com";

    public static final double TEST_LONGITUDE = 7.626135;
    public static final double TEST_LATITUDE = 51.960665;
    public static final Long TEST_CURRENT_EVENT_ID = 5L;

    public static final String TEST_FIRST_NAME = "Martin";
    public static final String TEST_LAST_NAME = "Müller";
    public static final Visibility TEST_LAST_NAME_VISIBILITY = Visibility.ONLY_FRIENDS;
    public static final String TEST_CITY = "Münster";
    public static final Visibility TEST_CITY_VISIBILITY = Visibility.ONLY_FRIENDS;
    public static final String TEST_DATE_OF_BIRTH = "1990-01-01";
    public static final Visibility TEST_DATE_OF_BIRTH_VISIBILITY = Visibility.ONLY_FRIENDS;
    public static final String TEST_DESCRIPTION = "Neue Beschreibung";
    public static final Gender TEST_GENDER = Gender.MALE;
    public static final String TEST_POSTAL_CODE = "48159";
    public static final Visibility TEST_POSTAL_CODE_VISIBILITY = Visibility.ONLY_FRIENDS;

    public static final Visibility TEST_GROUP_VISIBILITY = Visibility.ONLY_FRIENDS;
    public static final Boolean TEST_OPT_OUT_SEARCH = true;

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
            String selectedMail = prefs.getString("accountName", null);
            if (selectedMail == null || !ServiceProvider.getService().roleEndpoint().isAdmin(selectedMail).execute().getValue()) {
                throw new Exception("Benutzer muss Administrator sein");
            }
            credential.setSelectedAccountName(selectedMail);
            ServiceProvider.login(credential);
        } else {
            throw new Exception("Benutzer muss vorher ausgewählt werden");
        }

        // Zwei EndUser zum Testen erstellen
        final CountDownLatch createSignal = new CountDownLatch(2);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void voids) {
                createSignal.countDown();
            }
        }, TEST_MAIL);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void voids) {
                createSignal.countDown();
            }
        }, TEST_MAIL_2);
        assertTrue(createSignal.await(30, TimeUnit.SECONDS));
    }

    @Override
    public void tearDown() throws Exception {
        // Benutzer wieder löschen
        final CountDownLatch deleteSignal = new CountDownLatch(2);
        UserController.deleteUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid) {
                deleteSignal.countDown();
            }
        }, TEST_MAIL);
        UserController.deleteUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid) {
                deleteSignal.countDown();
            }
        }, TEST_MAIL_2);
        assertTrue(deleteSignal.await(30, TimeUnit.SECONDS));
        super.tearDown();
    }

    /**
     * Erstellt einen Benutzer und prüft ob der Benutzer auf dem Server gespeichert ist.
     *
     * @throws Exception
     */
    @SmallTest
    public void testGetUser() throws InterruptedException {
        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser user) {
                assertEquals(TEST_MAIL, user.getEmail());
                assertEquals(TEST_MAIL, user.getUserInfoPicture().getEmail());
                assertEquals(TEST_MAIL, user.getUserInfoPicture().getUserPicture().getEmail());
                assertEquals(TEST_MAIL, user.getUserInfoPicture().getUserInfo().getEmail());
                assertEquals(TEST_MAIL, user.getUserLocation().getEmail());
                getSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Standortinformationen eines Benutzers richtig abgerufen werden.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testGetUserLocation() throws InterruptedException {
        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserLocation(new ExtendedTaskDelegateAdapter<Void, UserLocation>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserLocation userLocation) {
                assertNotNull(userLocation);
                assertEquals(TEST_MAIL, userLocation.getEmail());
                getSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Standortinformationen eines Benutzers richtig abgerufen werden.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testGetUserInfo() throws InterruptedException {
        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserLocation(new ExtendedTaskDelegateAdapter<Void, UserLocation>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserLocation userLocation) {
                assertNotNull(userLocation);
                assertEquals(TEST_MAIL, userLocation.getEmail());
                getSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(getSignal.await(20, TimeUnit.SECONDS));
    }

    /**
     * Prüft ob die allgemeinen Informationen von Benutzern richtig abgerufen werden.
     *
     * @throws Exception
     */
    @SmallTest
    public void testListUserInfo() throws InterruptedException {
        final List<String> userMails = new ArrayList<>();
        userMails.add(TEST_MAIL);
        userMails.add(TEST_MAIL_2);

        final CountDownLatch listSignal = new CountDownLatch(1);
        UserController.listUserInfo(new ExtendedTaskDelegateAdapter<Void, List<UserInfo>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserInfo> userInfos) {
                List<String> userMailsServer = new ArrayList<>();
                for (UserInfo userInfo : userInfos) {
                    userMailsServer.add(userInfo.getEmail());
                }

                assertTrue(userMailsServer.containsAll(userMails) &&
                        userMails.containsAll(userMailsServer));
                listSignal.countDown();
            }
        }, userMails);
        assertTrue(listSignal.await(20, TimeUnit.SECONDS));
    }

    /**
     * Prüft ob die allgemeinen Informationen von Benutzern zusammen mit den Profilbildern richtig
     * abgerufen werden.
     *
     * @throws Exception
     */
    @SmallTest
    public void testListUserInfoWithPicture() throws InterruptedException {
        final List<String> userMails = new ArrayList<>();
        userMails.add(TEST_MAIL);
        userMails.add(TEST_MAIL_2);

        final CountDownLatch listSignal = new CountDownLatch(1);
        UserController.listUserInfoWithPicture(new ExtendedTaskDelegateAdapter<Void, List<UserInfoPicture>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserInfoPicture> userInfoPictures) {
                List<String> userMailsServer = new ArrayList<>();
                for (UserInfoPicture userInfoPicture : userInfoPictures) {
                    userMailsServer.add(userInfoPicture.getEmail());
                }

                assertTrue(userMailsServer.containsAll(userMails) &&
                        userMails.containsAll(userMailsServer));
                listSignal.countDown();
            }
        }, userMails);
        assertTrue(listSignal.await(20, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Suche das richtige Ergebnis liefert.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testSearchUsers() throws InterruptedException {
        final CountDownLatch searchSignal = new CountDownLatch(1);
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<UserInfo>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserInfo> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                assertEquals(result.get(0).getEmail(), TEST_MAIL);
                searchSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(searchSignal.await(20, TimeUnit.SECONDS));
    }

    @SmallTest
    public void testUpdateUserLocation() throws InterruptedException {
        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.updateUserLocation(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid) {
                updateSignal.countDown();
            }
        }, TEST_MAIL, TEST_LATITUDE, TEST_LONGITUDE, TEST_CURRENT_EVENT_ID);
        assertTrue(updateSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserLocation(new ExtendedTaskDelegateAdapter<Void, UserLocation>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserLocation userLocation) {
                assertNotNull(userLocation);
                assertEquals(TEST_MAIL, userLocation.getEmail());
                assertEquals(new Date().getTime(), userLocation.getUpdatedAt().getValue(), 10000);
                assertEquals(TEST_LONGITUDE, userLocation.getLongitude(), 0.001);
                assertEquals(TEST_LATITUDE, userLocation.getLatitude(), 0.001);
                assertEquals(TEST_CURRENT_EVENT_ID, userLocation.getCurrentEventId());
                getSignal.countDown();
            }
        }, TEST_MAIL);
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
        newUserInfo.setEmail(TEST_MAIL);
        newUserInfo.setFirstName(TEST_FIRST_NAME);
        newUserInfo.setLastName(TEST_LAST_NAME);
        newUserInfo.setLastNameVisibility(TEST_LAST_NAME_VISIBILITY.getId());
        newUserInfo.setCity(TEST_CITY);
        newUserInfo.setCityVisibility(TEST_CITY_VISIBILITY.getId());
        newUserInfo.setDateOfBirth(TEST_DATE_OF_BIRTH);
        newUserInfo.setDateOfBirthVisibility(TEST_DATE_OF_BIRTH_VISIBILITY.getId());
        newUserInfo.setDescription(TEST_DESCRIPTION);
        newUserInfo.setGender(TEST_GENDER.getRepresentation());
        newUserInfo.setPostalCode(TEST_POSTAL_CODE);
        newUserInfo.setPostalCodeVisibility(TEST_POSTAL_CODE_VISIBILITY.getId());

        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.updateUserProfile(new ExtendedTaskDelegateAdapter<Void, UserInfo>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserInfo userInfo) {
                updateSignal.countDown();
            }
        }, newUserInfo, TEST_OPT_OUT_SEARCH, TEST_GROUP_VISIBILITY);
        assertTrue(updateSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser endUser) {
                UserInfo userInfo = endUser.getUserInfoPicture().getUserInfo();
                assertNotNull(userInfo);
                assertEquals(TEST_MAIL, userInfo.getEmail());
                assertEquals(TEST_FIRST_NAME, userInfo.getFirstName());
                assertEquals(TEST_LAST_NAME, userInfo.getLastName());
                assertEquals(TEST_LAST_NAME_VISIBILITY.getId(), userInfo.getLastNameVisibility());
                assertEquals(TEST_CITY, userInfo.getCity());
                assertEquals(TEST_CITY_VISIBILITY.getId(), userInfo.getCityVisibility());
                assertEquals(TEST_DATE_OF_BIRTH, userInfo.getDateOfBirth());
                assertEquals(TEST_DATE_OF_BIRTH_VISIBILITY.getId(), userInfo.getCityVisibility());
                assertEquals(TEST_DESCRIPTION, userInfo.getDescription());
                assertEquals(TEST_GENDER.getRepresentation(), userInfo.getGender());
                assertEquals(TEST_POSTAL_CODE, userInfo.getPostalCode());
                assertEquals(TEST_POSTAL_CODE_VISIBILITY.getId(), userInfo.getPostalCodeVisibility());

                assertEquals(TEST_GROUP_VISIBILITY.getId(), endUser.getGroupVisibility());
                assertEquals(TEST_OPT_OUT_SEARCH, endUser.getOptOutSearch());
                getSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));
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
        }, TEST_MAIL, TEST_MAIL_2);
        assertTrue(addFriendSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch getUserSignal = new CountDownLatch(1);
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser endUser) {
                assertNotNull(endUser.getMyFriends());
                assertTrue(endUser.getMyFriends().size() == 1);
                assertTrue(endUser.getMyFriends().get(0).equals(TEST_MAIL_2));
                getUserSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(getUserSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch removeFriendSignal = new CountDownLatch(1);
        UserController.removeFriend(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                assertTrue(result);
                removeFriendSignal.countDown();
            }
        }, TEST_MAIL, TEST_MAIL_2);
        assertTrue(removeFriendSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch getUserRemovedSignal = new CountDownLatch(1);
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser endUser) {
                assertNull(endUser.getMyFriends());
                getUserRemovedSignal.countDown();
            }
        }, TEST_MAIL);

        assertTrue(getUserRemovedSignal.await(30, TimeUnit.SECONDS));
    }

    // TODO: Wenn Eventcontroller fertig
    @SmallTest
    public void testListEvents() {

    }

    // TODO: Wenn Groupcontroller fertig
    @SmallTest
    public void testListUserGroups() {

    }
}
