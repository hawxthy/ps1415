package ws1415.ps1415.authTest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.EndUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.common.controller.RoleController;
import ws1415.common.controller.UserController;
import ws1415.common.model.GlobalRole;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.Constants;
import ws1415.ps1415.activity.ImageStorageTestActivity;

/**
 * Muss von einem Admin, oder dem Admin der auf dem Server als "First Admin" eingetragen ist,
 * ausgeführt werden.
 */
public class AdminRoleAuthTest extends ActivityInstrumentationTestCase2<ImageStorageTestActivity> {
    public static String MY_MAIL = "";
    public static final String TEST_MAIL = "test@gmail.com";

    public static final GlobalRole TEST_ROLE = GlobalRole.ADMIN;

    public AdminRoleAuthTest() {
        super(ImageStorageTestActivity.class);
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

        final CountDownLatch createSignal = new CountDownLatch(1);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void voids) {
                createSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(createSignal.await(30, TimeUnit.SECONDS));
    }

    @Override
    public void tearDown() throws Exception {
        final CountDownLatch deleteSignal = new CountDownLatch(1);
        UserController.deleteUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid) {
                deleteSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(deleteSignal.await(30, TimeUnit.SECONDS));
        super.tearDown();
    }

    @SmallTest
    public void testAssignGlobalRole() throws InterruptedException {
        final CountDownLatch assignSignal = new CountDownLatch(1);
        RoleController.assignGlobalRole(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid){
                assignSignal.countDown();
            }
        }, TEST_MAIL, TEST_ROLE);
        assertTrue(assignSignal.await(30, TimeUnit.SECONDS));

        // Server zum aktualisieren Zeit lasen
        Thread.sleep(500);

        final CountDownLatch getSignal = new CountDownLatch(1);
        UserController.getFullUser(new ExtendedTaskDelegateAdapter<Void, EndUser>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EndUser enduser) {
                assertEquals(TEST_ROLE.getId(), enduser.getGlobalRole());
                getSignal.countDown();
            }
        }, TEST_MAIL);
        assertTrue(getSignal.await(30, TimeUnit.SECONDS));
    }

    @SmallTest
    public void testListGlobalAdmins() throws InterruptedException {
        final CountDownLatch assignSignal = new CountDownLatch(1);
        RoleController.assignGlobalRole(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid) {
                assignSignal.countDown();
            }
        }, TEST_MAIL, TEST_ROLE);
        assertTrue(assignSignal.await(30, TimeUnit.SECONDS));

        final List<String> adminMails = Arrays.asList(TEST_MAIL, MY_MAIL);
        final CountDownLatch listSignal = new CountDownLatch(1);
        RoleController.listGlobalAdmins(new ExtendedTaskDelegateAdapter<Void, List<EndUser>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<EndUser> admins){
                List<String> adminMailsRetrieved = new ArrayList<String>();
                for(EndUser admin : admins){
                    adminMailsRetrieved.add(admin.getEmail());
                }
                assertEquals(adminMails.size(), adminMailsRetrieved.size());
                assertTrue(adminMailsRetrieved.containsAll(adminMails)
                        && adminMails.containsAll(adminMailsRetrieved));
                listSignal.countDown();
            }
        });
        assertTrue(listSignal.await(30, TimeUnit.SECONDS));
    }


}