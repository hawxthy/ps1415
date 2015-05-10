package ws1415.SkatenightBackend.model;

import com.google.appengine.datanucleus.annotations.Unowned;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die UserInfo-Klasse speichert die allgemeinen Informationen zu einem Benutzer.
 */
@PersistenceCapable
public class UserInfo {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent
    private String firstName;
    @Persistent
    private String lastName;
    @Persistent
    private Gender gender;
    @Persistent
    private String dateOfBirth;
    @Persistent
    private String city;
    @Persistent
    private String postalCode;
    @Persistent
    private String description;
    @Persistent
    @Unowned
    private UserPicture picture;

    public UserInfo() {
    }

    public UserInfo(String email, UserPicture picture) {
        this.email = email;
        firstName = "Martin";
        lastName = "";
        gender = Gender.NA;
        dateOfBirth = "";
        city = "";
        postalCode = "";
        description = "";
        this.picture = picture;
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

    public UserPicture getPicture() {
        return picture;
    }

    public void setPicture(UserPicture picture) {
        this.picture = picture;
    }
}
