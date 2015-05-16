package ws1415.ps1415.authTest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.GroupMemberRights;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
public class GroupControllerAuthTest extends ActivityInstrumentationTestCase2<ShowInformationActivity>{
    private String MY_MAIL = "";
    final private String TEST_GROUP_NAME = "Testgruppe1";

    public GroupControllerAuthTest(){
        super(ShowInformationActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Nutzer einloggen
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        if (credential.getSelectedAccountName() == null) {
            credential.setSelectedAccountName(prefs.getString("accountName", null));
            MY_MAIL = credential.getSelectedAccountName();
            ServiceProvider.login(credential);
        } else {
            throw new Exception("Benutzer muss vorher ausgewählt werden");
        }

        // Nutzergruppe erstellen, die bei jedem Test benötigt wird.
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().createUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try{
            assertTrue("setUp for createUserGroup failed", signal.await(30, TimeUnit.SECONDS));
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void tearDown(){
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().deleteUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>(){
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid){
                signal.countDown();
            }
        },TEST_GROUP_NAME);
        try{
            assertTrue("tearDown for deleteUserGroup failed", signal.await(30, TimeUnit.SECONDS));
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        try{
            super.tearDown();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testGetUserGroup(){
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>(){
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group){
                if(group.getName().equals(TEST_GROUP_NAME)){
                    signal.countDown();
                }
            }
        }, TEST_GROUP_NAME);
        try{
            assertTrue("teastGetUserGroup failed", signal.await(30, TimeUnit.SECONDS));
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @SmallTest
    public void testCreatorRights(){
        final CountDownLatch signal = new CountDownLatch(1);

        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>(){
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group){
                for(GroupMemberRights member : group.getMemberRanks()){
                    if(member.getRights().contains(Right.FULLRIGHTS.name())){
                        signal.countDown();
                    }
                }
            }
        }, TEST_GROUP_NAME);
        try{
            assertTrue("testCreatorRights failed", signal.await(30, TimeUnit.SECONDS));
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}
