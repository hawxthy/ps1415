package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Repräsentiert eine Strecke mit den zugehörigen Metadaten.
 * Created by Richard on 31.10.2014.
 */
@PersistenceCapable
public class Route {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private String name;
    /**
     * Speichert die Koordinaten der Route als komprimierten String ab.
     */
    @Persistent
    private Text routeData;
    @Persistent(serialized = "true", defaultFetchGroup = "true")
    private ArrayList<RoutePoint> routePoints;
    @Persistent
    private String length;
    @Persistent(serialized = "true", defaultFetchGroup = "true")
    private ArrayList<ServerWaypoint> waypoints = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Text getRouteData() {
        return routeData;
    }

    public void setRouteData(Text routeData) {
        this.routeData = routeData;
    }

    public ArrayList<RoutePoint> getRoutePoints() {
        if (routePoints == null) return new ArrayList();
        else return routePoints;
    }

    public void setRoutePoints(ArrayList<RoutePoint> routePoints) {
        this.routePoints = routePoints;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public Key getKey() {
        return key;
    }

    public ArrayList<ServerWaypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(ArrayList<ServerWaypoint> waypoints) {
        this.waypoints = waypoints;
    }
}
