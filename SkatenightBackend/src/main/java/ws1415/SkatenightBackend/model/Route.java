package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;

/**
 * Repräsentiert eine Strecke mit den zugehörigen Metadaten.
 * @author Richard Schulze
 */
@Entity
public class Route {
    @Id
    private Long id;
    @Index
    private String name;
    /**
     * Speichert die Koordinaten der Route als komprimierten String ab.
     */
    private Text routeData;
    private ArrayList<RoutePoint> routePoints;
    private String length;
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

    public ArrayList<ServerWaypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(ArrayList<ServerWaypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
