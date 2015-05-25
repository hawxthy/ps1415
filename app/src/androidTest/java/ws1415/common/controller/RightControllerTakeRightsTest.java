package ws1415.common.controller;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.util.Right;

/**
 * Created by Bernd on 25.05.2015.
 */
public class RightControllerTakeRightsTest extends AuthenticatedAndroidTestCase {
    private String MY_MAIL = "";
    private String MY_SECOND_MAIL = "";
    final private String TEST_GROUP_NAME = "Testgruppe1";
    final private boolean TEST_BOOLEAN_IS_OPEN = true;
    final private String TEST_RIGHT_DELETEGROUP = Right.DELETEGROUP.name();
    final private String TEST_RIGHT_POSTBLACKBOARD = Right.POSTBLACKBOARD.name();
    final private String TEST_RIGHT_NEWMEMBERRIGHTS = Right.NEWMEMBERRIGHTS.name();


    @Override
    public void setUp() throws Exception {
        super.setUp();

        MY_MAIL = getAccountMail(0);
        MY_SECOND_MAIL = getAccountMail(1);

        // Nutzergruppe erstellen, die bei jedem Test benötigt wird.
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().createUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_BOOLEAN_IS_OPEN);
        try {
            assertTrue("setUp for createUserGroup failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        changeAccount(1);
        final CountDownLatch signal2 = new CountDownLatch(1);

        GroupController.getInstance().joinUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>(){
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid){
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try{
            assertTrue("setUp for joinUserGroup failed", signal2.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        changeAccount(0);
    }

    @Override
    public void tearDown() {
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().deleteUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("tearDown for deleteUserGroup failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            super.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
