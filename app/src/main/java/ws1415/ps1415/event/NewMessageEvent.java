package ws1415.ps1415.event;

/**
 * Dieses Event wird an ConversationActivies gesendet, damit diese bei neuen Nachrichten ihre
 * Informationen updaten.
 *
 * @author Martin Wrodarczyk
 */
public class NewMessageEvent {
    private String userMail;

    public NewMessageEvent(String userMail) {
        this.userMail = userMail;
    }

    public String getUserMail() {
        return userMail;
    }
}
