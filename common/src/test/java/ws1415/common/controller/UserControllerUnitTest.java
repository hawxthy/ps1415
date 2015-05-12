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


}
