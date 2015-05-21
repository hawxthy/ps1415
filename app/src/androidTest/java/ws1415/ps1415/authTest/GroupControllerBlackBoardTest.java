package ws1415.ps1415.authTest;

import android.test.suitebuilder.annotation.SmallTest;

import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.controller.GroupController;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Bernd on 17.05.2015.
 */
public class GroupControllerBlackBoardTest extends AuthenticatedAndroidTestCase {
    private String MY_MAIL = "";
    final private String TEST_GROUP_NAME = "Testgruppe1";
    final private boolean TEST_BOOLEAN_IS_OPEN = true;
    final private String TEST_BLACK_BOARD_MESSAGE = "Das ist eine Blackboard Message";
    final private String TEST_BLACK_BOARD_MESSAGE_2 = "Das ist die zweite Blackboard Message";
    final private String TEST_BLACK_BOARD_MESSAGE_3 = "Das ist die dritte Blackboard Message";

    // Variablen zum füllen von wichtigen Attributen, wie BoardEntries
    private boolean found;
    private long boardEntryId;


    @Override
    public void setUp() throws Exception {
        super.setUp();

        MY_MAIL = getAccountMail(0);

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

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal2.countDown();
            }
        }, TEST_GROUP_NAME, TEST_BLACK_BOARD_MESSAGE);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to post the first message", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                found = false;
                for (BoardEntry be : group.getBlackBoard()) {
                    assertFalse("Message_3 sollte nicht enthalten sein", be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE_3));
                    assertFalse("Message_2 sollte nicht enthalten sein", be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE_2));
                    if (be.getMessage().toString().equals(TEST_BLACK_BOARD_MESSAGE)) {
                        boardEntryId = be.getId();
                        found = true;
                    }
                }
                assertTrue("Die erste Blackboard Message ist nicht enthalten", found);
                signal3.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to get the BoardEntry", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    @SmallTest
    public void testRemoveBlackBoardMessage() {
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().deleteBoardMessage(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, boardEntryId);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to remove the BoardEntry", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                found = false;
                if (group.getBlackBoard() != null) {
                    for (BoardEntry be : group.getBlackBoard()) {
                        if (be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE)) {
                            found = true;
                        }
                    }
                }
                assertFalse("Die Blackboard Message 1 sollte nicht mehr enthalten sein", found);
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to check the removed BoardEntry", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
