package ws1415.ps1415.model;

/**
 * Das MessageType-Enum dient zum einfachen Unterscheiden von Nachrichten.
 */
public enum MessageType {
    OUTGOING(1),
    INCOMING(2);

    private int id;

    MessageType(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public static MessageType getValue(int id) {
        for (MessageType type : MessageType.values()) {
            if (type.getId() == id)
                return type;
        }
        throw new IllegalArgumentException("Ungültige Id");
    }
}
