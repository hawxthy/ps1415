package ws1415.SkatenightBackend.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die UserInfo-Klasse speichert die allgemeinen Informationen zu einem Benutzer.
 *
 * @author Martin Wrodarczyk
 */
@PersistenceCapable(detachable="true")
public class UserInfo {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent
    private String firstName;
    @Persistent
    private String gender;

    @Persistent(defaultFetchGroup = "true")
    @Embedded(members = {
            @Persistent(name="value", columns=@Column(name="lastName")),
            @Persistent(name="visibility", columns=@Column(name="lastNameVisibility")),})
    private InfoPair lastName;

    @Persistent(defaultFetchGroup = "true")
    @Embedded(members = {
            @Persistent(name="value", columns=@Column(name="dateOfBirth")),
            @Persistent(name="visibility", columns=@Column(name="dateOfBirthVisibility")),})
    private InfoPair dateOfBirth;

    @Persistent(defaultFetchGroup = "true")
    @Embedded(members = {
            @Persistent(name="value", columns=@Column(name="city")),
            @Persistent(name="visibility", columns=@Column(name="cityVisibility")),})
    private InfoPair city;

    @Persistent(defaultFetchGroup = "true")
    @Embedded(members = {
            @Persistent(name="value", columns=@Column(name="postalCode")),
            @Persistent(name="visibility", columns=@Column(name="postalCodeVisibility")),})
    private InfoPair postalCode;

    @Persistent(defaultFetchGroup = "true")
    @Embedded(members = {
            @Persistent(name="value", columns=@Column(name="description")),
            @Persistent(name="visibility", columns=@Column(name="descriptionVisibility")),})
    private InfoPair description;

    /**
     * Die InfoPair-Klasse speichert zu einer Information, seinen eigentlichen Wert und dazu
     * die Sichtbarkeit der Information.
     */
    @PersistenceCapable(detachable="true")
    @EmbeddedOnly
    public static class InfoPair {
        @Persistent
        private String value;
        @Persistent
        private Integer visibility;

        public InfoPair(String value, Integer visibility) {
            this.value = value;
            this.visibility = visibility;
        }

        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public Integer getVisibility() {
            return visibility;
        }
        public void setVisibility(Integer visibility) {
            this.visibility = visibility;
        }
    }

    public UserInfo() {
    }

    public UserInfo(String email) {
        this.email = email;
        firstName = "";
        gender = Gender.NA.getRepresentation();
        lastName = new InfoPair("", Visibility.PUBLIC.getId());
        dateOfBirth = new InfoPair("", Visibility.PUBLIC.getId());
        city = new InfoPair("", Visibility.PUBLIC.getId());
        postalCode = new InfoPair("", Visibility.PUBLIC.getId());
        description = new InfoPair("", Visibility.PUBLIC.getId());
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public InfoPair getLastName() {
        return lastName;
    }

    public void setLastName(InfoPair lastName) {
        this.lastName = lastName;
    }

    public InfoPair getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(InfoPair dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public InfoPair getCity() {
        return city;
    }

    public void setCity(InfoPair city) {
        this.city = city;
    }

    public InfoPair getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(InfoPair postalCode) {
        this.postalCode = postalCode;
    }

    public InfoPair getDescription() {
        return description;
    }

    public void setDescription(InfoPair description) {
        this.description = description;
    }
}
