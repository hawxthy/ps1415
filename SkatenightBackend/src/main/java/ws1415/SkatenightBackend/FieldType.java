package ws1415.SkatenightBackend;

/**
 * Created by Bernd Eissing on 05.12.2014.
 */
public enum FieldType {
    SIMPLETEXT(1),
    FEE(2),
    DATE(3),
    TIME(4),
    ROUTE(5),
    PICTURE(6),
    LINK(7),
    TITLE(8),
    LOCATION(9),
    DESCRIPTION(10);


    private int id;

    private FieldType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
