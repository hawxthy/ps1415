package ws1415.ps1415.controller;

import android.graphics.Bitmap;
import android.test.suitebuilder.annotation.SmallTest;

import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupBlackBoardTransport;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;
import com.skatenight.skatenightAPI.model.UserGroupNewsBoardTransport;
import com.skatenight.skatenightAPI.model.UserGroupPicture;
import com.skatenight.skatenightAPI.model.UserGroupVisibleMembers;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.model.Right;
import ws1415.ps1415.model.UserGroupType;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.controller.GroupController;

/**
 * Created by Bernd on 14.05.2015.
 */
public class GroupControllerTest extends AuthenticatedAndroidTestCase {
    private String MY_MAIL = "";
    private String MY_SECOND_MAIL = "";
    final private String TEST_GROUP_NAME = "Testgruppe1";
    final private String TEST_GROUP_NAME_2 = "Testgruppe2";
    final private boolean TEST_BOOLEAN_GROUP_PRIVAT_FALSE = false;
    final private boolean TEST_BOOLEAN_GROUP_PRIVAT_TRUE = true;
    final private UserGroupType TEST_GROUP_TYPE = UserGroupType.NORMALGROUP;
    final private String TEST_GROUP_PASSWORD = "Testgruppe1";
    final private String TEST_GROUP_PASSWORD_2 = "Testgruppe2";
    final private String TEST_GROUP_DESCRIPTION = "Testbeschreibung "+TEST_GROUP_NAME;
    final private String TEST_GROUP_DESCRIPTION_2 = "Testbeschreibung "+TEST_GROUP_NAME_2;
    final private String TEST_BLACK_BOARD_MESSAGE = "Das ist eine Blackboard Message";
    final private String TEST_BLACK_BOARD_MESSAGE_2 = "Das ist die zweite Blackboard Message";
    final private String TEST_BLACK_BOARD_MESSAGE_3 = "Das ist die dritte Blackboard Message";
    final private String TEST_NEWS_BOARD_MESSAGE = "Das ist eine Newsboard Message";
    final private String TEST_NEWS_BOARD_MESSAGE_2 = "Das ist die zweite Newsboard Message";
    final private String TEST_NEWS_BOARD_MESSAGE_3 = "Das ist die dritte Newsboard Message";

    // Variablen zum füllen von wichtigen Attributen, wie BoardEntries
    private boolean found;
    private Long boardEntryId;


    @Override
    public void setUp() throws Exception {
        super.setUp();

        MY_MAIL = getAccountMail(0);

        if(ServiceProvider.getService().groupEndpoint().getUserGroup(TEST_GROUP_NAME).execute() != null){
            ServiceProvider.getService().groupEndpoint().deleteUserGroup(TEST_GROUP_NAME).execute();
        }

        // Nutzergruppe erstellen, die bei jedem Test benötigt wird.
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().createOpenUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION);
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
            assertTrue("GetUserGroup failed", signal.await(30, TimeUnit.SECONDS));
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
                assertTrue("Der Ersteller einer Nutzergruppe sollte zu Anfang die vollen Rechte haben", found);
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
    public void testPostNewsBoard() {
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().postNewsBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_NEWS_BOARD_MESSAGE);
        try {
            assertTrue("testPostNewsBoard message 1 failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getNewsBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupNewsBoardTransport>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupNewsBoardTransport newsBoard) {
                assertFalse("Es sollte mindestens ein BoardEntry enthalten sein", newsBoard.getBoardEntries() == null);
                found = false;
                for (BoardEntry be : newsBoard.getBoardEntries()) {
                    assertFalse("Message_3 sollte nicht enthalten sein", be.getMessage().equals(TEST_NEWS_BOARD_MESSAGE_3));
                    assertFalse("Message_2 sollte nicht enthalten sein", be.getMessage().equals(TEST_NEWS_BOARD_MESSAGE_2));
                    if (be.getMessage().toString().equals(TEST_NEWS_BOARD_MESSAGE)) {
                        found = true;
                    }
                }
                assertTrue("Die erste Newsboard Message ist nicht enthalten", found);
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testNewsBoardMessage check value failed", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);
        GroupController.getInstance().postNewsBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, TEST_NEWS_BOARD_MESSAGE_2);
        try {
            assertTrue("testPostNewsBoard messgage 2 failed", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getNewsBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupNewsBoardTransport>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupNewsBoardTransport newsBoard) {
                assertFalse("Es sollte mindestens ein BoardEntry enthalten sein", newsBoard.getBoardEntries() == null);
                found = false;
                for (BoardEntry be : newsBoard.getBoardEntries()) {
                    if (be.getMessage().equals(TEST_NEWS_BOARD_MESSAGE_2)) {
                        found = true;
                    }
                }
                assertTrue("Die zweite Newsboard Message sollte enthalten sein", found);
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testNewsBoardMessageValue2 failed", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testRemoveNewsBoardMessage() {
        final CountDownLatch signal = new CountDownLatch(1);
        GroupController.getInstance().postNewsBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_NEWS_BOARD_MESSAGE);
        try {
            assertTrue("postNewsBoard failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getNewsBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupNewsBoardTransport>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupNewsBoardTransport newsBoard) {
                found = false;
                for (BoardEntry be : newsBoard.getBoardEntries()) {
                    assertFalse("Message_3 sollte nicht enthalten sein", be.getMessage().equals(TEST_NEWS_BOARD_MESSAGE_3));
                    assertFalse("Message_2 sollte nicht enthalten sein", be.getMessage().equals(TEST_NEWS_BOARD_MESSAGE_2));
                    if (be.getMessage().toString().equals(TEST_NEWS_BOARD_MESSAGE)) {
                        boardEntryId = be.getId();
                        found = true;
                    }
                }
                assertTrue("Die erste Newsboard Message ist nicht enthalten", found);
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testRemoveNewsBoardMessage failed to get the BoardEntry", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);
        GroupController.getInstance().deleteNewsMessage(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, boardEntryId);
        try {
            assertTrue("deleteNewsMessage failed", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getNewsBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupNewsBoardTransport>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupNewsBoardTransport newsBoard) {
                found = false;
                if (newsBoard.getBoardEntries() != null) {
                    for (BoardEntry be : newsBoard.getBoardEntries()) {
                        if (be.getMessage().equals(TEST_NEWS_BOARD_MESSAGE)) {
                            found = true;
                        }
                    }
                }
                assertFalse("Die Newsboard Message 1 sollte nicht mehr enthalten sein", found);
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("testRemoveNewsBoardMessage failed to check the removed BoardEntry", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testJoinAndLeaveUserGroup() {
        final CountDownLatch signal = new CountDownLatch(1);

        changeAccount(1);
        MY_SECOND_MAIL = getAccountMail(1);

        GroupController.getInstance().joinUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BooleanWrapper result) {
                assertTrue("Der User " + MY_SECOND_MAIL + " sollte erfolgreich der Nutzergruppe " + TEST_GROUP_NAME + " beigetreten sein", result.getValue());
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

        GroupController.getInstance().joinUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BooleanWrapper result) {
                assertTrue("Der user " + MY_SECOND_MAIL + " sollte der Nutzergruppe " + TEST_GROUP_NAME + " erfolgreicht begetreten sein", result.getValue());
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

        MY_MAIL = getAccountMail(0);

        GroupController.getInstance().getUserGroupMetaData(new ExtendedTaskDelegateAdapter<Void, UserGroupMetaData>(){
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupMetaData metaData){
                assertTrue("Es sollten Meta Daten zu" + TEST_GROUP_NAME + " existieren", metaData.getName().equals(TEST_GROUP_NAME));
                assertFalse("Die Meta Daten zu " + TEST_GROUP_NAME + " sollten öffentlich sein", metaData.getPrivat());
                assertTrue("Die Meta Daten zu " +TEST_GROUP_NAME+ " sollten den Ersteller "+MY_MAIL+ " haben", metaData.getCreator().equals(MY_MAIL));
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try{
            assertTrue("getUserGroupMetaData failed", signal.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testGetUserGroupMetaDatas(){
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().deleteUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("deleteUserGroup failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MY_MAIL = getAccountMail(0);

        final CountDownLatch signal2 = new CountDownLatch(1);

        GroupController.getInstance().getUserGroupMetaDatas(new ExtendedTaskDelegateAdapter<Void, List<UserGroupMetaData>>(){
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserGroupMetaData> metaDatas){
                if(metaDatas != null){
                    for(UserGroupMetaData metaData : metaDatas){
                        assertFalse("Die " + TEST_GROUP_NAME + " mit dem Ersteller " + MY_MAIL + " sollte nicht existieren", metaData.getName().equals(TEST_GROUP_NAME) && metaData.getCreator().equals(MY_MAIL));
                    }
                }
                signal2.countDown();
            }
        });
        try{
            assertTrue("getUserGroupMetaDatas failed", signal2.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);

        GroupController.getInstance().createOpenUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION);
        try {
            assertTrue("createUserGroup failed", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);

        GroupController.getInstance().createOpenUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal4.countDown();
            }
        }, TEST_GROUP_NAME_2, TEST_GROUP_DESCRIPTION_2);
        try {
            assertTrue("createUserGroup failed", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal5 = new CountDownLatch(1);

        GroupController.getInstance().getUserGroupMetaDatas(new ExtendedTaskDelegateAdapter<Void, List<UserGroupMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserGroupMetaData> metaDatas) {
                boolean found1 = false;
                boolean found2 = false;
                if (metaDatas.size() != 0) {
                    for (UserGroupMetaData metaData : metaDatas) {
                        if (metaData.getCreator().equals(MY_MAIL) && metaData.getName().equals(TEST_GROUP_NAME)) {
                            assertFalse("Die Meta Daten zu " +TEST_GROUP_NAME+ " sollten öffentlich sein", metaData.getPrivat());
                            assertTrue("Die Meta Daten zu " + TEST_GROUP_NAME + " sollten den Ersteller " + MY_MAIL + " haben", metaData.getCreator().equals(MY_MAIL));
                            found1 = true;
                        }
                        if (metaData.getCreator().equals(MY_MAIL) && metaData.getName().equals(TEST_GROUP_NAME_2)) {
                            assertFalse("Die Meta Daten zu " +TEST_GROUP_NAME_2+ " sollten öffentlich sein", metaData.getPrivat());
                            assertTrue("Die Meta Daten zu " + TEST_GROUP_NAME_2 + " sollten den Ersteller " + MY_MAIL + " haben", metaData.getCreator().equals(MY_MAIL));
                            found2 = true;
                        }
                    }
                }
                assertTrue("Es sollten Meta Daten zu " + TEST_GROUP_NAME + " existieren", found1);
                assertTrue("Es sollten Meta Daten zu " + TEST_GROUP_NAME_2 + " existieren", found2);
                signal5.countDown();
            }
        });
        try{
            assertTrue("getUserGroupMetaDatas failed", signal5.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal6 = new CountDownLatch(1);

        GroupController.getInstance().deleteUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal6.countDown();
            }
        }, TEST_GROUP_NAME_2);
        try {
            assertTrue("deleteUserGroup failed", signal6.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testChangeUserGroupPassword(){
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().deleteUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("deleteUserGroup failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().createPrivateUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal2.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_TYPE, TEST_GROUP_DESCRIPTION, TEST_GROUP_PASSWORD);
        try {
            assertTrue("createPrivateUserGroup failed", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        final CountDownLatch signal3 = new CountDownLatch(1);

        GroupController.getInstance().changeUserGroupPassword(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BooleanWrapper result) {
                assertTrue("Das Passwort sollte erfolgreich geändert worden sein", result.getValue());
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_PASSWORD, TEST_GROUP_PASSWORD_2);
        try{
            assertTrue("changeUserGroupPassword failed",signal3.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);

        GroupController.getInstance().changeUserGroupPassword(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BooleanWrapper result) {
                assertTrue("Das Passwort sollte erfolgreicht zurück geändert worden sein", result.getValue());
                signal4.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_PASSWORD_2, TEST_GROUP_PASSWORD);
        try{
            assertTrue("changeUserGroupPassword failed", signal4.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testMakeUserGroupPrivat(){
        final CountDownLatch signal = new CountDownLatch(1);

        MY_MAIL = getAccountMail(0);
        MY_SECOND_MAIL = getAccountMail(1);

        GroupController.getInstance().makeUserGroupPrivat(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_PASSWORD);
        try{
            assertTrue("changeUserGroupPrivacy failed", signal.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                assertTrue("Die Nutzergruppe hat den falschen namen", group.getName().equals(TEST_GROUP_NAME));
                assertTrue("Die Nutzergruppe ist nicht privat", group.getPrivat());
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("GetUserGroup failed", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        changeAccount(1);

        final CountDownLatch signal3 = new CountDownLatch(1);
        GroupController.getInstance().joinPrivateUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BooleanWrapper booleanWrapper) {
                assertFalse("Es war möglich " + TEST_GROUP_NAME + " mit dem Passwort: " + TEST_GROUP_PASSWORD_2 + " beizutreten.", booleanWrapper.getValue());
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_PASSWORD_2);
        try{
            assertTrue("joinUserGroup failed", signal3.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().joinPrivateUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BooleanWrapper booleanWrapper) {
                assertTrue("Es war nicht möglich " + TEST_GROUP_NAME + " mit dem Passwort: " + TEST_GROUP_PASSWORD + " beizutreten.", booleanWrapper.getValue());
                signal4.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_PASSWORD);
        try{
            assertTrue("joinUserGroup failed", signal3.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testGetUserGroupVisibleMembers(){
        final CountDownLatch signal = new CountDownLatch(1);

        MY_MAIL = getAccountMail(0);

        GroupController.getInstance().getUserGroupVisibleMembers(new ExtendedTaskDelegateAdapter<Void, UserGroupVisibleMembers>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupVisibleMembers visibleMembers) {
                assertTrue("Es sollte mindestens einen sichtbares Mitglied geben", visibleMembers.getList().size() > 0);
                found = false;
                for (String visibleMember : visibleMembers.getList()) {
                    if (visibleMember.equals(MY_MAIL)) {
                        found = true;
                    }
                }
                assertTrue("Das Mitglied " + MY_MAIL + "sollte sichtbar sein", found);
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try{
            assertTrue("getUserGroupVisibleMembers failed", signal.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testChangeMyVisibility(){
        final CountDownLatch signal = new CountDownLatch(1);

        MY_MAIL = getAccountMail(0);

        GroupController.getInstance().changeMyVisibility(new ExtendedTaskDelegateAdapter<Void, Void>(){
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid){
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try{
            assertTrue("changeMyVisibility failed", signal.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);

        GroupController.getInstance().getUserGroupVisibleMembers(new ExtendedTaskDelegateAdapter<Void, UserGroupVisibleMembers>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupVisibleMembers visibleMembers) {
                found = false;
                if (visibleMembers.getList() != null) {
                    for (String visibleMember : visibleMembers.getList()) {
                        if (visibleMember.equals(MY_MAIL)) {
                            found = true;
                        }
                    }
                }
                assertFalse("Das Mitglied " + MY_MAIL + "sollte nicht mehr sichtbar sein", found);
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try{
            assertTrue("getUserGroupVisibleMembers failed", signal2.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);

        GroupController.getInstance().changeMyVisibility(new ExtendedTaskDelegateAdapter<Void, Void>(){
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid){
                signal3.countDown();
            }
        }, TEST_GROUP_NAME);
        try{
            assertTrue("changeMyVisibility failed", signal3.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);

        GroupController.getInstance().getUserGroupVisibleMembers(new ExtendedTaskDelegateAdapter<Void, UserGroupVisibleMembers>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupVisibleMembers visibleMembers) {
                found = false;
                if (visibleMembers.getList() != null) {
                    for (String visibleMember : visibleMembers.getList()) {
                        if (visibleMember.equals(MY_MAIL)) {
                            found = true;
                        }
                    }
                }
                assertTrue("Das Mitglied " + MY_MAIL + "sollte wieder sichtbar sein", found);
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try{
            assertTrue("getUserGroupVisibleMembers failed", signal4.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testJoinPrivateGroup(){
        final CountDownLatch signal = new CountDownLatch(1);

        MY_MAIL = getAccountMail(0);
        MY_SECOND_MAIL = getAccountMail(1);

        GroupController.getInstance().makeUserGroupPrivat(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_PASSWORD);
        try{
            assertTrue("changeUserGroupPrivacy failed", signal.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);

        changeAccount(1);

        GroupController.getInstance().joinPrivateUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BooleanWrapper result) {
                assertFalse("Der User " + MY_SECOND_MAIL + " sollte der Nutzergruppe " + TEST_GROUP_NAME + "nicht beigetreten sein", result.getValue());
                signal2.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_PASSWORD_2);
        try{
            assertTrue("joinUserGroup failed", signal2.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);

        GroupController.getInstance().joinPrivateUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BooleanWrapper result) {
                assertTrue("Der User " + MY_SECOND_MAIL + " sollte der Nutzergruppe " + TEST_GROUP_NAME + "erfolgreich beigetreten sein", result.getValue());
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_PASSWORD);
        try{
            assertTrue("joinUserGroup failed", signal3.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        changeAccount(0);
    }

    @SmallTest
    public void testGetUserGroupPicture(){
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().getUserGroupPicture(new ExtendedTaskDelegateAdapter<Void, Bitmap>(){
            @Override
            public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                if(bitmap !=null){
                    signal.countDown();
                }
                signal.countDown();
            }
        },TEST_GROUP_NAME);
        try{
            assertTrue("getUserGroupPicture failed", signal.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
