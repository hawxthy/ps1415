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
import java.util.Date;
import java.util.List;

import ws1415.common.model.Gender;
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
    // Um Tests zu Methoden zu schreiben, die Authentifizierung benötigen
    public static final String MY_MAIL = "martin.wrod@googlemail.com";

    public static final String TEST_MAIL = "test@gmail.com";
    public static final String TEST_MAIL_2 = "test2@gmail.com";

    public static final String TEST_FIRST_NAME = "Martin";
    public static final String TEST_LAST_NAME = "Müller";
    public static final String TEST_CITY = "Münster";
    public static final String TEST_DATE_OF_BIRTH = "1990-01-01";
    public static final String TEST_DESCRIPTION = "Neue Beschreibung";
    public static final Gender TEST_GENDER = Gender.MALE;
    public static final String TEST_POSTAL_CODE= "48159";

    public static final double TEST_LONGITUDE = 7.626135;
    public static final double TEST_LATITUDE = 51.960665;
    public static final long TEST_CURRENT_EVENT_ID = 5L;


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
                List<String> userMailsSever = new ArrayList<>();
                userMailsSever.add(userInfos.get(0).getEmail());
                userMailsSever.add(userInfos.get(1).getEmail());

                assertTrue(userMailsSever.containsAll(userMails)
                        && userMails.containsAll(userMailsSever));
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
    }

    /**
     * Prüft, ob die allgemeinen Informationen eines Benutzers richtig geändert werden.
     * @throws InterruptedException
     */
    @Test
    public void testUpdateUserInfo() throws InterruptedException {
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

        Thread.sleep(4000);

        UserController.deleteUser(null, MY_MAIL);
    }

    /**
     * Prüft ob die Standortinformationen richtig geupdated werden.
     *
     * @throws InterruptedException
     */
    @Test
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

    // TODO: Profilbild ändern
    @Test
    public void testUpdateUserPicture(){

    }

    // TODO: Wenn Groupcontroller fertig
    public void testListUserGroups() throws InterruptedException {

    }

    //TODO: Wenn Eventcontroller fertig
    public void testListEvents() throws InterruptedException {

    }


}
