package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.io.IOException;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.gcm.Message;
import ws1415.SkatenightBackend.gcm.RegistrationManager;
import ws1415.SkatenightBackend.gcm.Sender;

/**
 * Der MessageEndpoint stellt Hilfsmethoden bereit, die genutzt werden um den Nachrichtenfluss zwischen
 * den Benutzern zu verwalten.
 *
 * @author Martin Wrodarczyk
 */
public class MessageEndpoint extends SkatenightServerEndpoint {

    /**
     * Sendet über GCM eine Nachricht an den Benutzer mit der angegebenen Email.
     *
     * @param receiver E-Mail Adresse des Empfängers
     * @param message Nachricht an den Empfänger
     * @throws IOException
     */
    public void sendMessage(User user, @Named("receiver") String receiver,
                            @Named("message") String message, @Named("sendDate") Long sendDate) throws IOException, OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if(!new UserEndpoint().existsUser(user.getEmail()).value){
            throw new JDOObjectNotFoundException("Kein gültiger Benutzer");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        RegistrationManager registrationManager = getRegistrationManager(pm);

        String id = registrationManager.getUserIdByMail(receiver);

        Sender GCMSender = new Sender(Constants.GCM_API_KEY);

        Message m = new Message.Builder()
                .delayWhileIdle(false)
                .timeToLive(2419200)
                .addData("sender", user.getEmail())
                .addData("message", message)
                .addData("sendDate", Long.toString(sendDate))
                .build();
        try {
            // Versucht, Nachricht ungefähr 18 Stunden lang zu senden
            GCMSender.send(m, id, 26);
        }
        catch (IOException e) {
            throw new IOException("Nachricht konnte nicht gesendet werden");
        }
        if (pm != null) pm.close();

    }
}
