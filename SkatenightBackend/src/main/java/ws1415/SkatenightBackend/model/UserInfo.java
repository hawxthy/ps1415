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
    private Integer lastNameVisibility;
    @Persistent
    private String gender;
    @Persistent
    private String dateOfBirth;
    @Persistent
    private Integer dateOfBirthVisibility;
    @Persistent
    private String city;
    @Persistent
    private Integer cityVisibility;
    @Persistent
    private String postalCode;
    @Persistent
    private Integer postalCodeVisibility;
    @Persistent
    private String description;

    public UserInfo() {
    }

    public UserInfo(String email) {
        this.email = email;
        firstName = "";
        lastName = "";
        lastNameVisibility = Visibility.PUBLIC.getId();
        gender = Gender.NA.getRepresentation();
        dateOfBirth = "";
        dateOfBirthVisibility = Visibility.PUBLIC.getId();
        city = "";
        cityVisibility = Visibility.PUBLIC.getId();
        postalCode = "";
        postalCodeVisibility = Visibility.PUBLIC.getId();
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

    public Integer getLastNameVisibility() {
        return lastNameVisibility;
    }

    public void setLastNameVisibility(Integer lastNameVisibility) {
        this.lastNameVisibility = lastNameVisibility;
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

    public Integer getDateOfBirthVisibility() {
        return dateOfBirthVisibility;
    }

    public void setDateOfBirthVisibility(Integer dateOfBirthVisibility) {
        this.dateOfBirthVisibility = dateOfBirthVisibility;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getCityVisibility() {
        return cityVisibility;
    }

    public void setCityVisibility(Integer cityVisibility) {
        this.cityVisibility = cityVisibility;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Integer getPostalCodeVisibility() {
        return postalCodeVisibility;
    }

    public void setPostalCodeVisibility(Integer postalCodeVisibility) {
        this.postalCodeVisibility = postalCodeVisibility;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
