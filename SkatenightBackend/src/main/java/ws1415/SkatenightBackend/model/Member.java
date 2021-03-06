package ws1415.SkatenightBackend.model;

import com.google.appengine.datanucleus.annotations.Unowned;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Stellt einen User innerhalb der Anwendung dar.
 *
 * TEST PUSh
 *
 * Created by Tristan Rust on 24.10.2014.
 */
@PersistenceCapable
public class Member {
    @Persistent
    private String name;      // Angegebener Name
    @Persistent
    private Date updatedAt;   // Zeitpunkt der letzten Standortaktualisierung
    @Persistent
    private double latitude;
    @Persistent
    private double longitude;
    @Unowned
    private Long currentEventId;
    @Persistent
    private Integer currentWaypoint;
    @PrimaryKey
    @Persistent
    private String email;     // Einzigartige google Email, zum identifizieren eines Nutzers (Primary Key)
    @Persistent(defaultFetchGroup = "true")
    private Set<String> groups = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Long getCurrentEventId() {
        return currentEventId;
    }

    public void setCurrentEventId(Long currentEventId) {
        this.currentEventId = currentEventId;
    }

    public Integer getCurrentWaypoint() {
        return currentWaypoint;
    }

    public void setCurrentWaypoint(Integer currentWaypoint) {
        this.currentWaypoint = currentWaypoint;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void addGroup(UserGroup g) {
        if (g != null) {
            groups.add(g.getName());
        }
    }

    public void removeGroup(UserGroup g) {
        if (g != null) {
            groups.remove(g.getName());
        }
    }

    public Set<String> getGroups() {
        return groups;
    }
}
