package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;

import java.util.List;

import ws1415.SkatenightBackend.model.Gender;

/**
 * Die UserProfile-Klasse ist für die Übertragung der Nutzerprofildaten an die Anwendung
 * implementiert.
 *
 * @author Martin Wrodarczyk
 */
public class UserProfile {
    private String email;
    private BlobKey userPicture;
    private String firstName;
    private String lastName;
    private Gender gender;
    private String dateOfBirth;
    private String city;
    private String postalCode;
    private String description;
    private List<UserGroupMetaData> myUserGroups;
    private int eventCount;

    public UserProfile(){
    }

    public UserProfile(String email, BlobKey userPicture, String firstName, String lastName,
                       Gender gender, String dateOfBirth, String city, String postalCode,
                       String description, List<UserGroupMetaData> myUserGroups, int eventCount) {
        this.email = email;
        this.userPicture = userPicture;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.city = city;
        this.postalCode = postalCode;
        this.description = description;
        this.myUserGroups = myUserGroups;
        this.eventCount = eventCount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BlobKey getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(BlobKey userPicture) {
        this.userPicture = userPicture;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<UserGroupMetaData> getMyUserGroups() {
        return myUserGroups;
    }

    public void setMyUserGroups(List<UserGroupMetaData> myUserGroups) {
        this.myUserGroups = myUserGroups;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }
}
