package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
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
import ws1415.SkatenightBackend.model.BooleanWrapper;

/**
 * Der TransferEndpoint stellt Hilfsmethoden bereit, die genutzt werden um den Nachrichtenfluss zwischen
 * den Benutzern zu verwalten.
 *
 * @author Martin Wrodarczyk
 */
public class TransferEndpoint extends SkatenightServerEndpoint {

    /**
     * Sendet über GCM eine Nachricht an den Benutzer mit der angegebenen Email.
     *
     * @param user       Sender der Nachricht
     * @param receiver   Empfänger der Nachricht
     * @param messageId  Id der lokalen Nachricht des Senders
     * @param sendDate   Sendedatum der Nachricht
     * @param content    Inhalt der Nachricht
     * @return true, falls Übertragung erfolgreich, false andernfalls
     * @throws IOException
     * @throws OAuthRequestException
     */
    @ApiMethod(path = "local_message_send")
    public BooleanWrapper sendMessage(User user, @Named("receiver") String receiver,
                                      @Named("messageId") Long messageId, @Named("sendDate") Long sendDate,
                                      @Named("content") String content) throws IOException, OAuthRequestException {
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
                .addData("receiver", receiver)
                .addData("sendDate", Long.toString(sendDate))
                .addData("content", content)
                .build();
        try {
            GCMSender.send(m, id, 3);
        } catch (Exception e) {
            return new BooleanWrapper(false);
        } finally {
            pm.close();
        }
        return new BooleanWrapper(true);
    }


    /**
     * Sendet über GCM eine Bestätigung des Erhaltes einer Nachricht.
     *
     * @param user      User zur Authentifizierung
     * @param receiver  Empfänger der Bestätigung
     * @param messageId Lokale Nachrichten Id des ursprünglichen Senders
     * @param sendDate  Sendedatum der ursprünglichen Nachricht
     * @throws OAuthRequestException
     * @throws IOException
     */
    @ApiMethod(path = "local_message_confirmation")
    public BooleanWrapper sendConfirmation(User user, @Named("receiver") String receiver,
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
                .addData("receiver", receiver)
                .addData("sendDate", Long.toString(sendDate))
                .build();

        try {
            GCMSender.send(m, id, 3);
        } catch (Exception e) {
            return new BooleanWrapper(false);
        } finally {
            pm.close();
        }
        return new BooleanWrapper(true);
    }
}
