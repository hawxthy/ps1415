package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.Host;

/**
 * Created by Richard on 01.05.2015.
 */
public class HostEndpoint extends SkatenightServerEndpoint {

    /**
     * Fügt die angegebene Mail-Adresse als Veranstalter hinzu.
     * @param mail Die hinzuzufügende Mail-Adresse
     */
    public void addHost(User user, @Named("mail") String mail) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Query q = pm.newQuery(Host.class);
            q.setFilter("email == emailParam");
            q.declareParameters("String emailParam");
            List<Host> results = (List<Host>) q.execute(mail);
            if (results.isEmpty()) {
                Host h = new Host();
                h.setEmail(mail);
                pm.makePersistent(h);
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Entfernt die angegebene Mail-Adresse aus den Veranstaltern.
     * @param mail Die zu entfernende Mail-Adresse
     */
    public void removeHost(User user, @Named("mail") String mail) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Query q = pm.newQuery(Host.class);
            q.setFilter("email == emailParam");
            q.declareParameters("String emailParam");
            List<Host> results = (List<Host>) q.execute(mail);
            if (!results.isEmpty()) {
                pm.deletePersistentAll(results);
            }
        } finally {
            pm.close();
        }
    }

    /**
     * Prüft, ob die angegebene Mail-Adresse zu einem authorisierten Veranstalter-Account gehört.
     *
     * @param mail Die zu prüfende Mail
     * @return true, wenn der Account ein authorisierter Veranstalter ist, false sonst
     */
    public BooleanWrapper isHost(@Named("mail") String mail) {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        try {
            Query q = pm.newQuery(Host.class);
            q.setFilter("email == emailParam");
            q.declareParameters("String emailParam");
            List<Host> results = (List<Host>) q.execute(mail);
            return new BooleanWrapper(!results.isEmpty());
        } finally {
            pm.close();
        }
    }

    /**
     * Gibt die Liste aller registrierten Veranstalter zurück.
     * @param user Der Benutzer, der die Liste anfordert. Er muss bereits als Veranstalter eingetra-
     *             gen sein.
     * @return Eine Liste aller Veranstalter.
     * @throws OAuthRequestException
     */
    public List<Host> getHosts(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!isHost(user.getEmail()).value) {
            throw new OAuthRequestException("user is not a host");
        }

        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        List<Host> result;
        try {
            result = (List<Host>) pm.newQuery(Host.class).execute();
        } finally {
            pm.close();
        }
        if (result == null) {
            result = new ArrayList<Host>();
        }
        return result;
    }
}
