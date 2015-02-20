package ws1415.SkatenightBackend;

import java.util.Calendar;
import java.util.Date;

/**
 * Enum Klasse für die verschiedenen Typen an dynamischen Feldern eines
 * Events.
 *
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

    // ---------- Hilfsmethoden für den Umgang mit den dynamischen Eigenschaften ---------

    /**
     * Methode zum finden von Id's von eindeutigen Feldern.
     *
     * @param type der Typ des eindeutigen Feldes
     * @return Die Id, -1 falls keins gefunden wurde
     */
    public static int getUniqueFieldId(FieldType type, Event e){
        for(int i = 0; i < e.getDynamicFields().size(); i++){
            if(e.getDynamicFields().get(i).getType() == type.getId()){
                return i;
            }
        }
        return -1;
    }

    /**
     * Verbindet die beiden Datumsangaben des Events zu einem.
     * @return Das zusammengefügte Date.
     */
    public static Date getFusedDate(Event e) {
        Date time = new Date(Long.parseLong(getUniqueField(FieldType.TIME.getId(), e).getValue()));
        Date date = new Date(Long.parseLong(getUniqueField(FieldType.DATE.getId(), e).getValue()));
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(time);
        cal1.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
        cal1.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
        return cal1.getTime();
    }

    /**
     * Methode zum finden von eindeutigen Feldern
     *
     * @param type der Typ des eindeutigen Feldes
     * @return Das Feld, null falls keins gefunden wurde
     */
    public static Field getUniqueField(int type, Event e) {
        for (int i = 0; i < e.getDynamicFields().size(); i++) {
            if (e.getDynamicFields().get(i).getType() == type) {
                return e.getDynamicFields().get(i);
            }
        }
        return null;
    }
}
