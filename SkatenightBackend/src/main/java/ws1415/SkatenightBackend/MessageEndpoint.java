package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.io.IOException;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.gcm.Message;
import ws1415.SkatenightBackend.gcm.MessageType;
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
     * @param user      User zur Authentifizierung
     * @param receiver  Empfänger der Nachricht
     * @param messageId Lokale Nachrichten Id des Senders zur Bestätigung
     * @param content   Inhalt der Nachricht
     * @param sendDate  Sendedatum der Nachricht
     * @throws IOException
     * @throws OAuthRequestException
     */
    public void sendMessage(User user, @Named("receiver") String receiver, @Named("messageId") Long messageId,
                            @Named("content") String content, @Named("sendDate") Long sendDate) throws IOException, OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!new UserEndpoint().existsUser(user.getEmail()).value) {
            throw new JDOObjectNotFoundException("Kein gültiger Benutzer");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        RegistrationManager registrationManager = getRegistrationManager(pm);

        String id = registrationManager.getUserIdByMail(receiver);

        Sender GCMSender = new Sender(Constants.GCM_API_KEY);

        Message m = new Message.Builder()
                .delayWhileIdle(false)
                .timeToLive(2419200)
                .addData("type", MessageType.USER_NEW_MESSAGE.name())
                .addData("messageId", Long.toString(messageId))
                .addData("sender", user.getEmail())
                .addData("sendDate", Long.toString(sendDate))
                .addData("content", content)
                .addData("title", "Skatenight-App")
                .build();
        try {
            GCMSender.send(m, id, 3);
        } catch (IOException e) {
            throw new IOException("Nachricht konnte nicht gesendet werden");
        }
        if (pm != null) pm.close();
    }


    /**
     * Sendet über GCM eine Bestätigung des Erhaltes einer Nachricht.
     *
     * @param user User zur Authentifizierung
     * @param receiver Empfänger der Bestätigung
     * @param messageId Lokale Nachrichten Id des ursprünglichen Senders
     * @param sendDate Sendedatum der ursprünglichen Nachricht
     *
     * @throws OAuthRequestException
     * @throws IOException
     */
    public void sendConfirmation(User user, @Named("receiver") String receiver,
                                 @Named("messageId") Long messageId, @Named("sendDate") Long sendDate) throws OAuthRequestException, IOException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!new UserEndpoint().existsUser(user.getEmail()).value) {
            throw new JDOObjectNotFoundException("Kein gültiger Benutzer");
        }
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        RegistrationManager registrationManager = getRegistrationManager(pm);

        String id = registrationManager.getUserIdByMail(receiver);

        Sender GCMSender = new Sender(Constants.GCM_API_KEY);

        Message m = new Message.Builder()
                .delayWhileIdle(false)
                .timeToLive(2419200)
                .addData("type", MessageType.USER_CONFIRMATION_MESSAGE.name())
                .addData("messageId", Long.toString(messageId))
                .addData("sender", user.getEmail())
                .addData("sendDate", Long.toString(sendDate))
                .build();
        try {
            GCMSender.send(m, id, 3);
        } catch (IOException e) {
            throw new IOException("Nachricht konnte nicht gesendet werden");
        }
        if (pm != null) pm.close();
    }
}
