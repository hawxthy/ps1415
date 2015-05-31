package ws1415.common.controller;

import android.test.suitebuilder.annotation.SmallTest;

import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.model.UserGroupType;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.model.Right;

/**
 * Created by Bernd on 22.05.2015.
 */
public class RightControllerTest extends AuthenticatedAndroidTestCase {
    private String MY_MAIL = "";
    private String MY_SECOND_MAIL = "";
    final private String TEST_GROUP_NAME = "Testgruppe1";
    final private boolean TEST_BOOLEAN_GROUP_PRIVAT = false;
    final private String TEST_GROUP_PASSWORD = "Testgruppe1";
    final private UserGroupType TEST_GROUP_TYPE = UserGroupType.NORMALGROUP;
    final private String TEST_RIGHT_FULLRIGHTS = Right.FULLRIGHTS.name();
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
        }, TEST_GROUP_NAME, TEST_BOOLEAN_GROUP_PRIVAT, TEST_GROUP_TYPE, TEST_GROUP_PASSWORD);
        try {
            assertTrue("setUp for createUserGroup failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        changeAccount(1);
        final CountDownLatch signal2 = new CountDownLatch(1);

        GroupController.getInstance().joinUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BooleanWrapper result) {
                assertTrue("Der User " +MY_SECOND_MAIL+ " sollte erfolgreich der Nutzergruppe " +TEST_GROUP_NAME+ " beigetreten sein",result.getValue());
                signal2.countDown();
            }
        }, TEST_GROUP_NAME, TEST_GROUP_PASSWORD);
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

    @SmallTest
    public void testDistributeTakeRightToFromUser(){
        final CountDownLatch signal = new CountDownLatch(1);
        MY_SECOND_MAIL = getAccountMail(1);

        RightController.getInstance().giveRightToUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, MY_SECOND_MAIL, TEST_RIGHT_DELETEGROUP);
        try{
            assertTrue("giveRightToUser failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e){
            e.printStackTrace();
        }


        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                ArrayList<String> rights = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rights.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DELETEGROUP + " haben!", rights.contains(TEST_RIGHT_DELETEGROUP));
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("getUserGroup failed", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);
        RightController.getInstance().takeRightFromUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, MY_SECOND_MAIL, TEST_RIGHT_DELETEGROUP);
        try{
            assertTrue("takeRightFromUser failed", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                ArrayList<String> rights = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rights.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DELETEGROUP + " nicht haben!", rights.contains(TEST_RIGHT_DELETEGROUP));
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("getUserGroup failed", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testDistributeTakeRightsToFromUser(){
        final CountDownLatch signal = new CountDownLatch(1);
        MY_SECOND_MAIL = getAccountMail(1);

        ArrayList<String> rights = new ArrayList<String>();
        rights.add(TEST_RIGHT_DELETEGROUP);
        rights.add(TEST_RIGHT_NEWMEMBERRIGHTS);
        rights.add(TEST_RIGHT_POSTBLACKBOARD);

        RightController.getInstance().giveRightsToUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, MY_SECOND_MAIL, rights);
        try{
            assertTrue("giveRightsToUser failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                ArrayList<String> rights = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DELETEGROUP + " haben!", rights.contains(TEST_RIGHT_DELETEGROUP));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_POSTBLACKBOARD + " haben!", rights.contains(TEST_RIGHT_POSTBLACKBOARD));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rights.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("getUserGroup failed", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);
        RightController.getInstance().takeRightsFromUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, MY_SECOND_MAIL, rights);
        try{
            assertTrue("takeRightsFromUser failed", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                ArrayList<String> rights = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DELETEGROUP + " nicht  haben!", rights.contains(TEST_RIGHT_DELETEGROUP));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_POSTBLACKBOARD + " nicht haben!", rights.contains(TEST_RIGHT_POSTBLACKBOARD));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rights.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("getUserGroup failed", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testDistributeTakeRightToFromUsers(){
        final CountDownLatch signal = new CountDownLatch(1);
        MY_MAIL = getAccountMail(0);
        MY_SECOND_MAIL = getAccountMail(1);

        ArrayList<String> users = new ArrayList<String>();
        users.add(MY_MAIL);
        users.add(MY_SECOND_MAIL);
        RightController.getInstance().giveRightToUsers(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, users, TEST_RIGHT_POSTBLACKBOARD);
        try{
            assertTrue("giveRightToUsers failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                ArrayList<String> rightsUser0 = (ArrayList<String>) group.getMemberRights().get(MY_MAIL);
                ArrayList<String> rightsUser1 = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_FULLRIGHTS + " haben!", rightsUser0.contains(TEST_RIGHT_FULLRIGHTS));
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_POSTBLACKBOARD + " haben!", rightsUser0.contains(TEST_RIGHT_POSTBLACKBOARD));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rightsUser1.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_POSTBLACKBOARD + " haben!", rightsUser1.contains(TEST_RIGHT_POSTBLACKBOARD));
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("getUserGroup failed", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);
        RightController.getInstance().takeRightFromUsers(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, users, TEST_RIGHT_POSTBLACKBOARD);
        try{
            assertTrue("takeRightFromUsers failed", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                ArrayList<String> rightsUser0 = (ArrayList<String>) group.getMemberRights().get(MY_MAIL);
                ArrayList<String> rightsUser1 = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_FULLRIGHTS + " haben!", rightsUser0.contains(TEST_RIGHT_FULLRIGHTS));
                assertFalse("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_POSTBLACKBOARD + " nicht haben!", rightsUser0.contains(TEST_RIGHT_POSTBLACKBOARD));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + "  haben!", rightsUser1.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_POSTBLACKBOARD + " nicht haben!", rightsUser1.contains(TEST_RIGHT_POSTBLACKBOARD));
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("getUserGroup failed", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testDistributeTakeRightsToFromUsers(){
        final CountDownLatch signal = new CountDownLatch(1);
        MY_MAIL = getAccountMail(0);
        MY_SECOND_MAIL = getAccountMail(1);

        ArrayList<String> users = new ArrayList<>();
        users.add(MY_MAIL);
        users.add(MY_SECOND_MAIL);

        ArrayList<String> rights = new ArrayList<>();
        rights.add(TEST_RIGHT_DELETEGROUP);
        rights.add(TEST_RIGHT_NEWMEMBERRIGHTS);
        rights.add(TEST_RIGHT_POSTBLACKBOARD);
        RightController.getInstance().giveRightsToUsers(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, users, rights);
        try{
            assertTrue("giveRightsToUsers failed", signal.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                ArrayList<String> rightsUser0 = (ArrayList<String>) group.getMemberRights().get(MY_MAIL);
                ArrayList<String> rightsUser1 = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_FULLRIGHTS + " haben!", rightsUser0.contains(TEST_RIGHT_FULLRIGHTS));
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_POSTBLACKBOARD + " haben!", rightsUser0.contains(TEST_RIGHT_POSTBLACKBOARD));
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_DELETEGROUP + " haben!", rightsUser0.contains(TEST_RIGHT_DELETEGROUP));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_POSTBLACKBOARD + " haben!", rightsUser1.contains(TEST_RIGHT_POSTBLACKBOARD));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rightsUser1.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DELETEGROUP + " haben!", rightsUser1.contains(TEST_RIGHT_DELETEGROUP));
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("getUserGroup failed", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal3 = new CountDownLatch(1);
        RightController.getInstance().takeRightsFromUsers(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, users, rights);
        try {
            assertTrue("takeRightsFromUsers failed", signal3.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                ArrayList<String> rightsUser0 = (ArrayList<String>) group.getMemberRights().get(MY_MAIL);
                ArrayList<String> rightsUser1 = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_FULLRIGHTS + " haben!", rightsUser0.contains(TEST_RIGHT_FULLRIGHTS));
                assertFalse("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_POSTBLACKBOARD + " nicht haben!", rightsUser0.contains(TEST_RIGHT_POSTBLACKBOARD));
                assertFalse("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_DELETEGROUP + " nicht haben!", rightsUser0.contains(TEST_RIGHT_DELETEGROUP));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rightsUser1.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_POSTBLACKBOARD + " nicht haben!", rightsUser1.contains(TEST_RIGHT_POSTBLACKBOARD));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DELETEGROUP + " nicht haben!", rightsUser1.contains(TEST_RIGHT_DELETEGROUP));
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("getUserGroup failed", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testChangeLeader(){
        MY_MAIL = getAccountMail(0);
        MY_SECOND_MAIL = getAccountMail(1);

        final CountDownLatch signal = new CountDownLatch(1);
        RightController.getInstance().changeLeader(new ExtendedTaskDelegateAdapter<Void, Void>(){
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid){
                signal.countDown();
            }
        }, TEST_GROUP_NAME, MY_SECOND_MAIL);
        try{
            assertTrue("changeLeader failed", signal.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal2 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                ArrayList<String> rightsUser0 = (ArrayList<String>) group.getMemberRights().get(MY_MAIL);
                ArrayList<String> rightsUser1 = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rightsUser0.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertFalse("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_FULLRIGHTS + " nicht haben!", rightsUser0.contains(TEST_RIGHT_FULLRIGHTS));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_FULLRIGHTS + " haben!", rightsUser1.contains(TEST_RIGHT_FULLRIGHTS));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " nichgt haben!", rightsUser1.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                signal2.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("getUserGroup failed", signal2.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        changeAccount(1);

        final CountDownLatch signal3 = new CountDownLatch(1);
        RightController.getInstance().changeLeader(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal3.countDown();
            }
        }, TEST_GROUP_NAME, MY_MAIL);
        try{
            assertTrue("changeLeader failed", signal3.await(30, TimeUnit.SECONDS));
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        final CountDownLatch signal4 = new CountDownLatch(1);
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                ArrayList<String> rightsUser0 = (ArrayList<String>) group.getMemberRights().get(MY_MAIL);
                ArrayList<String> rightsUser1 = (ArrayList<String>) group.getMemberRights().get(MY_SECOND_MAIL);
                assertFalse("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " nicht haben!", rightsUser0.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_FULLRIGHTS + " haben!", rightsUser0.contains(TEST_RIGHT_FULLRIGHTS));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_FULLRIGHTS + " nicht haben!", rightsUser1.contains(TEST_RIGHT_FULLRIGHTS));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rightsUser1.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                signal4.countDown();
            }
        }, TEST_GROUP_NAME);
        try {
            assertTrue("getUserGroup failed", signal4.await(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        changeAccount(0);
    }
}
