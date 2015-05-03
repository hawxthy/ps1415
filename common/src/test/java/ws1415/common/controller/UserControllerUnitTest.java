package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.EndUser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Diese Klasse wird dazu genutzt die Funktionalitäten des UserControllers zu testen.
 *
 * @author Martin Wrodarczyk
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class UserControllerUnitTest {
    public static final String TEST_MAIL = "test@gmail.com";

    @Test
    public void testCreateUser() throws Exception {
        UserController.createUser(null, TEST_MAIL);
    }

    @Test
    public void testGetFullUser() throws Exception{
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>(){
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser user) {
                assertEquals(TEST_MAIL, user.getEmail());
            }
        }, TEST_MAIL);
    }
}
