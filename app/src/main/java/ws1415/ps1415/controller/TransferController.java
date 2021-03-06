package ws1415.ps1415.controller;


import java.io.IOException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.model.Message;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;

/**
 * Der TransferController steuert die Übertragung von Nachrichten.
 *
 * @author Martin Wrodarczyk
 */
public abstract class TransferController {
    /**
     * Sendet eine Nachricht an einen Benutzer.
     *
     * @param handler  Auszuführender Task
     * @param receiver E-Mail Adresse des Empfängers
     * @param mes      Nachricht
     */
    public static void sendMessage(ExtendedTaskDelegate<Void, Boolean> handler, final String receiver,
                                   final Message mes) {
        final Long messageId = mes.get_id();
        final Long sendDate = mes.getSendDate().getTime();
        final String content = mes.getContent();
        new ExtendedTask<Void, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().transferEndpoint().sendMessage(content, messageId,
                            receiver, sendDate).execute().getValue();
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
     * @param handler   Auszuführender Task
     * @param receiver  E-Mail Adresse des Empfängers
     * @param messageId Lokale Nachrichten Id der ursprünglichen Nachricht
     * @param sendDate  Sendedatum der ursprünglichen Nachricht
     */
    public static void sendConfirmation(ExtendedTaskDelegate<Void, Boolean> handler, final String receiver,
                                        final long messageId, final Long sendDate) {
        new ExtendedTask<Void, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().transferEndpoint().sendConfirmation(messageId,
                            receiver, sendDate).execute().getValue();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Nachricht konnte nicht bestätigt werden");
                    return null;
                }
            }
        }.execute();
    }
}
