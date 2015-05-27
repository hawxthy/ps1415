package ws1415.common.model;

/**
 * Das LocalMessageType-Enum dient zum einfachen Unterscheiden von Nachrichten.
 */
public enum LocalMessageType {
    OUTGOING_NOT_RECEIVED(0),
    OUTGOING_RECEIVED(1),
    INCOMING(2);

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
