package ws1415.common.controller;


import java.io.IOException;

import ws1415.common.model.Message;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;

/**
 * Der MessageController steuert die Verwaltung der Nachrichten.
 *
 * @author Martin Wrodarczyk
 */
public class MessageController {
    /**
     * Keine Instanziierung ermöglichen.
     */
    private MessageController() {
    }

    /**
     * Sendet eine Nachricht an einen Benutzer.
     *
     * @param handler
     * @param receiver E-Mail Adresse des Empfängers
     * @param m Nachricht
     */
    public static void sendMessage(ExtendedTaskDelegate handler, final String receiver,
                                   final Message m) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().messageEndpoint().sendMessage(receiver, m.get_id(), m.getContent(), m.getSendDate().getTime()).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Nachricht konnte nicht gesendet werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Sendet eine Bestätigung über den Erhalt einer Nachricht an einen Benutzer.
     *
     * @param handler
     * @param receiver E-Mail Adresse des Empfängers
     * @param messageId Nachrichten Id der ursprünglichen Nachricht
     * @param sendDate Sendedatum der ursprünglichen Nachricht
     */
    public static void sendConfirmation(ExtendedTaskDelegate handler, final String receiver, final long messageId,
                                        final Long sendDate){
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().messageEndpoint().sendConfirmation(receiver, messageId, sendDate).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Nachricht konnte nicht bestätigt werden");
                    return null;
                }
            }
        }.execute();

    }


}
