package ws1415.common.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Diese Klasse wird dazu genutzt die Funktionalitäten des RoleControllers zu testen.
 *
 * @author Martin Wrodarczyk
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, emulateSdk = 18)
public class RoleControllerUnitTest {
    public static final String TEST_MAIL = "test@gmail.com";

    @Test
    // TODO: Veranstaltung braucht Domain, damit über einen Key die Rolle zurückgegeben kann
    public void testSetAndGetUserRole() throws InterruptedException {
//        UserController.createUser(null, TEST_MAIL);
//
//        Thread.sleep(3000);
//
//        Domain domain = new Domain();
//        List<Integer> roles = new ArrayList<>();
//        roles.add(Role.ADMIN.getId());
//        roles.add(Role.HOST.getId());
//        domain.setPossibleRoles(roles);
//
//        RoleController.setUserRole(null, TEST_MAIL, domain, Role.ADMIN);
    }
}
