package ws1415.common.controller;

import java.io.IOException;

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
     * Sendet eine Nachricht an einen Benutzer.
     *
     * @param handler
     * @param sender E-Mail Adresse des Senders der Nachricht
     * @param receiver E-Mail Adresse des Empfängers der Nachricht
     * @param message Nachricht
     */
    public static void sendMessage(ExtendedTaskDelegate handler, final String sender, final String receiver,final String message) {
        new ExtendedTask<Void, Void, Void>(handler){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().messageEndpoint().sendMessage(sender, receiver, message).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzerdaten konnten nicht geändert werden");
                    return null;
                }
            }
        }.execute();
    }
}
