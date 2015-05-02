package ws1415.SkatenightBackend.model;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die UserLocation-Klasse speichert Standortinformationen zu einem Benutzer.
 *
 * @author Martin Wrodarczyk
 */
@PersistenceCapable
public class UserLocation {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent
    private double latitude;
    @Persistent
    private double longitude;
    @Persistent
    private Date updatedAt;
    @Persistent
    private Long currentEventId;
    @Persistent
    private Integer currentWaypoint;

    public UserLocation(){}

    public UserLocation(String email){
        this.email = email;
        latitude = 0;
        longitude = 0;
        updatedAt = new Date();
        currentEventId = 0L;
        currentWaypoint = 0;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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
}
