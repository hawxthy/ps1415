package ws1415.ps1415.model;

/**
 * Das MessageType-Enum dient zum einfachen Unterscheiden von Nachrichten.
 */
public enum LocalMessageType {
    OUTGOING_NOT_RECEIVED(1),
    OUTGOING_RECEIVED(2),
    INCOMING(3);

    private int id;

    LocalMessageType(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public static LocalMessageType getValue(int id) {
        for (LocalMessageType type : LocalMessageType.values()) {
            if (type.getId() == id)
                return type;
        }
        throw new IllegalArgumentException("Ungültige Id");
    }
}
