package ws1415.SkatenightBackend;

import java.io.Serializable;

/**
 * Datenmodell für einen Wegpunkt auf einer Route.
 *
 * @author Richard
 */
public class ServerWaypoint implements Serializable {
    private double latitude;
    private double longitude;
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
}
