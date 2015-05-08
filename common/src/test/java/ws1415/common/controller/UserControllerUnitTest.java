package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserInfoPicture;
import com.skatenight.skatenightAPI.model.UserLocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

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
    public static final String TEST_MAIL = "test@gmail.com";
    public static final String TEST_MAIL_2 = "test2@gmail.com";

    /**
     * Erstellt einen Benutzer und prüft ob der Benutzer auf dem Server gespeichert ist.
     *
     * @throws Exception
     */
    @Test
    public void testCreateAndGetUser() throws Exception {
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void voids) {
            }
        }, TEST_MAIL);

        Thread.sleep(4000);

        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser user) {
                assertEquals(TEST_MAIL, user.getEmail());
                assertEquals(TEST_MAIL, user.getUserInfoPicture().getEmail());
                assertEquals(TEST_MAIL, user.getUserInfoPicture().getUserPicture().getEmail());
                assertEquals(TEST_MAIL, user.getUserInfoPicture().getUserInfo().getEmail());
                assertEquals(TEST_MAIL, user.getUserLocation().getEmail());
            }
        }, TEST_MAIL);

        Thread.sleep(2000);

        UserController.deleteUser(null, TEST_MAIL);
    }

    /**
     * Prüft ob die allgemeinen Informationen von Benutzern richtig abgerufen werden.
     *
     * @throws Exception
     */
    @Test
    public void testListUserInfo() throws Exception {
        UserController.createUser(null, TEST_MAIL);
        UserController.createUser(null, TEST_MAIL_2);

        Thread.sleep(4000);

        final List<String> userMails = new ArrayList<>();
        userMails.add(TEST_MAIL);
        userMails.add(TEST_MAIL_2);


        UserController.listUserInfo(new ExtendedTaskDelegateAdapter<Void, List<UserInfo>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserInfo> userInfos) {
                assertEquals(userMails.size(), userInfos.size());
                List<String> userMailsServer = new ArrayList<>();
                userMailsServer.add(userInfos.get(0).getEmail());
                userMailsServer.add(userInfos.get(1).getEmail());

                assertTrue(userMailsServer.containsAll(userMails)
                        && userMails.containsAll(userMailsServer));
            }
        }, userMails);

        Thread.sleep(3000);

        UserController.deleteUser(null, TEST_MAIL);
        UserController.deleteUser(null, TEST_MAIL_2);
    }

    /**
     * Prüft ob die allgemeinen Informationen von Benutzern zusammen mit den Profilbildern richtig
     * abgerufen werden.
     *
     * @throws Exception
     */
    @Test
    public void testListUserInfoWithPicture() throws InterruptedException {
        UserController.createUser(null, TEST_MAIL);
        UserController.createUser(null, TEST_MAIL_2);

        Thread.sleep(4000);

        final List<String> userMails = new ArrayList<>();
        userMails.add(TEST_MAIL);
        userMails.add(TEST_MAIL_2);

        UserController.listUserInfoWithPicture(new ExtendedTaskDelegateAdapter<Void, List<UserInfoPicture>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserInfoPicture> userInfoPictures) {
                assertEquals(userMails.size(), userInfoPictures.size());
                assertNotNull(userInfoPictures.get(0).getUserPicture());
                assertNotNull(userInfoPictures.get(0).getUserInfo());
                assertNotNull(userInfoPictures.get(1).getUserPicture());
                assertNotNull(userInfoPictures.get(1).getUserInfo());

                List<String> userMailsSever = new ArrayList<>();
                userMailsSever.add(userInfoPictures.get(0).getEmail());
                userMailsSever.add(userInfoPictures.get(1).getEmail());

                assertTrue(userMailsSever.containsAll(userMails)
                        && userMails.containsAll(userMailsSever));
            }
        }, userMails);

        Thread.sleep(3000);

        UserController.deleteUser(null, TEST_MAIL);
        UserController.deleteUser(null, TEST_MAIL_2);
    }

    /**
     * Prüft, ob die Standortinformationen eines Benutzers richtig abgerufen werden.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetUserLocation() throws InterruptedException {
        UserController.createUser(null, TEST_MAIL);

        Thread.sleep(4000);

        UserController.getUserLocation(new ExtendedTaskDelegateAdapter<Void, UserLocation>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserLocation userLocation) {
                assertNotNull(userLocation);
                assertEquals(TEST_MAIL, userLocation.getEmail());
            }
        }, TEST_MAIL);

        Thread.sleep(3000);

        UserController.deleteUser(null, TEST_MAIL);
    }

    /**
     * Prüft, ob die Standortinformationen eines Benutzers richtig abgerufen werden.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetUserInfo() throws InterruptedException {
        UserController.createUser(null, TEST_MAIL);

        Thread.sleep(4000);

        UserController.getUserLocation(new ExtendedTaskDelegateAdapter<Void, UserLocation>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserLocation userLocation) {
                assertNotNull(userLocation);
                assertEquals(TEST_MAIL, userLocation.getEmail());
            }
        }, TEST_MAIL);

        Thread.sleep(3000);

        UserController.deleteUser(null, TEST_MAIL);
    }


    // TODO: Wenn Groupcontroller fertig
    public void testListUserGroups() throws InterruptedException {

    }

    //TODO: Wenn Eventcontroller fertig
    public void testListEvents() throws InterruptedException {

    }


}
