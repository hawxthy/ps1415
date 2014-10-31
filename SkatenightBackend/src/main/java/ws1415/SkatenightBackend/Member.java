package ws1415.SkatenightBackend;

import java.util.Date;

/**
 * Created by Tristan Rust on 24.10.2014.
 * @TODO Referenz auf die google mail Adresse setzten als _owner
 */
public class Member {

    private String name;
    private String updatedAt;
    private String location;
    private String email;

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(String updatedAt) {this.updatedAt = updatedAt;}

    public String getLocation() {return location;}

    public void setLocation(String location) {this.location = location;}

    public void setEmail(String email) {this.email = email;}

    public String getEmail() {return email;}

}
