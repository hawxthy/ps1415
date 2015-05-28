package ws1415.SkatenightBackend.transport;

/**
 * Repräsentiert die Daten eines Filters zum Abrufen von Events.
 * @author Richard Schulze
 */
public class EventFilter {
    /**
     * Textuelle Repräsentation des Cursor
     */
    private String cursorString;
    /**
     * Die maximale Anzahl Events, die abgerufen werden sollen.
     */
    private int limit;
    /**
     * Die ID des Benutzers (E-Mail) für den die Events abgerufen werden sollen.
     */
    private String userId;

    public String getCursorString() {
        return cursorString;
    }

    public void setCursorString(String cursorString) {
        this.cursorString = cursorString;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
