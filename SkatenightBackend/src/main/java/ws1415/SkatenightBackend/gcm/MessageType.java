package ws1415.SkatenightBackend.gcm;

/**
 * EIne Auflistung aller Message-Types, die per GCM an die App gesendet werden können.
 * @author Richard
 */
public enum MessageType {
    /**
     * Eine Nachricht, die als Notification angezeigt werden soll.
     * DIe GCM-Nachricht muss die Nachricht als Attribut "content" enthalten.
     */
    NOTIFICATION_MESSAGE,
    /**
     * Wird an alle Teilnehmer eines Events gesendet, wenn das Event beginnt.
     */
    EVENT_START_MESSAGE;
}
