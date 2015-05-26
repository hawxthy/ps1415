package ws1415.ps1415.authTest;

import android.test.suitebuilder.annotation.SmallTest;

import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupBlackBoardTransport;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.controller.GroupController;
import ws1415.common.model.UserGroupType;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.model.Right;

/**
 * Created by Bernd on 14.05.2015.
 */
public class GroupControllerUserGroupTest extends AuthenticatedAndroidTestCase {
    private String MY_MAIL = "";
    private String MY_SECOND_MAIL = "";
    final private String TEST_GROUP_NAME = "Testgruppe1";
    final private boolean TEST_BOOLEAN_IS_OPEN = true;
    final private UserGroupType TEST_GROUP_TYPE = UserGroupType.NORMALGROUP;
    final private String TEST_BLACK_BOARD_MESSAGE = "Das ist eine Blackboard Message";
    final private String TEST_BLACK_BOARD_MESSAGE_2 = "Das ist die zweite Blackboard Message";
    final private String TEST_BLACK_BOARD_MESSAGE_3 = "Das ist die dritte Blackboard Message";

    // Variablen zum füllen von wichtigen Attributen, wie BoardEntries
    private boolean found;
    private Long boardEntryId;


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
        }, TEST_GROUP_NAME, TEST_BOOLEAN_IS_OPEN, TEST_GROUP_TYPE);
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
                assertTrue("Die erstellte Nutzergruppe hat den falschen namen", group.getName().equals(TEST_GROUP_NAME));
                assertTrue("Die erstellte Nutzergruppe ist nicht vom Typ " + TEST_GROUP_TYPE.name(), group.getGroupType().equals(TEST_GROUP_TYPE.name()));
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("teastGetUserGroup failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testCreatorRights() {
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                found = false;
                ArrayList<String> memberRights = (ArrayList<String>) group.getMemberRights().get(MY_MAIL);
                for (String right : memberRights) {
                    if (right.equals(Right.FULLRIGHTS.name())) {
                        found = true;
                    }
                }
                assertTrue(found);
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testCreatorRights failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testPostBlackBoard() {
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_BLACK_BOARD_MESSAGE);
        try {
            assertTrue("testPostBlackBoard message 1 failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getBlackBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupBlackBoardTransport>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupBlackBoardTransport blackBoard) {
                assertFalse("Es sollte mindestens ein BoardEntry enthalten sein", blackBoard.getBoardEntries() == null);
                found = false;
                for (BoardEntry be : blackBoard.getBoardEntries()) {
                    assertFalse("Message_3 sollte nicht enthalten sein", be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE_3));
                    assertFalse("Message_2 sollte nicht enthalten sein", be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE_2));
                    if (be.getMessage().toString().equals(TEST_BLACK_BOARD_MESSAGE)) {
                        found = true;
                    }
                }
                assertTrue("Die erste Blackboard Message ist nicht enthalten", found);
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testBlackBoardMessage check value failed", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);
        GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, TEST_BLACK_BOARD_MESSAGE_2);
        try {
            assertTrue("testPostBlackBoard messgage 2 failed", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getBlackBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupBlackBoardTransport>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupBlackBoardTransport blackBoard) {
                assertFalse("Es sollte mindestens ein BoardEntry enthalten sein", blackBoard.getBoardEntries() == null);
                found = false;
                for (BoardEntry be : blackBoard.getBoardEntries()) {
                    if (be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE_2)) {
                        found = true;
                    }
                }
                assertTrue("Die zweite Blackboard Message sollte enthalten sein", found);
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testBlackBoardMessageValue2 failed", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testRemoveBlackBoardMessage() {
        final CountDownLatch signal = new CountDownLatch(1);
        GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_BLACK_BOARD_MESSAGE);
        try {
            assertTrue("postBlackBoard failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getBlackBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupBlackBoardTransport>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupBlackBoardTransport blackBoard) {
                found = false;
                for (BoardEntry be : blackBoard.getBoardEntries()) {
                    assertFalse("Message_3 sollte nicht enthalten sein", be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE_3));
                    assertFalse("Message_2 sollte nicht enthalten sein", be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE_2));
                    if (be.getMessage().toString().equals(TEST_BLACK_BOARD_MESSAGE)) {
                        boardEntryId = be.getId();
                        found = true;
                    }
                }
                assertTrue("Die erste Blackboard Message ist nicht enthalten", found);
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to get the BoardEntry", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);
        GroupController.getInstance().deleteBoardMessage(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, boardEntryId);
        try {
            assertTrue("deleteBoardMessage failed", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getBlackBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupBlackBoardTransport>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupBlackBoardTransport blackBoard) {
                found = false;
                if (blackBoard.getBoardEntries() != null) {
                    for (BoardEntry be : blackBoard.getBoardEntries()) {
                        if (be.getMessage().equals(TEST_BLACK_BOARD_MESSAGE)) {
                            found = true;
                        }
                    }
                }
                assertFalse("Die Blackboard Message 1 sollte nicht mehr enthalten sein", found);
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testRemoveBlackBoardMessage failed to check the removed BoardEntry", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testJoinAndLeaveUserGroup() {
        final CountDownLatch signal = new CountDownLatch(1);

        changeAccount(1);
        MY_SECOND_MAIL = getAccountMail(1);

        GroupController.getInstance().joinUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("Die joinUserGroup Methode konnte nicht ausgeführt werden", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                assertTrue("Die E-Mail " + MY_SECOND_MAIL + " sollte enthalten sein", group.getMemberRights().containsKey(MY_SECOND_MAIL));
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("die getUserGroup Methode konnte nicht ausgeführt werden", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);
        GroupController.getInstance().leaveUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("Die leaveUserGroup Methode konnte nicht ausgeführt werden", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                assertFalse("Die E-Mail " + MY_SECOND_MAIL + " sollte nicht enthalten sein", group.getMemberRights().containsKey(MY_SECOND_MAIL));
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("die getUserGroup Methode konnte nicht ausgeführt werden", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        changeAccount(0);
    }

    @SmallTest
    public void testRemoveMember() {
        final CountDownLatch signal = new CountDownLatch(1);

        changeAccount(1);
        MY_SECOND_MAIL = getAccountMail(1);

        GroupController.getInstance().joinUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("Die joinUserGroup Methode konnte nicht ausgeführt werden", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                assertTrue("Die E-Mail " + MY_SECOND_MAIL + " sollte enthalten sein", group.getMemberRights().containsKey(MY_SECOND_MAIL));
                found = false;
                ArrayList<String> memberRights = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                for (String right : memberRights) {
                    if (right.equals(Right.NEWMEMBERRIGHTS.name())) {
                        found = true;
                    }
                }
                assertTrue("Die E-Mail " + MY_SECOND_MAIL + " sollte das Rrecht " + Right.NEWMEMBERRIGHTS.name() + " haben", found);
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("Die getUserGroup Methode konnte nicht ausgeführt werden", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        changeAccount(0);

        final CountDownLatch signal3 = new CountDownLatch(1);
        GroupController.getInstance().removeMember(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, MY_SECOND_MAIL);
        try {
            assertTrue("Die removeMember Methode konnte nicht ausgeführter werden", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                assertFalse("Die E-Mail " + MY_SECOND_MAIL + " sollte nicht enthalten sein", group.getMemberRights().containsKey(MY_SECOND_MAIL));
                found = false;
                ArrayList<String> memberRights = (ArrayList<String>) group.getMemberRights().get(MY_MAIL);
                for (String right : memberRights) {
                    if (right.equals(Right.FULLRIGHTS.name())) {
                        found = true;
                    }
                }
                assertTrue("Die E-Mail " + MY_MAIL + " sollte das Rrecht " + Right.FULLRIGHTS.name() + " haben", found);
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("Die getUserGroup Methode konnte nicht ausgeführt werden", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testGetUserGroupMetaData(){
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().getUserGroupMetaData(new ExtendedTaskDelegateAdapter<Void, UserGroupMetaData>(){
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupMetaData metaData){

            }
        }, TEST_GROUP_NAME);
    }
}
