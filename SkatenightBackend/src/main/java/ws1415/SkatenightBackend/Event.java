package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.datanucleus.annotations.Unowned;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonCreator;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private Route route;
    // Dynamische ArrayListe von dynamischen Komponenten
    @Persistent(serialized = "true", defaultFetchGroup = "true")
    private List<Field> dynamicFields = new ArrayList<Field>();
    @Persistent
    private int routeFieldFirst;
    @Persistent
    private int routeFieldLast;
    @Persistent(serialized = "true", defaultFetchGroup = "true")
    private ArrayList<String> memberList;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getTitle() {
        return (String)getUniqueField(FieldType.TITLE.getId()).getValue();
    }

    public void setDate(Date date){
        getUniqueField(FieldType.DATE.getId()).setValue(Long.toString(date.getTime()));
    }

    public Date getDate(){
        return new Date(Long.parseLong(getUniqueField(FieldType.DATE.getId()).getValue()));
    }

    public void setTime(Date time){
        getUniqueField(FieldType.TIME.getId()).setValue(Long.toString(time.getTime()));
    }

    public Date getTime(){
        return new Date(Long.parseLong(getUniqueField(FieldType.TIME.getId()).getValue()));
    }

    public String getFee() {
        return (String)getUniqueField(FieldType.FEE.getId()).getValue();
    }

    public String getLocation() {
        if((String)getUniqueField(FieldType.LOCATION.getId()).getValue() != null){
            return  (String)getUniqueField(FieldType.LOCATION.getId()).getValue();
        }
        return "";
    }

    public Text getDescription() {
        if(new Text(getUniqueField(FieldType.DESCRIPTION.getId()).getValue()) != null){
            return  new Text(getUniqueField(FieldType.DESCRIPTION.getId()).getValue());
        }
        return new Text("");
    }

    public void setRoute(Route route){
        this.route = route;
    }

    public Route getRoute() {
        return  route;
    }

    public List<Field> getDynamicFields(){
        return dynamicFields;
    }

    public void setDynamicFields(ArrayList<Field> list){
        this.dynamicFields = list;
    }

    /**
     * Methode zum finden von eindeutigen Feldern
     *
     * @param type der Typ des eindeutigen Feldes
     * @return Das Feld, null falls keins gefunden wurde
     */
    public Field getUniqueField(int type) {
        for (int i = 0; i < dynamicFields.size(); i++) {
            if (dynamicFields.get(i).getType() == type) {
                return dynamicFields.get(i);
            }
        }
        return null;
    }

    public int getRouteFieldFirst() {
        return routeFieldFirst;
    }

    public void setRouteFieldFirst(int routeFieldFirst) {
        this.routeFieldFirst = routeFieldFirst;
    }

    public int getRouteFieldLast() {
        return routeFieldLast;
    }

    public void setRouteFieldLast(int routeFieldLast) {
        this.routeFieldLast = routeFieldLast;
    }

    public void setMemberList(ArrayList<String> memberList) {
        this.memberList = memberList;
    }

    public ArrayList<String> getMemberList() {
        if (this.memberList == null) return new ArrayList();
        else return this.memberList;
    }
}
