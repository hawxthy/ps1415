package ws1415.ps1415.controller;

import android.test.suitebuilder.annotation.SmallTest;

import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.UserListData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.ps1415.model.GlobalRole;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.controller.RoleController;
import ws1415.ps1415.controller.UserController;

/**
 * Dient dazu Funktionalitäten zu testen, die mit globalen Rollen zu tun haben.
 *
 * Folgende E-Mail Accounts müssen auf dem Gerät registriert sein um die Tests auszuführen:
 * - {@code ADMIN_MAIL}
 * - {@code TEST_MAIL}
 * Dazu müssen diese in den Einstellungen des Gerätes mit dem Passwort: "skatenight123" hinzugefügt
 * werden.
 *
 * @author Martin Wrodarczyk
 */
public class RoleControllerTest extends AuthenticatedAndroidTestCase {
    public static final String ADMIN_MAIL = "skatenight.host@gmail.com";
    public static final String TEST_MAIL = "skatenight.user1@gmail.com";
    public static final List<String> TEST_MAILS = Arrays.asList(ADMIN_MAIL, TEST_MAIL);

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
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testAssignGlobalRole() throws InterruptedException {
        final CountDownLatch assignSignal = new CountDownLatch(1);
        RoleController.assignGlobalRole(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void avoid) {
                assignSignal.countDown();
            }
        }, TEST_MAIL, TEST_ROLE);
        assertTrue(assignSignal.await(30, TimeUnit.SECONDS));

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
     * Prüft, ob die im Test genutzten Administratoren richtig abgerufen werden.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
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

        final CountDownLatch listSignal = new CountDownLatch(1);
        RoleController.listGlobalAdmins(new ExtendedTaskDelegateAdapter<Void, List<UserListData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserListData> admins){
                List<String> adminMailsServer = new ArrayList<String>();
                for(UserListData admin : admins){
                    adminMailsServer.add(admin.getEmail());
                }
                assertTrue(adminMailsServer.containsAll(TEST_MAILS));
                listSignal.countDown();
            }
        });
        assertTrue(listSignal.await(30, TimeUnit.SECONDS));
    }
}
