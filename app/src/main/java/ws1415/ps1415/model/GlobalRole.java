package ws1415.ps1415.model;

/**
 * Diese Enum-Klasse verwaltet die Rollen, die ein Benutzer global in der Anwendung annehmen
 * kann.
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

    public static GlobalRole getValue(Integer i) {
        for (GlobalRole r : GlobalRole.values()) {
            if (r.getId().equals(i))
                return r;
        }
        throw new IllegalArgumentException("Keine gültige Id für die Rolle");
    }
}
