package ws1415.veranstalterapp.util;

import android.content.Context;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;

import java.util.List;

import ws1415.veranstalterapp.R;

/**
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
    public static EventUtils getInstance(){
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
    public void addDynamicField(String title, TYPE type, Event event, int pos){
        Field tmpField = new Field();
        tmpField.setType(type.name());
        tmpField.setTitle(title);
        event.getDynamicFields().add(pos, tmpField);
    }

    /**
     * Enum um die Felder zu spezifizieren.
     */
    public enum TYPE{
        SIMPLETEXT, FEE, DATE, TIME, ROUTE, PICTURE, LINK, TITLE, LOCATION, DESCRIPTION
    }

    public void setStandardFields(Event event){
        event.getDynamicFields().clear();
        addDynamicField(context.getResources().getString(R.string.announce_info_title), EventUtils.TYPE.TITLE, event, 0);
        addDynamicField(context.getResources().getString(R.string.announce_info_fee), EventUtils.TYPE.FEE, event, 1);
        addDynamicField(context.getResources().getString(R.string.announce_info_date), EventUtils.TYPE.DATE, event, 2);
        addDynamicField(context.getResources().getString(R.string.announce_info_set_time), EventUtils.TYPE.TIME, event, 3);
        addDynamicField(context.getResources().getString(R.string.announce_info_location), EventUtils.TYPE.LOCATION, event, 4);
        addDynamicField(context.getResources().getString(R.string.announce_info_choose_map), EventUtils.TYPE.ROUTE, event, 5);
        addDynamicField(context.getResources().getString(R.string.announce_info_description), EventUtils.TYPE.DESCRIPTION, event, 6);
    }

    public void setEventInfo(Event event, AnnounceCursorAdapter adapter){
        List<Field> tmpList = event.getDynamicFields();
        for(int i = 0; i < tmpList.size(); i++){
            if(tmpList.get(i).getType().equals(TYPE.SIMPLETEXT.name())){
                // mach was
            }
        }
    }
}
