package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;

import java.io.IOException;

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
     * @param sender E-Mail Adresse des Senders
     * @param receiver E-Mail Adresse des Empfängers
     * @param message Nachricht an den Empfänger
     * @throws IOException
     */
    public void sendMessage(@Named("sender") String sender, @Named("receiver") String receiver,
                            @Named("message") String message) throws IOException {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        RegistrationManager registrationManager = getRegistrationManager(pm);

        String id = registrationManager.getUserIdByMail(receiver);

        Sender GCMSender = new Sender(Constants.GCM_API_KEY);

        Message m = new Message.Builder()
                .delayWhileIdle(false)
                .timeToLive(3600)
                .addData("sender", sender)
                .addData("message", message)
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
