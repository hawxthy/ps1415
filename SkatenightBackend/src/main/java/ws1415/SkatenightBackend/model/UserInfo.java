package ws1415.SkatenightBackend.model;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die UserInfo-Klasse speichert die allgemeinen Informationen zu einem Benutzer.
 *
 * @author Martin Wrodarczyk
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
    private String gender;
    @Persistent
    private String dateOfBirth;
    @Persistent
    private String city;
    @Persistent
    private String postalCode;
    @Persistent
    private String description;

    public UserInfo() {
    }

    public UserInfo(String email) {
        this.email = email;
        firstName = "";
        lastName = "";
        gender = Gender.NA.getRepresentation();
        dateOfBirth = "";
        city = "";
        postalCode = "";
        description = "";
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
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
}
