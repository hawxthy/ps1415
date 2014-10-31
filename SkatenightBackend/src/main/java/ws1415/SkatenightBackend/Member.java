package ws1415.SkatenightBackend;

import java.util.Date;

/**
 * Created by Tristan Rust on 24.10.2014.
 *
 * Stellt einen User innerhalb der Anwendung dar.
 *
 */
public class Member {

    private String name;      // Angegebener Name
    private String updatedAt; // Zeitpunkt der letzten Standortaktualisierung
    private String location;  // Die aktuelle Location als String kodiert
    private String email;     // Einzigartige google Email, zum identifizieren eines Nutzers

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(String updatedAt) {this.updatedAt = updatedAt;}

    public String getLocation() {return location;}

    public void setLocation(String location) {this.location = location;}

    public void setEmail(String email) {this.email = email;}

    public String getEmail() {return email;}

}
