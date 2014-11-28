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
    private Date time;
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

    public void setDate(Date date){
        getUniqueField(TYPE.DATE).setValue(date);
    }

    public Date getDate(){
        return (Date)getUniqueField(TYPE.DATE).getValue();
    }

    public void setTime(Date time){
        getUniqueField(TYPE.TIME).setValue(time);
    }

    public Date getTime(){
        return (Date)getUniqueField(TYPE.TIME).getValue();
    }

    public String getFee() {
        return (String)getUniqueField(TYPE.FEE).getValue();
    }

    public String getLocation() {
        return  (String)getUniqueField(TYPE.LOCATION).getValue();
    }

    public Text getDescription() {
        return  (Text)getUniqueField(TYPE.DESCRIPTION).getValue();
    }

    public void setRoute(Route route){
        this.route = route;
    }

    public Route getRoute() {
        return  route;
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


    public ArrayList<Field> getDynamicFields(){
        return dynamicFields;
    }

    /**
     * Enum um die Felder zu spezifizieren.
     */
    private enum TYPE{
        SIMPLETEXT, FEE, DATE, TIME, ROUTE, PICTURE, LINK, TITLE, LOCATION, DESCRIPTION
    }


    /**
     * Klasse für die Informationsfelder einer Veranstaltung
     */
    public class Field{
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

        public void setType(TYPE type){
            this.type = type;
        }
    }
}
