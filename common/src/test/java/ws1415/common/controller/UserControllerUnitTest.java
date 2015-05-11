package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserInfoPicture;
import com.skatenight.skatenightAPI.model.UserLocation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Diese Klasse wird dazu genutzt die Funktionalitäten des UserControllers zu testen.
 *
 * @author Martin Wrodarczyk
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, emulateSdk = 18)
public class UserControllerUnitTest {
    public static boolean initialized = false;
    public static final String TEST_MAIL = "test@gmail.com";
    public static final String TEST_MAIL_2 = "test2@gmail.com";

    @Before
    public void testSetUp() throws Exception {
        // Lösche beim Start der Tests alle Benutzer vom Server
        if (!initialized) {
            deleteAllUsers();
            initialized = true;
        }

        // Zwei EndUser zum Testen erstellen
        final CountDownLatch createSignal = new CountDownLatch(1);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void voids) {
                createSignal.countDown();
            }
        }, TEST_MAIL_2);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void voids) {
                createSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(createSignal.await(30, TimeUnit.SECONDS));
    }

    @After
    public void testTearDown() throws Exception {
        deleteAllUsers();
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
        assertTrue(signal.await(45, TimeUnit.SECONDS));

        final CountDownLatch deleteAllSignal = new CountDownLatch(userInfosFinal.size());
        for (UserInfo userInfo : userInfosFinal) {
            UserController.deleteUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Void avoid) {
                    deleteAllSignal.countDown();
                }
            }, userInfo.getEmail());
        }
        assertTrue(deleteAllSignal.await(45, TimeUnit.SECONDS));
    }

    /**
     * Erstellt einen Benutzer und prüft ob der Benutzer auf dem Server gespeichert ist.
     *
     * @throws Exception
     */
    @Test
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
        assertTrue(getSignal.await(20, TimeUnit.SECONDS));
    }

    /**
     * Prüft ob die allgemeinen Informationen von Benutzern richtig abgerufen werden.
     *
     * @throws Exception
     */
    @Test
    public void testListUserInfo() throws InterruptedException {
        final List<String> userMails = new ArrayList<>();
        userMails.add(TEST_MAIL);
        userMails.add(TEST_MAIL_2);

        final CountDownLatch listSignal = new CountDownLatch(1);
        UserController.listUserInfo(new ExtendedTaskDelegateAdapter<Void, List<UserInfo>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserInfo> userInfos) {
                assertEquals(userMails.size(), userInfos.size());
                List<String> userMailsServer = new ArrayList<>();
                userMailsServer.add(userInfos.get(0).getEmail());
                userMailsServer.add(userInfos.get(1).getEmail());

                assertTrue(userMailsServer.containsAll(userMails)
                        && userMails.containsAll(userMailsServer));
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
    @Test
    public void testListUserInfoWithPicture() throws InterruptedException {
        final List<String> userMails = new ArrayList<>();
        userMails.add(TEST_MAIL);
        userMails.add(TEST_MAIL_2);

        final CountDownLatch listSignal = new CountDownLatch(1);
        UserController.listUserInfoWithPicture(new ExtendedTaskDelegateAdapter<Void, List<UserInfoPicture>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserInfoPicture> userInfoPictures) {
                assertEquals(userMails.size(), userInfoPictures.size());

                List<String> userMailsSever = new ArrayList<>();
                userMailsSever.add(userInfoPictures.get(0).getEmail());
                userMailsSever.add(userInfoPictures.get(1).getEmail());

                assertTrue(userMailsSever.containsAll(userMails)
                        && userMails.containsAll(userMailsSever));
                listSignal.countDown();
            }
        }, userMails);
        assertTrue(listSignal.await(20, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Standortinformationen eines Benutzers richtig abgerufen werden.
     *
     * @throws InterruptedException
     */
    @Test
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
        assertTrue(getSignal.await(20, TimeUnit.SECONDS));
    }

    /**
     * Prüft, ob die Standortinformationen eines Benutzers richtig abgerufen werden.
     *
     * @throws InterruptedException
     */
    @Test
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
     * Prüft, ob die Suche das richtige Ergebnis liefert.
     *
     * @throws InterruptedException
     */
    @Test
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

}
