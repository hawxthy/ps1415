package ws1415.veranstalterapp.util;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;
import com.skatenight.skatenightAPI.model.Route;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ws1415.veranstalterapp.Adapter.AnnounceCursorAdapter;
import ws1415.veranstalterapp.R;

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
        tmpField.setValue("");
        event.getDynamicFields().add(pos, tmpField);
    }

    /**
     * Löscht das Feld aus dem Event mit der angegebenen Position.
     *
     * @param event Event
     * @param pos Position in der Liste der Felder
     */
    public void deleteDynamicField(Event event, int pos){
        event.getDynamicFields().remove(pos);
    }

    /**
     * Setzt die Standard Event Informationen, diese umfassen Titel, Fee, Datum, Uhrzeit, Location,
     * Route und Description.
     *
     * @param event Event
     */
    public void setStandardFields(Event event){
        if (event.getDynamicFields() == null) {
            event.setDynamicFields(new ArrayList<Field>());
        } else {
            event.getDynamicFields().clear();
        }
        addDynamicField(context.getResources().getString(R.string.announce_info_title), FieldType.TITLE, event, 0);
        addDynamicField(context.getResources().getString(R.string.announce_info_fee), FieldType.FEE, event, 1);
        addDynamicField(context.getResources().getString(R.string.announce_info_date), FieldType.DATE, event, 2);
        addDynamicField(context.getResources().getString(R.string.announce_info_time), FieldType.TIME, event, 3);
        addDynamicField(context.getResources().getString(R.string.announce_info_location), FieldType.LOCATION, event, 4);
        addDynamicField(context.getResources().getString(R.string.announce_info_map), FieldType.ROUTE, event, 5);
        addDynamicField(context.getResources().getString(R.string.announce_info_description), FieldType.DESCRIPTION, event, 6);
    }

    /**
     * Geht die Liste von Fields von Event durch und setzt die Informationen, die in den EditText Elementen
     * stehen auf die value von den Field objekten.
     *
     * @param event Event
     * @param list Liste von Fields von Event
     */
    public void setEventInfo(Event event, ListView list){
        List<Field> tmpList = event.getDynamicFields();
        for(int i = 0; i < tmpList.size(); i++){
            if(tmpList.get(i).getType() == FieldType.TITLE.getId() ||
               tmpList.get(i).getType() == FieldType.LOCATION.getId() ||
               tmpList.get(i).getType() == FieldType.DESCRIPTION.getId()){
                EditText editText = (EditText) list.getChildAt(i).findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
                    tmpList.get(i).setValue(editText.getText().toString());
            }else if(tmpList.get(i).getType() == FieldType.FEE.getId()){
                EditText editText = (EditText) list.getChildAt(i).findViewById(R.id.list_view_item_announce_information_fee_editText);
                tmpList.get(i).setValue(editText.getText().toString());
            }else if(tmpList.get(i).getType() == FieldType.PICTURE.getId()){
                // Mach was Richard
            }else if(tmpList.get(i).getType() == FieldType.LINK.getId()){
                EditText editText = (EditText) list.getChildAt(i).findViewById(R.id.list_view_item_announce_information_simpletext_editText);
                tmpList.get(i).setValue(editText.getText().toString());
            }else if(tmpList.get(i).getType() == FieldType.DATE.getId()){
                tmpList.get(i).setValue(Long.toString(((AnnounceCursorAdapter) list.getAdapter()).getDate().getTime()));
            }else if(tmpList.get(i).getType() == FieldType.TIME.getId()){
                tmpList.get(i).setValue(Long.toString(((AnnounceCursorAdapter) list.getAdapter()).getDate().getTime()));
            }else if(tmpList.get(i).getType() == FieldType.SIMPLETEXT.getId()){
                EditText editText = (EditText) list.getChildAt(i).findViewById(R.id.list_view_item_announce_information_simpletext_editText);
                tmpList.get(i).setValue(editText.getText().toString());
            }else if(tmpList.get(i).getType() == FieldType.ROUTE.getId()){
                //tmpList.get(i).setValue(((AnnounceCursorAdapter)list.getAdapter()).getRoute());
                event.setRoute(((AnnounceCursorAdapter)list.getAdapter()).getRoute());
            }
        }
    }

    /**
     * Wird vom AnnounceCursorAdapter aufgerufen, um die angegebene Route dem Event zuzuordnen.
     *
     * @param event Event
     * @param route Route
     */
    public void setRouteToEvent(Event event, Route route){
        event.setRoute(route);
    }

    /**
     * Wird vom AnnounceCursorAdapter aufgerufen, um das angegebene Datum dem Event zuzuordnen.
     *
     * @param event Event
     * @param date Datum
     */
    public void setDateToEvent(Event event, Date date){
        event.setDate(new DateTime(date));
    }

    /**
     * Wird vom AnnounceCursorAdapter aufgerufen, um die angegebene Uhrzeit dem EVent zuzuordnen.
     *
     * @param event Event
     * @param date Uhrzeit
     */
    public void setTimeToEvent(Event event, Date date){
        event.setTime(new DateTime(date));
    }

    /**
     * Holt sich die Uhrzeit und das Datum vom Event und vereinigt diese zu einem Date
     *
     * @param event Event
     * @return Das vereinigte Date, umfasst Datum und Uhrzeit
     */
    public Date getFusedDate(Event event) {
        Date time = new Date(event.getDate().getValue());
        Date date = new Date(event.getTime().getValue());
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(time);
        cal1.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
        cal1.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
        return cal1.getTime();
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
}
