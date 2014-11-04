package ws1415.SkatenightBackend;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by Tristan Rust on 24.10.2014.
 *
 * Stellt einen User innerhalb der Anwendung dar.
 *
 */
@PersistenceCapable
public class Member {
    @Persistent
    private String name;      // Angegebener Name
    @Persistent
    private Date updatedAt;   // Zeitpunkt der letzten Standortaktualisierung
    @Persistent
    private String location;  // Die aktuelle Location als String kodiert
    @PrimaryKey
    @Persistent
    private String email;     // Einzigartige google Email, zum identifizieren eines Nutzers

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public Date getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(Date updatedAt) {this.updatedAt = updatedAt;}

    public String getLocation() {return location;}

    public void setLocation(String location) {this.location = location;}

    public void setEmail(String email) {this.email = email;}

    public String getEmail() {return email;}

}
