package ws1415.ps1415.authTest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.GroupMemberRights;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.controller.GroupController;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.util.Right;
import ws1415.ps1415.Constants;
import ws1415.ps1415.activity.ShowInformationActivity;

/**
 * Created by Bernd on 14.05.2015.
 */
public class GroupControllerAuthTest extends AuthenticatedAndroidTestCase {
    private String MY_MAIL = "";
    final private String TEST_GROUP_NAME = "Testgruppe1";
    final private String TEST_BLACK_BOARD_MESSAGE = "Das ist eine Blackboard Message";
    final private String TEST_BLACK_BOARD_MESSAGE_2 = "Das ist die zweite Blackboard Message";
    final private String TEST_BLACK_BOARD_MESSAGE_3 = "Das ist die dritte Blackboard Message";

    // Variablen zum füllen von wichtigen Attributen, wie BoardEntries
    private boolean found;
    private BoardEntry boardEntry;
    private UserGroup userGroup;


    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Nutzergruppe erstellen, die bei jedem Test benötigt wird.
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().createUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("setUp for createUserGroup failed", signal.await(30, TimeUnit.SECONDS));
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
    public void testGetUserGroup() {
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                putUserGroup(group);
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("teastGetUserGroup failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue("Die erstellte Nutzergruppe hat den falschen namen", userGroup.getName().equals(TEST_GROUP_NAME));
    }

    @SmallTest
    public void testCreatorRights() {
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                putUserGroup(group);
                for (GroupMemberRights member : group.getMemberRanks()) {
                    if (member.getRights().contains(Right.DELETEGROUP.name())) {
                        found = true;
                        signal.countDown();
                    }
                }
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testCreatorRights failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        found = false;
//        assertTrue("Es gab kein Mitglied mit den Recht: FULLRIGHTS",found);
    }

    @SmallTest
    public void testPostBlackBoard() {
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_BLACK_BOARD_MESSAGE, MY_MAIL);
        try {
            assertTrue("testPostBlackBoard failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                putUserGroup(group);
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testBlackBoardMessageValue failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        found = false;
        for (BoardEntry be : userGroup.getBlackBoard()) {
            assertFalse("Message_3 sollte nicht enthalten sein", be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE_3));
            if (be.getMessage().toString().equals(TEST_BLACK_BOARD_MESSAGE)) {
                found = true;
            }
        }
        assertTrue("Die erste Blackboard Message ist nicht enthalten", found);

        GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_BLACK_BOARD_MESSAGE_2, MY_MAIL);
        try {
            assertTrue("testPostBlackBoard failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                putUserGroup(group);
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testBlackBoardMessageValue2 failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        found = false;
        for (BoardEntry be : userGroup.getBlackBoard()) {
            if (be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE_2)) {
                found = true;
            }
        }
        assertTrue("Die zweite Blackboard Message sollte enthalten sein", found);
    }

    @SmallTest
    public void testRemoveBlackBoardMessage() {
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_BLACK_BOARD_MESSAGE, MY_MAIL);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to post the message", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                putUserGroup(group);
                boardEntry = group.getBlackBoard().get(0);
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to get the BoardEntry", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //assertTrue("Die Nutzergruppe sollte die erste Message enthalten", userGroup.getBlackBoard().get(0).getMessage().equals(TEST_BLACK_BOARD_MESSAGE));

        GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_BLACK_BOARD_MESSAGE_2, MY_MAIL);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to post the message", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GroupController.getInstance().deleteBoardMessage(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, boardEntry);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to remove the BoardEntry", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                putUserGroup(group);
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to check the removed BoardEntry", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        found = false;
        for(BoardEntry be : userGroup.getBlackBoard()){
            if(be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE)){
                found = true;
            }
        }
        assertFalse("Die Blackboard Message 1 sollte nicht mehr enthalten sein", found);
    }

    public void putUserGroup(UserGroup group){
        this.userGroup = group;
    }
}
