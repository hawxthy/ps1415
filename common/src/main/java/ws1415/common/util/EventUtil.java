package ws1415.common.util;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Richard
 */
public class EventUtil {
    /**
     * Verbindet die beiden Datumsangaben des Events zu einem.
     * @return Das zusammengefügte Date.
     */
    public Date getFusedDate(Event event) {
        Date time = new Date(Long.parseLong(getUniqueField(4, event).getValue()));
        Date date = new Date(Long.parseLong(getUniqueField(3, event).getValue()));
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
    public Field getUniqueField(int type, Event event) {
        for (int i = 0; i < event.getDynamicFields().size(); i++) {
            if (event.getDynamicFields().get(i).getType() == type) {
                return event.getDynamicFields().get(i);
            }
        }
        return null;
    }
}
