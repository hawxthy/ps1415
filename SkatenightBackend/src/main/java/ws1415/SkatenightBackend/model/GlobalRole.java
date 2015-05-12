package ws1415.SkatenightBackend.model;

/**
 * Diese Enum-Klasse verwaltet die Rollen, die ein Benutzer innerhalb verschiedener
 * Umgebungen annehmen kann.
 *
 * @author Martin Wrodarczyk
 */
public enum GlobalRole {
    USER(1),
    ADMIN(2);

    private Integer id;

    private GlobalRole(Integer id){
        this.id = id;
    }

    public Integer getId(){
        return id;
    }

    public String getRepresentation(){
        switch(id){
            case 1:
                return "Benutzer";
            case 2:
                return "Administrator";
            default:
                return null;
        }
    }

    public static GlobalRole getValue(Integer i) {
        for (GlobalRole r : GlobalRole.values()) {
            if (r.getId().equals(i))
                return r;
        }
        throw new IllegalArgumentException("not a valid value for the role");
    }
}
