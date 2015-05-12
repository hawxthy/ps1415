package ws1415.SkatenightBackend.model;


/**
 * Enum-Klasse um die Sichtbarkeit der allgemeinen Informationen eines Benutzers zu vereinfachen.
 */
public enum Visibility {
    PUBLIC(1),
    ONLY_FRIENDS(2),
    ONLY_ME(3);

    private Integer id;

    private Visibility(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getRepresentation(){
        switch(id){
            case 1:
                return "Öffentlich";
            case 2:
                return "Nur Freunde";
            case 3:
                return "Nur ich";
            default:
                return null;
        }
    }

    public static Visibility getValue(Integer i){
        for (Visibility v : Visibility.values()){
            if (v.getId().equals(i))
                return v;
        }
        throw new IllegalArgumentException("not a valid value for the visibility");
    }
}
