package ws1415.common.controller;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.UserGroup;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.security.acl.Group;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

import static org.junit.Assert.assertEquals;

/**
 * Created by Bernd on 05.05.2015.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE, emulateSdk = 18)
public class GroupControllerUnitTest {
    public static final String TEST_GROUP_NAME = "Testgruppe1";


    @Test
    public void runBeforeClass() throws Exception{
        final CountDownLatch signal = new CountDownLatch(1);
        // Lösche alle Nutzergruppen, falls noch welche existieren
        GroupController.getInstance().getAllUserGroups(new ExtendedTaskDelegateAdapter<Void, List<UserGroup>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserGroup> groupList) {
                if (groupList.size() > 0) {
                    for (UserGroup group : groupList) {
                        GroupController.getInstance().deleteUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
                        }, group.getName());
                    }
                }
                Log.d("MESSAGE: ", "Deleting done, notifying the UIThread");
                signal.countDown();
            }
        });
        try{
            assert(signal.await(30, TimeUnit.SECONDS));
        }catch(InterruptedException io){
            io.printStackTrace();
        }

        // Erstelle eine neue Nutzergruppe auf dem Server
        GroupController.getInstance().createUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>(){
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid){
                Log.d("MESSAGE: ", "Created the UserGroup");
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try{
            assert(signal.await(10, TimeUnit.SECONDS));
        }catch(InterruptedException io){
            io.printStackTrace();
        }
    }

    @Test
    public void testCreateUserGroup() throws Exception{
        final CountDownLatch signal = new CountDownLatch(1);
        GroupController.getInstance().createUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>(){
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid){
                Log.d("MESSAGE: ", "Created the UserGroup");
                signal.countDown();
            }
        }, TEST_GROUP_NAME);
        try{
            assert(signal.await(10, TimeUnit.SECONDS));
        }catch(InterruptedException io){
            io.printStackTrace();
        }
    }

    @Test
    public void testGetAllUserGroups() throws Exception{
        final CountDownLatch signal = new CountDownLatch(1);
        GroupController.getInstance().getAllUserGroups(new ExtendedTaskDelegateAdapter<Void, List<UserGroup>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserGroup> groupList) {
                assertEquals(TEST_GROUP_NAME, groupList.get(0).getName(), "Der Name der Gruppe stimmt nicht überein");
                signal.countDown();
            }
        });
        try{
            assert(signal.await(10, TimeUnit.SECONDS));
        }catch(InterruptedException io){
            io.printStackTrace();
        }
    }
}
