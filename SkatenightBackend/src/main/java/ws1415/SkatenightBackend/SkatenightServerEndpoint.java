package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiNamespace;

import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import ws1415.SkatenightBackend.gcm.RegistrationManager;

/**
 * Stellt Hilfsmethoden bereit, die von allen Endpoints der Skatenight-API benötigt werden.
 */
@Api(name = "skatenightAPI",
        version = "v1",
        clientIds = {Constants.ANDROID_USER_CLIENT_ID, Constants.ANDROID_HOST_CLIENT_ID,
                Constants.WEB_CLIENT_ID, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = "skatenight.com", ownerName = "skatenight"))
public abstract class SkatenightServerEndpoint {
    private PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(
            "transactions-optional");

    protected PersistenceManagerFactory getPersistenceManagerFactory() {
        return pmf;
    }

    /**
     * Ruft über den angegebenen PersistenceManager den RegistrationManager aus der Datenbank ab.
     * @param pm Der zu verwendende PersistenceManager.
     * @return Gibt den RegistrationManager zurück, der in der Datenbank gespeichert ist.
     */
    protected RegistrationManager getRegistrationManager(PersistenceManager pm) {
        RegistrationManager registrationManager;
        Query q = pm.newQuery(RegistrationManager.class);
        List<RegistrationManager> result = (List<RegistrationManager>) q.execute();
        if (!result.isEmpty()) {
            registrationManager = result.get(0);
        } else {
            registrationManager = new RegistrationManager();
        }
        return registrationManager;
    }

}
