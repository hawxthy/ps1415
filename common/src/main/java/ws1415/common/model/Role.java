package ws1415.common.model;

/**
 * Diese Enum-Klasse verwaltet die Rollen, die ein Benutzer innerhalb verschiedener
 * Umgebungen annehmen kann.
 *
 * @author Martin Wrodarczyk
 */
public enum Role {
    USER(1),
    ADMIN(2),
    HOST(10),
    GUARD(11),
    MEDIC(12),
    PARTICIPANT(13);

    private int id;

    private Role(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public String getRepresentation(){
        switch(id){
            case 1:
                return "Benutzer";
            case 2:
                return "Administrator";
            case 10:
                return "Veranstalter";
            case 11:
                return "Ordner";
            case 12:
                return "Sanitäter";
            case 13:
                return "Teilnehmer";
            default:
                return null;
        }
    }

    public static Role getValue(int i)
    {
        for (Role r : Role.values()) {
            if (r.getId() == i)
                return r;
        }
        throw new IllegalArgumentException("Keine gültige Id für die Rolle");
    }
}
