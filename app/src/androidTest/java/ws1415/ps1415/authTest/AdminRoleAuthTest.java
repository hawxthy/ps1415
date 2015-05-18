package ws1415.ps1415.authTest;

import android.test.suitebuilder.annotation.SmallTest;

import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.controller.RoleController;
import ws1415.common.controller.UserController;
import ws1415.common.model.GlobalRole;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

/**
 * Dient dazu Funktionalitäten zu testen, die mit Adminrollen zu tun haben.
 *
 * Ausführer der Tests muss eingetragener Admin auf dem Server sein. Dies ist beispielsweise
 * umsetzbar in dem man als "FIRST_ADMIN" seine E-Mail Adresse einträgt, sodass anschließend bei
 * Serverstart ein mit der angegebenen E-Mail als Administrator angelegt wird.
 *
 * ADMIN_MAIL muss mit dem zu testenden Administrator ersetzt werden.
 * TEST_MAIL muss mit einer weiteren eingetragenen E-Mail Adresse ersetzt werden.
 */
public class AdminRoleAuthTest extends AuthenticatedAndroidTestCase {
    public static String ADMIN_MAIL = "martin.wrod@googlemail.com";
    public static String TEST_MAIL = "skatenight.dev@gmail.com";

    public static final GlobalRole TEST_ROLE = GlobalRole.ADMIN;

    /**
     * Loggt den Benutzer ein und erstellt einen Benutzer zum Testen.
     *
     * @throws Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        final CountDownLatch createSignal = new CountDownLatch(1);
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean result) {
                createSignal.countDown();
            }
        }, TEST_MAIL, "", "");
        assertTrue(createSignal.await(30, TimeUnit.SECONDS));

        changeAccount(ADMIN_MAIL);
    }

    /**
     * Löscht den erstellten Benutzer aus der setUp-Methode.
     *
     * @throws Exception
     */
    @Override
    public void tearDown() throws Exception {
        changeAccount(TEST_MAIL);
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

    /**
     * Prüft, ob einem Benutzer die Rolle richtig zugewiesen wird.
     *
     * @throws InterruptedException
     */
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

    /**
     * Prüft, ob globale Administratoren richtig abgerufen werden.
     *
     * @throws InterruptedException
     */
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

        final List<String> adminMails = Arrays.asList(TEST_MAIL, ADMIN_MAIL);
        final CountDownLatch listSignal = new CountDownLatch(1);
        RoleController.listGlobalAdmins(new ExtendedTaskDelegateAdapter<Void, List<UserProfile>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserProfile> admins){
                List<String> adminMailsRetrieved = new ArrayList<String>();
                for(UserProfile admin : admins){
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
