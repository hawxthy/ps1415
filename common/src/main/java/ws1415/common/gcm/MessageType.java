package ws1415.common.gcm;

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
     * Eine normale Notification, die beim ERstellen oder Ändern eines Events gesendet wird. In der
     * User-App wird die Liste der Events aktualisiert, wenn die Nachricht empfangen wird.
     */
    EVENT_NOTIFICATION_MESSAGE,
    /**
     * Wird an alle Teilnehmer eines Events gesendet, wenn das Event beginnt.
     */
    EVENT_START_MESSAGE,
    /**
     * Wird an alle Mitglieder einer Gruppe gesendet, wenn die Gruppe gelöscht wird.
     */
    GROUP_DELETED_NOTIFICATION_MESSAGE,
    /**
     * Wird an alle Mitglieder einer Gruppe gesendet, wenn eine Gruppe erstellt wird.
     */
    GROUP_CREATED_NOTIFICATION_MESSAGE,
    /**
     * Wird an einen Benutzer gesendet, wenn dieser eine neue private Nachricht erhält.
     */
    USER_NEW_MESSAGE,
    /**
     * Wird an einen Benutzer gesendet, um den Erhalt einer Nachricht zu bestätigen.
     */
    USER_CONFIRMATION_MESSAGE;
}
