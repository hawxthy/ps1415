package ws1415.ps1415.util;

import android.content.Context;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;

import java.util.Calendar;
import java.util.Date;


/**
 * Utility Singleton Klasse für die Klasse Event. Unterstützende Klasse um dynamische Felder für
 * zu erstellende oder zu editierende Events hinzuzufügen oder zu löschen.
 *
 * Created by Bernd Eissing on 28.11.2014.
 */
public class EventUtils {
    private static EventUtils instance;
    private static Context context;
    private EventUtils(Context context){
        this.context = context;
    }

    /**
     * Singleton Klasse EventUtils, gibt die Instanz dieser Klasse zurück.
     *
     * @return Instanz der EventUtils Klasse
     */
    public static EventUtils getInstance(Context context){
        if(instance == null){
            instance = new EventUtils(context);
        }
        return instance;
    }
    /**
     * Fügt ein Field der ListView in AnnounceInformationFragment mit leerem Inhalt hinzu.
     *
     * @param title Titel des DateFields
     * @param type Der Type des Feldes
     */
    public void addDynamicField(String title, FieldType type, Event event, int pos){
        Field tmpField = new Field();
        tmpField.setType(type.getId());
        tmpField.setTitle(title);
        event.getDynamicFields().add(pos, tmpField);
    }

    /**
     * Methode zum finden von Id's von eindeutigen Feldern.
     *
     * @param type der Typ des eindeutigen Feldes
     * @return Die Id, -1 falls keins gefunden wurde
     */
    public int getUniqueFieldId(FieldType type, Event event){
        for(int i = 0; i < event.getDynamicFields().size(); i++){
            if(event.getDynamicFields().get(i).getType() == type.getId()){
                return i;
            }
        }
        return -1;
    }

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
