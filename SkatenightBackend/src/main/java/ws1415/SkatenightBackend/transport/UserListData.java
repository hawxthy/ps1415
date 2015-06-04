package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;

/**
 * Dient ausschließlich der Übertragung der Benutzerdaten, die für die Anzeige von
 * Benutzern in Listen wichtig sind.
 */
public class UserListData {
    private String email;
    private BlobKey userPicture;
    private String firstName;
    private String lastName;
    private String city;
    private String dateOfBirth;

    public UserListData() {
    }

    public UserListData(String email, BlobKey userPicture, String firstName, String lastName,
                        String city, String dateOfBirth) {
        this.email = email;
        this.userPicture = userPicture;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.dateOfBirth = dateOfBirth;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public BlobKey getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(BlobKey userPicture) {
        this.userPicture = userPicture;
    }
}
