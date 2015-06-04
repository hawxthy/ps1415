package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;

/**
 * Die primären Daten eines Benutzers.
 *
 * @author Martin Wrodarczyk
 */
public class UserPrimaryData {
    private String email;
    private BlobKey picture;
    private String firstName;
    private String lastName;

    public UserPrimaryData(){
    }

    public UserPrimaryData(String email, BlobKey picture, String firstName, String lastName) {
        this.email = email;
        this.picture = picture;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BlobKey getPicture() {
        return picture;
    }

    public void setPicture(BlobKey picture) {
        this.picture = picture;
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
}
