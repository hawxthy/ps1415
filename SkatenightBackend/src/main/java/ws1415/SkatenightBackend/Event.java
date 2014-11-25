package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.datanucleus.annotations.Unowned;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Datenmodell für eine Veranstaltung.
 * Created by Richard Schulze, Bernd Eissing, Martin Wrodarczyk on 21.10.2014.
 */
@PersistenceCapable
public class Event{
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private String title;
    @Persistent
    private Date date;
    @Persistent
    private String fee;
    @Persistent
    private String location;
    @Persistent
    private Text description;
    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private Route route;

    // Dynamische ArrayListe von dynamischen Komponenten
    @Persistent
    private ArrayList<Field> dynamicFields;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getTitle() {
        return (String)getUniqueField(TYPE.TITLE).getValue();
    }

    //public void setTitle(String title) {
    //    this.title = title;
    //}

    public Date getDate() {
        Date time = (Date)getUniqueField(TYPE.TIME).getValue();
        Date date = (Date)getUniqueField(TYPE.DATE).getValue();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(time);
        cal1.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
        cal1.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
        return cal1.getTime();
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFee() {
        return (String)getUniqueField(TYPE.FEE).getValue();
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getLocation() {
        return  (String)getUniqueField(TYPE.LOCATION).getValue();
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Text getDescription() {
        return  (Text)getUniqueField(TYPE.DESCRIPTION).getValue();
    }

    public void setDescription(Text description) {
        this.description = description;
    }

    public Route getRoute() {
        return  (Route)getUniqueField(TYPE.ROUTE).getValue();
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    /**
     * Methode zum finden von eindeutigen Feldern
     *
     * @param type der Typ des eindeutigen Feldes
     * @return Das Feld, null falls keins gefunden wurde
     */
    public Field getUniqueField(TYPE type){
        for(int i = 0; i < dynamicFields.size(); i++){
            if(dynamicFields.get(i).getType() == type){
                return dynamicFields.get(i);
            }
        }
        return null;
    }

    /**
     * Fügt ein Field der ListView in AnnounceInformationFragment mit leerem Inhalt hinzu.
     *
     * @param title Titel des DateFields
     * @param type Der Type des Feldes
     */
    public void addField(String title, TYPE type){
        Field tmpField = new Field(title, type);
        dynamicFields.add(tmpField);
    }

    public ArrayList<Field> getDynamicFields(){
        return dynamicFields;
    }

    /**
     * Enum um die Felder zu spezifizieren.
     */
    public enum TYPE{
        SIMPLETEXT, FEE, DATE, TIME, ROUTE, PICTURE, LINK, TITLE, LOCATION, DESCRIPTION
    }

    /**
     * Klasse für die Informationsfelder einer Veranstaltung
     */
    private class Field{
        private String title;
        private Object value;
        private TYPE type;

        public Field(String title, TYPE type){
            this.title = title;
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public TYPE getType(){
            return type;
        }
    }
}
