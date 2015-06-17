package ws1415.ps1415.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.InfoPair;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.RoutePoint;
import com.skatenight.skatenightAPI.model.ServerWaypoint;
import com.skatenight.skatenightAPI.model.Text;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserListData;
import com.skatenight.skatenightAPI.model.UserLocation;
import com.skatenight.skatenightAPI.model.UserPrimaryData;
import com.skatenight.skatenightAPI.model.UserProfile;
import com.skatenight.skatenightAPI.model.UserProfileEdit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.model.EventParticipationVisibility;
import ws1415.ps1415.model.Gender;
import ws1415.ps1415.model.UserGroupType;
import ws1415.ps1415.model.Visibility;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.ImageUtil;

/**
 * Diese Klasse wird dazu genutzt die Funktionalitäten des UserControllers zu testen, die
 * Authorisierung benötigen.
 *
 * Folgende E-Mail Accounts müssen auf dem Gerät registriert sein um die Tests auszuführen:
 * - {@code ADMIN_MAIL}
 * - {@code TEST_MAIL_1}
 * - {@code TEST_MAIL_2}
 * Dazu müssen diese in den Einstellungen des Gerätes mit dem Passwort, das in der Dokumenation
 * zu finden ist, hinzugefügt werden.
 *
 * @author Martin Wrodarczyk
 */
public class UserControllerTest extends AuthenticatedAndroidTestCase {
    // Testdaten für die Benutzererstellung
    public static final String ADMIN_MAIL = "skatenight.host@gmail.com";
    public static final String TEST_MAIL_1 = "skatenight.user1@gmail.com";
    public static final String TEST_MAIL_2 = "skatenight.user2@gmail.com";
    public static final List<String> TEST_MAILS = Arrays.asList(TEST_MAIL_1, TEST_MAIL_2);
    public static final String TEST_INITIAL_FIRST_NAME_1 = "testVorname1";
    public static final String TEST_INITIAL_FIRST_NAME_2 = "testVorname2";
    public static final String TEST_INITIAL_LAST_NAME_1 = "testNachname1";
    public static final String TEST_INITIAL_LAST_NAME_2 = "testNachname2";

    // Testdaten für das Updaten der Standortinformationen
    public static final double TEST_LONGITUDE = 7.626135;
    public static final double TEST_LATITUDE = 51.960665;
    public static Long TEST_CURRENT_EVENT_ID = 5L;

    // Testdaten für das Updaten des Benutzerprofils
    public static final String TEST_FIRST_NAME = "Martin";
    public static final Gender TEST_GENDER = Gender.MALE;
    public static final Visibility TEST_SHOW_PRIVATE_GROUPS = Visibility.FRIENDS;
    public static final Boolean TEST_OPT_OUT_SEARCH = true;

    public static final String TEST_LAST_NAME = "Müller";
    public static final Visibility TEST_LAST_NAME_VISIBILITY = Visibility.FRIENDS;
    public static final InfoPair TEST_LAST_NAME_PAIR = new InfoPair().
            setValue(TEST_LAST_NAME).
            setVisibility(TEST_LAST_NAME_VISIBILITY.name());

    public static final String TEST_CITY = "Münster";
    public static final Visibility TEST_CITY_VISIBILITY = Visibility.ONLY_ME;
    public static final InfoPair TEST_CITY_PAIR = new InfoPair().
            setValue(TEST_CITY).
            setVisibility(TEST_CITY_VISIBILITY.name());

    public static final String TEST_DATE_OF_BIRTH = "1990/01/01";
    public static final Visibility TEST_DATE_OF_BIRTH_VISIBILITY = Visibility.PUBLIC;
    public static final InfoPair TEST_DATE_OF_BIRTH_PAIR = new InfoPair().
            setValue(TEST_DATE_OF_BIRTH).
            setVisibility(TEST_DATE_OF_BIRTH_VISIBILITY.name());

    public static final String TEST_DESCRIPTION = "Neue Beschreibung";
    public static final Visibility TEST_DESCRIPTION_VISIBILITY = Visibility.PUBLIC;
    public static final InfoPair TEST_DESCRIPTION_PAIR = new InfoPair().
            setValue(TEST_DESCRIPTION).
            setVisibility(TEST_DESCRIPTION_VISIBILITY.name());

    public static final String TEST_POSTAL_CODE = "48159";
    public static final Visibility TEST_POSTAL_CODE_VISIBILITY = Visibility.PUBLIC;
    public static final InfoPair TEST_POSTAL_CODE_PAIR = new InfoPair().
            setValue(TEST_POSTAL_CODE).
            setVisibility(TEST_POSTAL_CODE_VISIBILITY.name());

    /**
     * Loggt den Benutzer ein und erstellt zwei Benutzer.
     *
     * @throws Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Wechsel initial zu dem ersten Benutzer
        changeAccount(TEST_MAIL_1);

        final CountDownLatch createSignal = new CountDownLatch(2);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                createSignal.countDown();
            }
        }, TEST_MAIL_1, TEST_INITIAL_FIRST_NAME_1, TEST_INITIAL_LAST_NAME_1);

        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                createSignal.countDown();
            }
        }, TEST_MAIL_2, TEST_INITIAL_FIRST_NAME_2, TEST_INITIAL_LAST_NAME_2);
        assertTrue(createSignal.await(45, TimeUnit.SECONDS));
    }

    /**
     * Löscht die beiden Benutzer die in {@link #setUp()} erstellt worden sind.
     *
     * @throws Exception
     */
    @Override
    public void tearDown() throws Exception {
        deleteUser(TEST_MAIL_1);
        deleteUser(TEST_MAIL_2);
        super.tearDown();
    }

    private void deleteUser(String email) throws InterruptedException {
        changeAccount(email);
        final CountDownLatch deleteSignal1 = new CountDownLatch(1);
        UserController.deleteUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid) {
                deleteSignal1.countDown();
            }
        }, email);
        assertTrue(deleteSignal1.await(45, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Existenz eines Benutzers richtig abgerufen wird.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testExistsUser() throws InterruptedException {
        final CountDownLatch existsSignal = new CountDownLatch(1);
        UserController.existsUser(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                assertTrue(result);
                existsSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(existsSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Standortinformationen eines Benutzers richtig abgerufen werden.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testGetUserLocation() throws InterruptedException, IOException {
        UserLocation userLocation = ServiceProvider.getService().userEndpoint().getUserLocation(TEST_MAIL_1).execute();
        assertNotNull(userLocation);
    }

    /**
     * Prüft, ob das Profil eines Benutzers richtig abgerufen wird.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testGetUserProfile() throws InterruptedException {
        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserProfile(new ExtendedTaskDelegateAdapter<Void, UserProfile>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserProfile userProfile) {
                assertNotNull(userProfile);
                assertEquals(TEST_MAIL_1, userProfile.getEmail());
                getSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft ob die Informationen von mehreren Benutzern richtig abgerufen werden.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testListUserInfo() throws InterruptedException {
        final CountDownLatch listSignal = new CountDownLatch(1);
        UserController.listUserInfo(new ExtendedTaskDelegateAdapter<Void, List<UserListData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserListData> userListData) {
                List<String> userMailsServer = new ArrayList<>();
                for (UserListData userInfo : userListData) {
                    userMailsServer.add(userInfo.getEmail());
                }

                assertTrue(userMailsServer.containsAll(TEST_MAILS) &&
                        TEST_MAILS.containsAll(userMailsServer));
                listSignal.countDown();
            }
        }, TEST_MAILS);
        assertTrue(listSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Suche das richtige Ergebnis liefert.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testSearchUsers() throws InterruptedException {
        final CountDownLatch searchSignal = new CountDownLatch(1);
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                assertEquals(result.get(0), TEST_MAIL_1);
                searchSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(searchSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch searchSignal2 = new CountDownLatch(1);
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                assertEquals(result.get(0), TEST_MAIL_1);
                searchSignal2.countDown();
            }
        }, TEST_INITIAL_FIRST_NAME_1 + " " + TEST_INITIAL_LAST_NAME_1);
        assertTrue(searchSignal2.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob Standortinformationen eines Benutzers richtig aktualisiert werden.
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testUpdateUserLocation() throws InterruptedException, IOException {
        changeAccount(ADMIN_MAIL);
        Event testEvent = createEvent();
        TEST_CURRENT_EVENT_ID = testEvent.getId();

        changeAccount(TEST_MAIL_1);
        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.updateUserLocation(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid) {
                updateSignal.countDown();
            }
        }, TEST_MAIL_1, TEST_LATITUDE, TEST_LONGITUDE, TEST_CURRENT_EVENT_ID);
        assertTrue(updateSignal.await(30, TimeUnit.SECONDS));

        UserLocation userLocation = ServiceProvider.getService().userEndpoint().getUserLocation(TEST_MAIL_1).execute();
        assertNotNull(userLocation);
        assertEquals(new Date().getTime(), userLocation.getUpdatedAt().getValue(), 10000);
        assertEquals(TEST_LONGITUDE, userLocation.getLongitude(), 0.001);
        assertEquals(TEST_LATITUDE, userLocation.getLatitude(), 0.001);
        assertEquals(TEST_CURRENT_EVENT_ID, userLocation.getCurrentEventId());

        changeAccount(ADMIN_MAIL);
        deleteEvent(testEvent);
    }

    /**
     * Prüft, ob die allgemeinen Informationen und die Einstellungen eines Benutzers richtig geändert
     * werden und ob Sichtbarkeitseinstellungen richtig umgesetzt wurden.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testUpdateUserProfile() throws InterruptedException, IOException {
        changeAccount(TEST_MAIL_1);
        // Neue Testdaten initialisieren
        UserProfileEdit userProfileEdit = new UserProfileEdit();
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setFirstName(TEST_FIRST_NAME);
        newUserInfo.setGender(TEST_GENDER.name());
        newUserInfo.setLastName(TEST_LAST_NAME_PAIR);
        newUserInfo.setCity(TEST_CITY_PAIR);
        newUserInfo.setDateOfBirth(TEST_DATE_OF_BIRTH_PAIR);
        newUserInfo.setDescription(TEST_DESCRIPTION_PAIR);
        newUserInfo.setPostalCode(TEST_POSTAL_CODE_PAIR);
        userProfileEdit.setEmail(TEST_MAIL_1);
        userProfileEdit.setUserInfo(newUserInfo);
        userProfileEdit.setOptOutSearch(TEST_OPT_OUT_SEARCH);
        userProfileEdit.setShowPrivateGroups(TEST_SHOW_PRIVATE_GROUPS.name());

        // Profildaten updaten
        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.updateUserProfile(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                assertTrue(aBoolean);
                updateSignal.countDown();
            }
        }, userProfileEdit);
        assertTrue(updateSignal.await(45, TimeUnit.SECONDS));

        changeAccount(TEST_MAIL_2);
        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserProfile(new ExtendedTaskDelegateAdapter<Void, UserProfile>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserProfile userProfile) {
                assertEquals(TEST_MAIL_1, userProfile.getEmail());
                assertEquals(TEST_FIRST_NAME, userProfile.getFirstName());
                assertEquals(TEST_GENDER.name(), userProfile.getGender());
                assertEquals(TEST_DATE_OF_BIRTH, userProfile.getDateOfBirth());
                assertEquals(TEST_DESCRIPTION, userProfile.getDescription());
                assertEquals(TEST_POSTAL_CODE, userProfile.getPostalCode());
                assertNull(userProfile.getLastName());
                assertNull(userProfile.getCity());
                getSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));

        // Suche sollte kein Ergebnis mehr liefern
        final CountDownLatch searchSignal2 = new CountDownLatch(1);
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> result) {
                assertNull(result);
                searchSignal2.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(searchSignal2.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob nach dem Beitreten einer Gruppe und nach der Teilnahme an einer Veranstaltung,
     * diese auf dem Profil richtig abgerufen werden.
     *
     * @throws IOException Wenn Serverzugriffe eine Exception werfen
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testJoinGroupAndEvent() throws InterruptedException, IOException {
        changeAccount(ADMIN_MAIL);
        // Veranstaltung und Nutzergruppe erstellen
        final Event testEvent = createEvent();
        final UserGroup testUserGroup = createUserGroup();

        changeAccount(TEST_MAIL_1);
        // An Veranstaltung teilnehmen und Nutzergruppe beitreten
        ServiceProvider.getService().eventEndpoint().joinEvent(testEvent.getId(), EventParticipationVisibility.PUBLIC.name()).execute();
        ServiceProvider.getService().groupEndpoint().joinUserGroup(testUserGroup.getName()).execute();

        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserProfile(new ExtendedTaskDelegateAdapter<Void, UserProfile>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserProfile userProfile) {
                assertTrue(userProfile.getEventCount() == 1);
                assertEquals(testUserGroup.getName(), userProfile.getMyUserGroups().get(0));
                getSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(getSignal.await(45, TimeUnit.SECONDS));

        // Veranstaltung und Nutzergruppe wieder löschen
        changeAccount(ADMIN_MAIL);
        deleteEvent(testEvent);
        deleteUserGroup(testUserGroup);
    }


    /**
     * Prüft, ob das Profilbild eines Benutzers richtig hochgeladen wird und abgerufen wird.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testUploadAndGetUserPicture() throws InterruptedException, IOException {
        changeAccount(TEST_MAIL_1);
        final Bitmap pictureTest = BitmapFactory.decodeStream(getClass().getClassLoader().
                getResourceAsStream("image/test_user_picture.png"));
        final byte[] pictureTestByte = ImageUtil.BitmapToByteArray(pictureTest);
        final BlobKey[] retrievedImageKey = new BlobKey[1];

        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.uploadUserPicture(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                assertTrue(aBoolean);
                updateSignal.countDown();
            }
        }, TEST_MAIL_1, ImageUtil.BitmapToInputStream(pictureTest));
        assertTrue(updateSignal.await(120, TimeUnit.SECONDS));

        final CountDownLatch listSignal = new CountDownLatch(1);
        UserController.listUserInfo(new ExtendedTaskDelegateAdapter<Void, List<UserListData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserListData> userListData) {
                retrievedImageKey[0] = userListData.get(0).getUserPicture();
                listSignal.countDown();
            }
        }, Collections.singletonList(TEST_MAIL_1));
        assertTrue(listSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserPicture(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                byte[] pictureServerByte = ImageUtil.BitmapToByteArray(bitmap);
                assertTrue(Arrays.equals(pictureTestByte, pictureServerByte));
                getSignal.countDown();
            }
        }, retrievedImageKey[0]);
        assertTrue(getSignal.await(120, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob das Profilbild eines Benutzers entfernt wird.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testRemoveUserPicture() throws InterruptedException {
        changeAccount(TEST_MAIL_1);
        final Bitmap pictureTest = BitmapFactory.decodeStream(getClass().getClassLoader().
                getResourceAsStream("image/test_user_picture.png"));

        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.uploadUserPicture(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                assertTrue(aBoolean);
                updateSignal.countDown();
            }
        }, TEST_MAIL_1, ImageUtil.BitmapToInputStream(pictureTest));
        assertTrue(updateSignal.await(120, TimeUnit.SECONDS));

        final CountDownLatch removeSignal = new CountDownLatch(1);
        UserController.removeUserPicture(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                assertTrue(aBoolean);
                removeSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(removeSignal.await(45, TimeUnit.SECONDS));
    }

    @SmallTest
    public void testGetPrimaryData() throws InterruptedException {
        changeAccount(TEST_MAIL_1);
        final BlobKey blobKeyTest = uploadUserPicture();

        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getPrimaryData(new ExtendedTaskDelegateAdapter<Void, UserPrimaryData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserPrimaryData userPrimaryData) {
                assertNotNull(userPrimaryData);
                assertNotNull(userPrimaryData.getPicture());
                assertEquals(TEST_MAIL_1, userPrimaryData.getEmail());
                assertEquals(TEST_INITIAL_FIRST_NAME_1, userPrimaryData.getFirstName());
                assertEquals(TEST_INITIAL_LAST_NAME_1, userPrimaryData.getLastName());
                assertEquals(blobKeyTest.getKeyString(), userPrimaryData.getPicture().getKeyString());
                getSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));
    }

    // Damit beim Abrufen der PrimaryData, der Abruf des Profilbildes getestet werden kann
    private BlobKey uploadUserPicture() throws InterruptedException {
        final Bitmap pictureTest = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.default_picture);
        final BlobKey[] retrievedImageKey = new BlobKey[1];

        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.uploadUserPicture(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                assertTrue(aBoolean);
                updateSignal.countDown();
            }
        }, TEST_MAIL_1, ImageUtil.BitmapToInputStream(pictureTest));
        assertTrue(updateSignal.await(120, TimeUnit.SECONDS));

        final CountDownLatch listSignal = new CountDownLatch(1);
        UserController.listUserInfo(new ExtendedTaskDelegateAdapter<Void, List<UserListData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserListData> userListData) {
                retrievedImageKey[0] = userListData.get(0).getUserPicture();
                listSignal.countDown();
            }
        }, Collections.singletonList(TEST_MAIL_1));
        assertTrue(listSignal.await(30, TimeUnit.SECONDS));

        return retrievedImageKey[0];
    }

    /**
     * Prüft, ob ein Freund richtig hinzugefügt und auch wieder entfernt wird.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testAddAndRemoveFriend() throws InterruptedException {
        changeAccount(TEST_MAIL_1);
        final CountDownLatch addFriendSignal = new CountDownLatch(1);
        UserController.addFriend(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                assertTrue(result);
                addFriendSignal.countDown();
            }
        }, TEST_MAIL_1, TEST_MAIL_2);
        assertTrue(addFriendSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch listSignal = new CountDownLatch(1);
        UserController.listFriends(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> friendList) {
                assertNotNull(friendList);
                assertTrue(friendList.size() == 1);
                assertTrue(friendList.get(0).equals(TEST_MAIL_2));
                listSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(listSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch removeFriendSignal = new CountDownLatch(1);
        UserController.removeFriend(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                assertTrue(result);
                removeFriendSignal.countDown();
            }
        }, TEST_MAIL_1, TEST_MAIL_2);
        assertTrue(removeFriendSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch listSignal2 = new CountDownLatch(1);
        UserController.listFriends(new ExtendedTaskDelegateAdapter<Void, List<String>>(){
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> friendList) {
                assertTrue(friendList.size() == 0);
                listSignal2.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(listSignal2.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Liste der Freunde richtig abgerufen werden.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testListFriends() throws InterruptedException {
        changeAccount(TEST_MAIL_1);
        final CountDownLatch addFriendSignal = new CountDownLatch(1);
        UserController.addFriend(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                assertTrue(result);
                addFriendSignal.countDown();
            }
        }, TEST_MAIL_1, TEST_MAIL_2);
        assertTrue(addFriendSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch listFriendsSignal = new CountDownLatch(1);
        UserController.listFriends(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> friendInfos) {
                assertTrue(friendInfos.size() == 1);
                assertEquals(TEST_MAIL_2, friendInfos.get(0));
                listFriendsSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(listFriendsSignal.await(30, TimeUnit.SECONDS));
    }

    // Erstellt ein Event mit einer Route
    private Event createEvent() throws IOException {
        Route route = new Route();
        route.setLength("54 m");
        route.setName("Test");
        route.setRouteData(new Text().setValue("cfh|Hy_pm@AJEZMn@CJAJAFAJ?J?L@PF`BFtAJfCF^??JATANChBm@B?nA_@RG\\IFAHA??B@B@@@@BDHDLDHxCdM`BdHv@bDT`AfAfGl@dFVlBLXBFDL@HDLLp@F\\@ZANAJ?L?`@Aj@??Gb@ZATAxAGVAvEIzEUj@A`CG~ACvA?|BGdAEXAT?r@EnAIXC^Ap@C~@E|@C\\Av@CfCEJA|AAl@An@@b@AJ?`A@v@FV@??h@U~@yA|@sAn@_A|@uAXc@^_@fAsAPa@Rc@HUL]DKBGDKLa@HUBKDQFSDOFSFSFOBGHQLSBGFIJIZSHEFE\\OZM\\Kd@O^KZId@KxA[ZEZChAET?N?P?P?XBh@FdARFBhATVBRBf@ANAbAOd@GLAPCNALENIv@G^EJCREFANGRKRMRQTY^e@~AwB`@e@NUPQLKJGb@Sb@O`@KTEXCPHf@LXJD@\\L`@Lx@T\\JJ@RBVBJAJ@HANCREPGPGPEBAdAa@rAUTCt@E\\?\\?t@D\\DZHJBXFXLDB^Lf@VbAr@XP`Ap@vB|ApB|APLd@^jA`Ab@ZTPj@h@LLt@l@ZV~@r@~@p@x@h@~@r@`At@tAfAf@b@B@v@n@b@\\VR`@Zp@h@~@p@dBpA~BjBbBpAXTXR`BnA`@Zv@n@HFjA~@`Ar@lA`Ad@^v@n@f@`@fA|@??RNBMBQTeA??R?HDHDFDFBD@B@D?BAHA@?DCJEJCvAg@NGNENCBAXEPAr@LrA^pBr@??_@|BIZa@lBMh@Wz@M^IRELIPMN??c@y@W_@aAsAi@m@m@k@EEWUw@o@EEcBwAQOy@m@_As@a@Y"));
        route.setRoutePoints(Arrays.asList(new RoutePoint().setLongitude(7.61148).setLatitude(51.97001), new RoutePoint().setLongitude(7.61136).setLatitude(51.9701), new RoutePoint().setLongitude(7.61118).setLatitude(51.970220000000005), new RoutePoint().setLongitude(7.61095).setLatitude(51.97039)));
        route.setWaypoints(Arrays.asList(new ServerWaypoint().setTitle("Wegpunkt 1").setLongitude(7.611478).setLatitude(51.970008), new ServerWaypoint().setTitle("Wegpunkt 2").setLongitude(7.610938999999999).setLatitude(51.970382)));
        route = ServiceProvider.getService().routeEndpoint().addRoute(route).execute();

        Event testEvent = new Event();
        testEvent.setTitle("Testevent");
        testEvent.setDate(new DateTime(1431442800000l));
        testEvent.setRouteFieldFirst(2);
        testEvent.setRouteFieldLast(3);
        testEvent.setDescription(new Text().setValue("Beschreibung"));
        testEvent.setMeetingPlace("Münster");
        testEvent.setFee(2);
        testEvent.setRoute(route);

        return ServiceProvider.getService().eventEndpoint().createEvent(testEvent).execute();
    }

    // Löscht ein Event und eine Route
    private void deleteEvent(Event event) throws IOException {
        ServiceProvider.getService().eventEndpoint().deleteEvent(event.getId()).execute();
        ServiceProvider.getService().routeEndpoint().deleteRoute(event.getRoute().getId()).execute();
    }

    // Erstellt eine Nutzergruppe
    private UserGroup createUserGroup() throws IOException {
        ServiceProvider.getService().groupEndpoint().createUserGroup("Testgruppe", UserGroupType.NORMALGROUP.name(), false).execute();
        return ServiceProvider.getService().groupEndpoint().getUserGroup("Testgruppe").execute();
    }

    // Löscht eine Nutzergruppe
    private void deleteUserGroup(UserGroup userGroup) throws IOException {
        ServiceProvider.getService().groupEndpoint().deleteUserGroup(userGroup.getName()).execute();
    }
}
