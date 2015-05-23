package ws1415.SkatenightBackend;


import com.google.appengine.api.datastore.Text;

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
            new UserEndpoint().createUser(admin, "", "", new Text(null));
            try {
                EndUser user = pm.getObjectById(EndUser.class, admin);
                user.setGlobalRole(GlobalRole.ADMIN.getId());
            } finally {
                pm.close();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // shutdown code here
    }

}
