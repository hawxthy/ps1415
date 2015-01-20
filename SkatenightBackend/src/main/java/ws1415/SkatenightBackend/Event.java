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
    @Persistent
    private boolean notificationSend = false;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
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

    public boolean isNotificationSend() {
        return notificationSend;
    }

    public void setNotificationSend(boolean notificationSend) {
        this.notificationSend = notificationSend;
    }

    // ---------- Hilfsmethoden für den Umgang mit den dynamischen Eigenschaften ---------

    /**
     * Methode zum finden von Id's von eindeutigen Feldern.
     *
     * @param type der Typ des eindeutigen Feldes
     * @return Die Id, -1 falls keins gefunden wurde
     */
    public int getUniqueFieldId(FieldType type){
        for(int i = 0; i < this.getDynamicFields().size(); i++){
            if(this.getDynamicFields().get(i).getType() == type.getId()){
                return i;
            }
        }
        return -1;
    }

    /**
     * Verbindet die beiden Datumsangaben des Events zu einem.
     * @return Das zusammengefügte Date.
     */
    public Date getFusedDate() {
        Date time = new Date(Long.parseLong(getUniqueField(FieldType.TIME.getId()).getValue()));
        Date date = new Date(Long.parseLong(getUniqueField(FieldType.DATE.getId()).getValue()));
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
    public Field getUniqueField(int type) {
        for (int i = 0; i < this.getDynamicFields().size(); i++) {
            if (this.getDynamicFields().get(i).getType() == type) {
                return this.getDynamicFields().get(i);
            }
        }
        return null;
    }
}
