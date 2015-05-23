package ws1415.ps1415.authTest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.suitebuilder.annotation.SmallTest;

import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.InfoPair;
import com.skatenight.skatenightAPI.model.Text;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserListData;
import com.skatenight.skatenightAPI.model.UserLocation;
import com.skatenight.skatenightAPI.model.UserPicture;
import com.skatenight.skatenightAPI.model.UserProfile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.controller.UserController;
import ws1415.common.model.Gender;
import ws1415.common.model.Visibility;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.util.ImageUtil;
import ws1415.ps1415.R;

/**
 * Diese Klasse wird dazu genutzt die Funktionalitäten des UserControllers zu testen, die
 * Authorisierung benötigen.
 *
 * Für diese Tests müssen zwei E-Mail Adressen auf dem Gerät eingetragen sein. Anzumerken ist,
 * dass zu den beiden E-Mail Adressen zu Beginn gelöscht werden und nach Ablauf aller Tests
 * auch gelöscht bleiben.
 *
 * @author Martin Wrodarczyk
 */
public class UserControllerAuthTest extends AuthenticatedAndroidTestCase {
    public static boolean initialized = false;
    // Testdaten
    public static String TEST_MAIL = "";
    public static String TEST_MAIL_2 = "";
    public static String TEST_INITIAL_FIRST_NAME_1 = "Vorname1";
    public static String TEST_INITIAL_FIRST_NAME_2 = "Vorname2";
    public static String TEST_INITIAL_LAST_NAME_1 = "Nachname1";
    public static String TEST_INITIAL_LAST_NAME_2 = "Nachname2";

    public static final double TEST_LONGITUDE = 7.626135;
    public static final double TEST_LATITUDE = 51.960665;
    public static final Long TEST_CURRENT_EVENT_ID = 5L;
    public static final String TEST_FIRST_NAME = "Martin";
    public static final Gender TEST_GENDER = Gender.MALE;

    public static final String TEST_LAST_NAME = "Müller";
    public static final Visibility TEST_LAST_NAME_VISIBILITY = Visibility.FRIENDS;
    public static final InfoPair TEST_LAST_NAME_PAIR = new InfoPair().
            setValue(TEST_LAST_NAME).
            setVisibility(TEST_LAST_NAME_VISIBILITY.getId());

    public static final String TEST_CITY = "Münster";
    public static final Visibility TEST_CITY_VISIBILITY = Visibility.ONLY_ME;
    public static final InfoPair TEST_CITY_PAIR = new InfoPair().
            setValue(TEST_CITY).
            setVisibility(TEST_CITY_VISIBILITY.getId());

    public static final String TEST_DATE_OF_BIRTH = "1990/01/01";
    public static final Visibility TEST_DATE_OF_BIRTH_VISIBILITY = Visibility.PUBLIC;
    public static final InfoPair TEST_DATE_OF_BIRTH_PAIR = new InfoPair().
            setValue(TEST_DATE_OF_BIRTH).
            setVisibility(TEST_DATE_OF_BIRTH_VISIBILITY.getId());

    public static final String TEST_DESCRIPTION = "Neue Beschreibung";
    public static final Visibility TEST_DESCRIPTION_VISIBILITY = Visibility.PUBLIC;
    public static final InfoPair TEST_DESCRIPTION_PAIR = new InfoPair().
            setValue(TEST_DESCRIPTION).
            setVisibility(TEST_DESCRIPTION_VISIBILITY.getId());

    public static final String TEST_POSTAL_CODE = "48159";
    public static final Visibility TEST_POSTAL_CODE_VISIBILITY = Visibility.PUBLIC;
    public static final InfoPair TEST_POSTAL_CODE_PAIR = new InfoPair().
            setValue(TEST_POSTAL_CODE).
            setVisibility(TEST_POSTAL_CODE_VISIBILITY.getId());

    public static final Visibility TEST_GROUP_VISIBILITY = Visibility.PUBLIC;
    public static final Boolean TEST_OPT_OUT_SEARCH = true;

    /**
     * Loggt den Benutzer ein und erstellt zwei Benutzer zum Testen.
     *
     * @throws Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        changeAccount(0);
        TEST_MAIL = ServiceProvider.getEmail();

        changeAccount(1);
        TEST_MAIL_2 = ServiceProvider.getEmail();

        // Wechsel initial zu dem ersten Benutzer
        changeAccount(0);

        if(!initialized){
            boolean exists = ServiceProvider.getService().userEndpoint().existsUser(TEST_MAIL).execute().getValue();
            if(exists){
                deleteUser(0, TEST_MAIL);
            }
            exists = ServiceProvider.getService().userEndpoint().existsUser(TEST_MAIL_2).execute().getValue();
            if(exists){
                deleteUser(1, TEST_MAIL_2);
            }
        }

        final CountDownLatch createSignal1 = new CountDownLatch(1);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                createSignal1.countDown();
            }
        }, TEST_MAIL, TEST_INITIAL_FIRST_NAME_1, TEST_INITIAL_LAST_NAME_1, null);
        assertTrue(createSignal1.await(30, TimeUnit.SECONDS));

        final CountDownLatch createSignal2 = new CountDownLatch(1);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                createSignal2.countDown();
            }
        }, TEST_MAIL_2, TEST_INITIAL_FIRST_NAME_2, TEST_INITIAL_LAST_NAME_2, null);
        assertTrue(createSignal2.await(30, TimeUnit.SECONDS));
    }

    /**
     * Löscht die beiden Benutzer die in setUp erstellt worden sind.
     *
     * @throws Exception
     */
    @Override
    public void tearDown() throws Exception {
//        deleteUser(0, TEST_MAIL);
//        deleteUser(1, TEST_MAIL_2);
        super.tearDown();
    }

    private void deleteUser(int index, String email) throws InterruptedException {
        changeAccount(index);
        final CountDownLatch deleteSignal1 = new CountDownLatch(1);
        UserController.deleteUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid) {
                deleteSignal1.countDown();
            }
        }, email);
        assertTrue(deleteSignal1.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Existenz eines Benutzers richtig abgefragt wird.
     *
     * @throws InterruptedException
     */
    public void testExistsUser() throws InterruptedException {
        final CountDownLatch existsSignal = new CountDownLatch(1);
        UserController.existsUser(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                assertTrue(result);
                existsSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(existsSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft ob ein Benutzer richtig auf dem Server gespeichert wird.
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
                assertEquals(TEST_MAIL, user.getUserPicture().getEmail());
                assertEquals(TEST_MAIL, user.getUserInfo().getEmail());
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
     * Prüft, ob das Profil eines Benutzers richtig abgerufen wird.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testGetUserProfile() throws InterruptedException {
        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserProfile(new ExtendedTaskDelegateAdapter<Void, UserProfile>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserProfile userProfile) {
                assertNotNull(userProfile);
                assertEquals(TEST_MAIL, userProfile.getEmail());
                assertEquals(TEST_MAIL, userProfile.getUserInfo().getEmail());
                assertEquals(TEST_MAIL, userProfile.getUserPicture().getEmail());
                getSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft ob die allgemeinen Informationen von mehreren Benutzern richtig abgerufen werden.
     *
     * @throws Exception
     */
    @SmallTest
    public void testListUserInfo() throws InterruptedException {
        final List<String> userMails = new ArrayList<>();
        userMails.add(TEST_MAIL);
        userMails.add(TEST_MAIL_2);

        final CountDownLatch listSignal = new CountDownLatch(1);
        UserController.listUserInfo(new ExtendedTaskDelegateAdapter<Void, List<UserListData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserListData> userListData) {
                List<String> userMailsServer = new ArrayList<>();
                for (UserListData userInfo : userListData) {
                    userMailsServer.add(userInfo.getEmail());
                }

                assertTrue(userMailsServer.containsAll(userMails) &&
                        userMails.containsAll(userMailsServer));
                listSignal.countDown();
            }
        }, userMails, true);
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
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                assertEquals(result.get(0), TEST_MAIL);
                searchSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(searchSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch searchSignal2 = new CountDownLatch(1);
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                assertEquals(result.get(0), TEST_MAIL);
                searchSignal2.countDown();
            }
        }, TEST_INITIAL_FIRST_NAME_1);
        assertTrue(searchSignal2.await(30, TimeUnit.SECONDS));

        final CountDownLatch searchSignal3 = new CountDownLatch(1);
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                assertEquals(result.get(0), TEST_MAIL);
                searchSignal3.countDown();
            }
        }, TEST_INITIAL_LAST_NAME_1);
        assertTrue(searchSignal3.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob Standortinformationen eines Benutzers richtig aktualisiert werden.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testUpdateUserLocation() throws InterruptedException {
        changeAccount(0);
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
     * Prüft, ob die allgemeinen Informationen eines Benutzers richtig geändert werden und
     * ob Sichtbarkeitseinstellungen richtig umgesetzt wurden.
     *
     * TODO: Groupvisibility
     * @throws InterruptedException
     */
    @SmallTest
    public void testUpdateUserInfo() throws InterruptedException {
        changeAccount(0);
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setEmail(TEST_MAIL);
        newUserInfo.setFirstName(TEST_FIRST_NAME);
        newUserInfo.setGender(TEST_GENDER.getId());
        newUserInfo.setLastName(TEST_LAST_NAME_PAIR);
        newUserInfo.setCity(TEST_CITY_PAIR);
        newUserInfo.setDateOfBirth(TEST_DATE_OF_BIRTH_PAIR);
        newUserInfo.setDescription(TEST_DESCRIPTION_PAIR);
        newUserInfo.setPostalCode(TEST_POSTAL_CODE_PAIR);

        final CountDownLatch searchSignal1 = new CountDownLatch(1);
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                assertEquals(result.get(0), TEST_MAIL);
                searchSignal1.countDown();
            }
        }, TEST_MAIL);
        assertTrue(searchSignal1.await(30, TimeUnit.SECONDS));

        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.updateUserProfile(new ExtendedTaskDelegateAdapter<Void, UserInfo>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserInfo userInfo) {
                updateSignal.countDown();
            }
        }, newUserInfo, TEST_OPT_OUT_SEARCH, TEST_GROUP_VISIBILITY);
        assertTrue(updateSignal.await(30, TimeUnit.SECONDS));

        changeAccount(1);
        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserProfile(new ExtendedTaskDelegateAdapter<Void, UserProfile>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserProfile userProfile) {
                UserInfo userInfo = userProfile.getUserInfo();
                assertNotNull(userInfo);
                assertEquals(TEST_MAIL, userInfo.getEmail());
                assertEquals(TEST_FIRST_NAME, userInfo.getFirstName());
                assertEquals(TEST_GENDER.getId(), userInfo.getGender().intValue());
                assertNull(userInfo.getLastName().getValue());
                assertEquals(TEST_LAST_NAME_VISIBILITY.getId(), userInfo.getLastName().getVisibility());
                assertNull(userInfo.getCity().getValue());
                assertEquals(TEST_CITY_VISIBILITY.getId(), userInfo.getCity().getVisibility());
                assertEquals(TEST_DATE_OF_BIRTH, userInfo.getDateOfBirth().getValue());
                assertEquals(TEST_DATE_OF_BIRTH_VISIBILITY.getId(), userInfo.getDateOfBirth().getVisibility());
                assertEquals(TEST_DESCRIPTION, userInfo.getDescription().getValue());
                assertEquals(TEST_DESCRIPTION_VISIBILITY.getId(), userInfo.getDescription().getVisibility());
                assertEquals(TEST_POSTAL_CODE, userInfo.getPostalCode().getValue());
                assertEquals(TEST_POSTAL_CODE_VISIBILITY.getId(), userInfo.getPostalCode().getVisibility());
                getSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch searchSignal2 = new CountDownLatch(1);
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> result) {
                assertNull(result);
                searchSignal2.countDown();
            }
        }, TEST_MAIL);
        assertTrue(searchSignal2.await(30, TimeUnit.SECONDS));
    }


    /**
     * Prüft, ob das Profilbild eines Benutzers richtig geändert wird.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testUpdateUserPicture() throws InterruptedException {
        changeAccount(0);
        final Bitmap testImage = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.default_picture);
        final Text testImageText = ImageUtil.EncodeBitmapToText(testImage);

        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.updateUserPicture(new ExtendedTaskDelegateAdapter<Void, UserPicture>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserPicture userPicture) {
                updateSignal.countDown();
            }
        }, TEST_MAIL, testImage);
        assertTrue(updateSignal.await(120, TimeUnit.SECONDS));

        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserPicture(new ExtendedTaskDelegateAdapter<Void, UserPicture>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserPicture userPicture) {
                assertTrue(testImageText.getValue().equals(userPicture.getPicture().getValue()));
                getSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(getSignal.await(120, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob ein Freund richtig hinzugefügt und auch wieder entfernt wird.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testAddAndRemoveFriend() throws InterruptedException {
        String s = TEST_MAIL;
        changeAccount(0);
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

    /**
     * Prüft, ob die Liste der Freunde richtig abgerufen werden.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testListFriends() throws InterruptedException {
        changeAccount(0);
        final CountDownLatch addFriendSignal = new CountDownLatch(1);
        UserController.addFriend(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                assertTrue(result);
                addFriendSignal.countDown();
            }
        }, TEST_MAIL, TEST_MAIL_2);
        assertTrue(addFriendSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch listFriendsSignal = new CountDownLatch(1);
        UserController.listFriends(new ExtendedTaskDelegateAdapter<Void, List<UserListData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserListData> friendInfos) {
                assertTrue(friendInfos.size() == 1);
                assertEquals(TEST_MAIL_2, friendInfos.get(0).getEmail());
                listFriendsSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(listFriendsSignal.await(30, TimeUnit.SECONDS));
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
