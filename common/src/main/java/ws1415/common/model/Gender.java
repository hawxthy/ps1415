package ws1415.common.model;

/**
 * Enum-Klasse für das Geschlecht für die Benutzer.
 *
 * @author Martin Wrodarczyk
 */
public enum Gender {
    MALE(1),
    FEMALE(2),
    NA(3);

    private int id;

    private Gender(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getRepresentation() {
        switch (id) {
            case 1:
                return "Männlich";
            case 2:
                return "Weiblich";
            case 3:
                return "N.a";
            default:
                return "";
        }
    }

    public static Gender getValue(Integer id) {
        for (Gender g : Gender.values()) {
            if (g.getId() == id)
                return g;
        }
        throw new IllegalArgumentException("Ungültige Id");
    }
}
