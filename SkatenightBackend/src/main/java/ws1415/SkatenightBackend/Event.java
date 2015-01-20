package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.annotations.Unowned;

import java.util.ArrayList;
import java.util.List;

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
}
