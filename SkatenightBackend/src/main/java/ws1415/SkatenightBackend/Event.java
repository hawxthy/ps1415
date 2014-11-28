package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.datanucleus.annotations.Unowned;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonCreator;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    @Persistent(serialized = "true", defaultFetchGroup = "true")
    private ArrayList<Field> dynamicFields;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getTitle() {
        Field f = getUniqueField(TYPE.TITLE);
        if (f != null) {
            return (String)getUniqueField(TYPE.TITLE).getValue();
        } else {
            return null;
        }
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
        Field f = getUniqueField(TYPE.FEE);
        if (f != null) {
            return (String)getUniqueField(TYPE.FEE).getValue();
        } else {
            return null;
        }
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getLocation() {
        Field f = getUniqueField(TYPE.LOCATION);
        if (f != null) {
            return (String)getUniqueField(TYPE.LOCATION).getValue();
        } else {
            return null;
        }
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Text getDescription() {
        Field f = getUniqueField(TYPE.DESCRIPTION);
        if (f != null) {
            return (Text)getUniqueField(TYPE.DESCRIPTION).getValue();
        } else {
            return null;
        }
    }

    public void setDescription(Text description) {
        this.description = description;
    }

    public Route getRoute() {
        Field f = getUniqueField(TYPE.ROUTE);
        if (f != null) {
            return (Route)getUniqueField(TYPE.ROUTE).getValue();
        } else {
            return null;
        }
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
        if (dynamicFields == null) {
            dynamicFields = new ArrayList<Field>();
        }
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
    private class Field implements Serializable {
        private String title;
        private Object value;
        private Blob byteData;
        private TYPE type;

        public Field(String title, TYPE type){
            this.title = title;
            this.type = type;
        }

        @JsonCreator
        public Field() {
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

        public Blob getByteData() {
            return byteData;
        }

        public void setByteData(Blob byteData) {
            this.byteData = byteData;
        }

        public TYPE getType(){
            return type;
        }

        public void setType(TYPE type) {
            this.type = type;
        }
    }
}
