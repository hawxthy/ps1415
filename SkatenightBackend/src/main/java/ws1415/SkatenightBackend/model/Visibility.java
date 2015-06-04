package ws1415.SkatenightBackend.model;


/**
 * Enum-Klasse für die Sichtbarkeit der allgemeinen Informationen eines Benutzers.
 */
public enum Visibility {
    PUBLIC(0),
    FRIENDS(1),
    ONLY_ME(2);

    private Integer id;

    private Visibility(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public static Visibility getValue(Integer i){
        for (Visibility v : Visibility.values()){
            if (v.getId().equals(i))
                return v;
        }
        throw new IllegalArgumentException("not a valid value for the visibility");
    }
}
