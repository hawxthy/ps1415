package ws1415.SkatenightBackend.model;

/**
 * Diese Enum-Klasse verwaltet die Rollen, die ein Benutzer innerhalb verschiedener
 * Umgebungen annehmen kann.
 *
 * @author Martin Wrodarczyk
 */
public enum Role {
    USER(1),
    ADMIN(2),
    HOST(3),
    GUARD(4),
    MEDIC(5),
    PARTICIPANT(6);

    private int id;

    private Role(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public String getValue(int id){
        switch(id){
            case 1:
                return "Benutzer";
            case 2:
                return "Administrator";
            case 3:
                return "Veranstalter";
            case 4:
                return "Ordner";
            case 5:
                return "Sanitäter";
            case 6:
                return "Teilnehmer";
            default:
                return null;
        }
    }
}
