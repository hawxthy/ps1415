package ws1415.SkatenightBackend.transport;

/**
 * Dient der Übertragung des Benutzernamens zusammen mit der dazugehörigen Standortinformationen.
 *
 * @author Martin Wrodarczyk
 */
public class UserLocationInfo {
    private String email;
    private String firstName;
    private String lastName;
    private double latitude;
    private double longitude;

    public UserLocationInfo(){
    }

    public UserLocationInfo(String email, String firstName, String lastName, double latitude, double longitude) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
