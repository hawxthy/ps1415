package ws1415.SkatenightBackend;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 *
 * @author Richard
 */
public class ServerWaypoint implements Serializable {
    private LatLng position;
    private String title;

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
