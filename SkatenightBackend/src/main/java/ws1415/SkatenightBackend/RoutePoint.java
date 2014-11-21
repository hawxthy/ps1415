package ws1415.SkatenightBackend;

import java.io.Serializable;

/**
 * Created by Pascal Otto on 18.11.14.
 */
public class RoutePoint implements Serializable {
    private double latitude;
    private double longitude;

    public RoutePoint() {
        this.latitude = 0.0;
        this.longitude = 0.0;
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
