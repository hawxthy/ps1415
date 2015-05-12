package ws1415.SkatenightBackend;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.GlobalRole;

public class ServerStartListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        new UserEndpoint().createUser(Constants.FIRST_ADMIN);
        PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
                "transactions-optional").getPersistenceManager();
        try {
            EndUser user = pm.getObjectById(EndUser.class, Constants.FIRST_ADMIN);
            user.setGlobalRole(GlobalRole.ADMIN.getId());
        } finally {
            pm.close();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // shutdown code here
    }

}
