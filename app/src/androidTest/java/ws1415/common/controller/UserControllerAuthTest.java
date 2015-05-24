package ws1415.common.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.InfoPair;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserListData;
import com.skatenight.skatenightAPI.model.UserLocation;
import com.skatenight.skatenightAPI.model.UserProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
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
 * Die aufgeführten E-Mail Accounts müssen zum Testen auf dem Gerät registriert sein. Dazu
 * müssen sie in den Einstellungen des Gerätes mit dem Passwort: "skatenight123" hinzugefügt
 * werden.
 *
 * @author Martin Wrodarczyk
 */
public class UserControllerAuthTest extends AuthenticatedAndroidTestCase {
    // Testdaten
    public static final String ADMIN_MAIL = "skatenight.host@gmail.com";
    public static final String TEST_MAIL_1 = "skatenight.user1@gmail.com";
    public static final String TEST_MAIL_2 = "skatenight.user2@gmail.com";
    public static final List<String> TEST_MAILS = Arrays.asList(TEST_MAIL_1, TEST_MAIL_2);
    public static final String TEST_INITIAL_FIRST_NAME_1 = "testVorname1";
    public static final String TEST_INITIAL_FIRST_NAME_2 = "testVorname2";
    public static final String TEST_INITIAL_LAST_NAME_1 = "testNachname1";
    public static final String TEST_INITIAL_LAST_NAME_2 = "testNachname2";

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
        assertTrue(createSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Löscht die beiden Benutzer die in setUp erstellt worden sind.
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
        assertTrue(deleteSignal1.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Existenz eines Benutzers richtig abgefragt wird.
     *
     * @throws InterruptedException
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
     * Prüft ob ein Benutzer richtig auf dem Server gespeichert wird.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testGetUser() throws InterruptedException {
        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser endUser) {
                assertNotNull(endUser);
                assertNotNull(endUser.getUserInfo());
                assertNotNull(endUser.getUserLocation());
                assertEquals(TEST_MAIL_1, endUser.getEmail());
                assertEquals(TEST_MAIL_1, endUser.getUserInfo().getEmail());
                assertEquals(TEST_MAIL_1, endUser.getUserLocation().getEmail());
                getSignal.countDown();
            }
        }, TEST_MAIL_1);
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
                assertEquals(TEST_MAIL_1, userLocation.getEmail());
                getSignal.countDown();
            }
        }, TEST_MAIL_1);
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
                assertNotNull(userProfile.getUserInfo());
                assertEquals(TEST_MAIL_1, userProfile.getEmail());
                assertEquals(TEST_MAIL_1, userProfile.getUserInfo().getEmail());
                getSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft ob die Informationen von mehreren Benutzern richtig abgerufen werden.
     *
     * @throws Exception
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
        }, TEST_INITIAL_FIRST_NAME_1);
        assertTrue(searchSignal2.await(30, TimeUnit.SECONDS));

        final CountDownLatch searchSignal3 = new CountDownLatch(1);
        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                assertEquals(result.get(0), TEST_MAIL_1);
                searchSignal3.countDown();
            }
        }, TEST_INITIAL_LAST_NAME_1);
        assertTrue(searchSignal3.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob Standortinformationen eines Benutzers richtig aktualisiert werden.
     * TODO: GÜLTIGES EVENT ZUWEISEN
     * @throws InterruptedException
     */
    @SmallTest
    public void testUpdateUserLocation() throws InterruptedException {
//        changeAccount(TEST_MAIL_1);
//        final CountDownLatch updateSignal = new CountDownLatch(1);
//        UserController.updateUserLocation(new ExtendedTaskDelegateAdapter<Void, Void>() {
//            @Override
//            public void taskDidFinish(ExtendedTask task, Void avoid) {
//                updateSignal.countDown();
//            }
//        }, TEST_MAIL_1, TEST_LATITUDE, TEST_LONGITUDE, TEST_CURRENT_EVENT_ID);
//        assertTrue(updateSignal.await(30, TimeUnit.SECONDS));
//
//        final CountDownLatch getSignal = new CountDownLatch(1);
//        UserController.getUserLocation(new ExtendedTaskDelegateAdapter<Void, UserLocation>() {
//            @Override
//            public void taskDidFinish(ExtendedTask task, UserLocation userLocation) {
//                assertNotNull(userLocation);
//                assertEquals(TEST_MAIL_1, userLocation.getEmail());
//                assertEquals(new Date().getTime(), userLocation.getUpdatedAt().getValue(), 10000);
//                assertEquals(TEST_LONGITUDE, userLocation.getLongitude(), 0.001);
//                assertEquals(TEST_LATITUDE, userLocation.getLatitude(), 0.001);
//                assertEquals(TEST_CURRENT_EVENT_ID, userLocation.getCurrentEventId());
//                getSignal.countDown();
//            }
//        }, TEST_MAIL_1);
//        assertTrue(getSignal.await(30, TimeUnit.SECONDS));
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
        changeAccount(TEST_MAIL_1);
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setEmail(TEST_MAIL_1);
        newUserInfo.setFirstName(TEST_FIRST_NAME);
        newUserInfo.setGender(TEST_GENDER.getId());
        newUserInfo.setLastName(TEST_LAST_NAME_PAIR);
        newUserInfo.setCity(TEST_CITY_PAIR);
        newUserInfo.setDateOfBirth(TEST_DATE_OF_BIRTH_PAIR);
        newUserInfo.setDescription(TEST_DESCRIPTION_PAIR);
        newUserInfo.setPostalCode(TEST_POSTAL_CODE_PAIR);

        final CountDownLatch updateSignal = new CountDownLatch(1);
        UserController.updateUserProfile(new ExtendedTaskDelegateAdapter<Void, UserInfo>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserInfo userInfo) {
                updateSignal.countDown();
            }
        }, newUserInfo, TEST_OPT_OUT_SEARCH, TEST_GROUP_VISIBILITY);
        assertTrue(updateSignal.await(30, TimeUnit.SECONDS));

        changeAccount(TEST_MAIL_2);
        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getUserProfile(new ExtendedTaskDelegateAdapter<Void, UserProfile>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserProfile userProfile) {
                UserInfo userInfo = userProfile.getUserInfo();
                assertNotNull(userInfo);
                assertEquals(TEST_MAIL_1, userInfo.getEmail());
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
        }, TEST_MAIL_1);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));

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
     * Prüft, ob das Profilbild eines Benutzers richtig geändert wird.
     *
     * @throws InterruptedException
     */
    @SmallTest
    public void testUpdateUserPicture() throws InterruptedException {
        changeAccount(TEST_MAIL_1);
        final Bitmap pictureTest = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.default_picture);
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
        }, Arrays.asList(TEST_MAIL_1));
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
     * Prüft, ob ein Freund richtig hinzugefügt und auch wieder entfernt wird.
     *
     * @throws InterruptedException
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

        final CountDownLatch getUserSignal = new CountDownLatch(1);
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser endUser) {
                assertNotNull(endUser.getMyFriends());
                assertTrue(endUser.getMyFriends().size() == 1);
                assertTrue(endUser.getMyFriends().get(0).equals(TEST_MAIL_2));
                getUserSignal.countDown();
            }
        }, TEST_MAIL_1);
        assertTrue(getUserSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch removeFriendSignal = new CountDownLatch(1);
        UserController.removeFriend(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                assertTrue(result);
                removeFriendSignal.countDown();
            }
        }, TEST_MAIL_1, TEST_MAIL_2);
        assertTrue(removeFriendSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch getUserRemovedSignal = new CountDownLatch(1);
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser endUser) {
                assertNull(endUser.getMyFriends());
                getUserRemovedSignal.countDown();
            }
        }, TEST_MAIL_1);

        assertTrue(getUserRemovedSignal.await(30, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Liste der Freunde richtig abgerufen werden.
     *
     * @throws InterruptedException
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
        UserController.listFriends(new ExtendedTaskDelegateAdapter<Void, List<UserListData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserListData> friendInfos) {
                assertTrue(friendInfos.size() == 1);
                assertEquals(TEST_MAIL_2, friendInfos.get(0).getEmail());
                listFriendsSignal.countDown();
            }
        }, TEST_MAIL_1);
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

    private Event createEvent() throws IOException {
        Route route = new Route();
        route.setLength("6,8 km");
        route.setName("Test");
        route.setRouteData(new Text().setValue("cfh|Hy_pm@AJEZMn@CJAJAFAJ?J?L@PF`BFtAJfCF^??JATANChBm@B?nA_@RG\\IFAHA??B@B@@@@BDHDLDHxCdM`BdHv@bDT`AfAfGl@dFVlBLXBFDL@HDLLp@F\\@ZANAJ?L?`@Aj@??Gb@ZATAxAGVAvEIzEUj@A`CG~ACvA?|BGdAEXAT?r@EnAIXC^Ap@C~@E|@C\\Av@CfCEJA|AAl@An@@b@AJ?`A@v@FV@??h@U~@yA|@sAn@_A|@uAXc@^_@fAsAPa@Rc@HUL]DKBGDKLa@HUBKDQFSDOFSFSFOBGHQLSBGFIJIZSHEFE\\OZM\\Kd@O^KZId@KxA[ZEZChAET?N?P?P?XBh@FdARFBhATVBRBf@ANAbAOd@GLAPCNALENIv@G^EJCREFANGRKRMRQTY^e@~AwB`@e@NUPQLKJGb@Sb@O`@KTEXCPHf@LXJD@\\L`@Lx@T\\JJ@RBVBJAJ@HANCREPGPGPEBAdAa@rAUTCt@E\\?\\?t@D\\DZHJBXFXLDB^Lf@VbAr@XP`Ap@vB|ApB|APLd@^jA`Ab@ZTPj@h@LLt@l@ZV~@r@~@p@x@h@~@r@`At@tAfAf@b@B@v@n@b@\\VR`@Zp@h@~@p@dBpA~BjBbBpAXTXR`BnA`@Zv@n@HFjA~@`Ar@lA`Ad@^v@n@f@`@fA|@??RNBMBQTeA??R?HDHDFDFBD@B@D?BAHA@?DCJEJCvAg@NGNENCBAXEPAr@LrA^pBr@??_@|BIZa@lBMh@Wz@M^IRELIPMN??c@y@W_@aAsAi@m@m@k@EEWUw@o@EEcBwAQOy@m@_As@a@Y"));
        // TODO RoutePoints und Waypoints setzen
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
}
