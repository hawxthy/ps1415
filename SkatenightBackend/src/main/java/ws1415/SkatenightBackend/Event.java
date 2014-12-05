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
import java.util.List;
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
public class Event {
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
    private List<Field> dynamicFields = new ArrayList<Field>();

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getTitle() {
        return (String)getUniqueField(Field.TYPE.TITLE).getValue();
    }

    public void setDate(Date date){
        getUniqueField(Field.TYPE.DATE).setValue(Long.toString(date.getTime()));
    }

    public Date getDate(){
        return new Date(Long.parseLong(getUniqueField(Field.TYPE.DATE).getValue()));
    }

    public void setTime(Date time){
        getUniqueField(Field.TYPE.TIME).setValue(Long.toString(time.getTime()));
    }

    public Date getTime(){
        return new Date(Long.parseLong(getUniqueField(Field.TYPE.TIME).getValue()));
    }

    public String getFee() {
        return (String)getUniqueField(Field.TYPE.FEE).getValue();
    }

    public String getLocation() {
        return  (String)getUniqueField(Field.TYPE.LOCATION).getValue();
    }

    public Text getDescription() {
        return  new Text(getUniqueField(Field.TYPE.DESCRIPTION).getValue());
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
    public Field getUniqueField(Field.TYPE type){
        for(int i = 0; i < dynamicFields.size(); i++){
            if(dynamicFields.get(i).getType() == type){
                return dynamicFields.get(i);
            }
        }
        return null;
    }


    public List<Field> getDynamicFields(){
        return dynamicFields;
    }

    public void setDynamicFields(ArrayList<Field> list){
        this.dynamicFields = list;
    }
}
