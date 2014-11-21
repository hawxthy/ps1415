package ws1415.SkatenightBackend;

import java.util.Date;

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
    @PrimaryKey
    @Persistent
    private String email;     // Einzigartige google Email, zum identifizieren eines Nutzers (Primary Key)

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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
