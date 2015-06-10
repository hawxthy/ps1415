package ws1415.ps1415.controller;

import android.test.suitebuilder.annotation.SmallTest;

import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.ps1415.model.Right;
import ws1415.ps1415.model.UserGroupType;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Created by Bernd on 22.05.2015.
 */
public class RightControllerTest extends AuthenticatedAndroidTestCase {
    private String MY_MAIL = "";
    private String MY_SECOND_MAIL = "";
    final private String TEST_GROUP_NAME = "Testgruppe1";
    final private boolean TEST_BOOLEAN_GROUP_PRIVATE_TRUE = true;
    final private boolean TEST_BOOLEAN_GROUP_PRIVATE_FALSE = false;
    final private String TEST_GROUP_PASSWORD = "Testgruppe1";
    final private UserGroupType TEST_GROUP_TYPE = UserGroupType.NORMALGROUP;
    final private String TEST_GROUP_DESCRIPTION = "Testbeschreibung "+TEST_GROUP_NAME;
    final private String TEST_RIGHT_FULLRIGHTS = Right.FULLRIGHTS.name();
    final private String TEST_RIGHT_DISTRIBUTERIGHTS = Right.DISTRIBUTERIGHTS.name();
    final private String TEST_RIGHT_EDITBLACKBOARD = Right.EDITBLACKBOARD.name();
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
        }, TEST_GROUP_NAME, TEST_GROUP_TYPE, null, TEST_BOOLEAN_GROUP_PRIVATE_FALSE, null, null);
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

    @SmallTest
    public void testDistributeTakeRightToFromUser(){
        final CountDownLatch signal = new CountDownLatch(1);
        MY_SECOND_MAIL = getAccountMail(1);

        RightController.getInstance().giveRightToUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME, MY_SECOND_MAIL, TEST_RIGHT_DISTRIBUTERIGHTS);
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
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DISTRIBUTERIGHTS + " haben!", rights.contains(TEST_RIGHT_DISTRIBUTERIGHTS));
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
        }, TEST_GROUP_NAME, MY_SECOND_MAIL, TEST_RIGHT_DISTRIBUTERIGHTS);
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
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DISTRIBUTERIGHTS + " nicht haben!", rights.contains(TEST_RIGHT_DISTRIBUTERIGHTS));
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
        rights.add(TEST_RIGHT_DISTRIBUTERIGHTS);
        rights.add(TEST_RIGHT_NEWMEMBERRIGHTS);
        rights.add(TEST_RIGHT_EDITBLACKBOARD);

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
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DISTRIBUTERIGHTS + " haben!", rights.contains(TEST_RIGHT_DISTRIBUTERIGHTS));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_EDITBLACKBOARD + " haben!", rights.contains(TEST_RIGHT_EDITBLACKBOARD));
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
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DISTRIBUTERIGHTS + " nicht  haben!", rights.contains(TEST_RIGHT_DISTRIBUTERIGHTS));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_EDITBLACKBOARD + " nicht haben!", rights.contains(TEST_RIGHT_EDITBLACKBOARD));
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
        }, TEST_GROUP_NAME, users, TEST_RIGHT_EDITBLACKBOARD);
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
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_EDITBLACKBOARD + " haben!", rightsUser0.contains(TEST_RIGHT_EDITBLACKBOARD));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rightsUser1.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_EDITBLACKBOARD + " haben!", rightsUser1.contains(TEST_RIGHT_EDITBLACKBOARD));
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
        }, TEST_GROUP_NAME, users, TEST_RIGHT_EDITBLACKBOARD);
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
                assertFalse("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_EDITBLACKBOARD + " nicht haben!", rightsUser0.contains(TEST_RIGHT_EDITBLACKBOARD));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + "  haben!", rightsUser1.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_EDITBLACKBOARD + " nicht haben!", rightsUser1.contains(TEST_RIGHT_EDITBLACKBOARD));
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
        rights.add(TEST_RIGHT_DISTRIBUTERIGHTS);
        rights.add(TEST_RIGHT_NEWMEMBERRIGHTS);
        rights.add(TEST_RIGHT_EDITBLACKBOARD);
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
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_EDITBLACKBOARD + " haben!", rightsUser0.contains(TEST_RIGHT_EDITBLACKBOARD));
                assertTrue("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_DISTRIBUTERIGHTS + " haben!", rightsUser0.contains(TEST_RIGHT_DISTRIBUTERIGHTS));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_EDITBLACKBOARD + " haben!", rightsUser1.contains(TEST_RIGHT_EDITBLACKBOARD));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rightsUser1.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DISTRIBUTERIGHTS + " haben!", rightsUser1.contains(TEST_RIGHT_DISTRIBUTERIGHTS));
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
                assertFalse("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_EDITBLACKBOARD + " nicht haben!", rightsUser0.contains(TEST_RIGHT_EDITBLACKBOARD));
                assertFalse("Das Mitglied " + MY_MAIL + " sollte das Recht " + TEST_RIGHT_DISTRIBUTERIGHTS + " nicht haben!", rightsUser0.contains(TEST_RIGHT_DISTRIBUTERIGHTS));
                assertTrue("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_NEWMEMBERRIGHTS + " haben!", rightsUser1.contains(TEST_RIGHT_NEWMEMBERRIGHTS));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_EDITBLACKBOARD + " nicht haben!", rightsUser1.contains(TEST_RIGHT_EDITBLACKBOARD));
                assertFalse("Das Mitglied " + MY_SECOND_MAIL + " sollte das Recht " + TEST_RIGHT_DISTRIBUTERIGHTS + " nicht haben!", rightsUser1.contains(TEST_RIGHT_DISTRIBUTERIGHTS));
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
