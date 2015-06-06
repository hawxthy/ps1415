package ws1415.ps1415.controller;

import android.test.suitebuilder.annotation.SmallTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.model.GlobalRole;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

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

    public static final GlobalRole TEST_ADMIN_ROLE = GlobalRole.ADMIN;

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
    public void testAssignAdminRole() throws InterruptedException, IOException {
        final CountDownLatch assignSignal = new CountDownLatch(1);
        RoleController.assignGlobalRole(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                assertTrue(aBoolean);
                assignSignal.countDown();
            }
        }, TEST_MAIL, TEST_ADMIN_ROLE);
        assertTrue(assignSignal.await(30, TimeUnit.SECONDS));

        boolean isAdmin = ServiceProvider.getService().roleEndpoint().isAdmin(TEST_MAIL).execute().getValue();
        assertTrue(isAdmin);
    }

    /**
     * Prüft, ob die im Test genutzten Administratoren richtig abgerufen werden.
     *
     * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wird
     */
    @SmallTest
    public void testListGlobalAdmins() throws InterruptedException {
        final CountDownLatch assignSignal = new CountDownLatch(1);
        RoleController.assignGlobalRole(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                assertTrue(aBoolean);
                assignSignal.countDown();
            }
        }, TEST_MAIL, TEST_ADMIN_ROLE);
        assertTrue(assignSignal.await(30, TimeUnit.SECONDS));

        final CountDownLatch listSignal = new CountDownLatch(1);
        RoleController.listGlobalAdmins(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> admins){
                assertTrue(admins.containsAll(TEST_MAILS));
                listSignal.countDown();
            }
        });
        assertTrue(listSignal.await(30, TimeUnit.SECONDS));
    }
}
