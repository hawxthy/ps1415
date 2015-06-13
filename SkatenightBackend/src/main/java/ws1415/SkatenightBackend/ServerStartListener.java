package ws1415.SkatenightBackend;


import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import ws1415.SkatenightBackend.model.EndUser;
import ws1415.SkatenightBackend.model.GlobalRole;

public class ServerStartListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        for(String admin: Constants.INITIAL_ADMINS){
            PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
                    "transactions-optional").getPersistenceManager();
            new UserEndpoint().createUser(admin, "Skatenight Administrator", "");
            try {
                EndUser user = pm.getObjectById(EndUser.class, admin);
                user.setGlobalRole(GlobalRole.ADMIN);
            } finally {
                pm.close();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JDOHelper.getPersistenceManagerFactory().close();
        Logger.getLogger(ServerStartListener.class.getName()).info("Context destroyed");
    }

}
