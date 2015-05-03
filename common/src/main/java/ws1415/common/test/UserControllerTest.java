package ws1415.common.test;

import com.skatenight.skatenightAPI.model.EndUser;

import junit.framework.TestCase;

import ws1415.common.controller.UserController;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

/**
 * Diese Klasse wird dazu genutzt die Funktionalitäten des UserControllers zu testen.
 *
 * @author Martin Wrodarczyk
 */
public class UserControllerTest extends TestCase {
    public static final String TEST_MAIL = "test@gmail.com";

    public void testCreateUser() throws Exception{
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, EndUser>(){
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser user) {
                assertEquals(TEST_MAIL, user.getEmail());
            }
        }, TEST_MAIL);
    }

    public void testGetFullUser() throws Exception{
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>(){
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser user) {
                assertEquals(TEST_MAIL, user.getEmail());
            }
        }, TEST_MAIL);
    }
}
