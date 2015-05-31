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
public class UserInfo implements javax.jdo.listener.StoreCallback {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent
    private String firstName;
    @Persistent
    private Integer gender;

    // Wird für die Suche verwendet, da lowerCase und Zusammensetzung von Feldern bei JDO Queries nicht angeboten wird
    private String fullNameLc;
    @Override
    public void jdoPreStore() {
        InfoPair lastNamePair = getLastName();
        String lastName = null;
        if(lastNamePair.getValue() != null && !lastNamePair.getValue().isEmpty() && lastNamePair.
                getVisibility().equals(Visibility.PUBLIC.getId())) {
            lastName = lastNamePair.getValue();
        }
        String firstName = (this.firstName == null || this.firstName.isEmpty()) ? null : this.firstName;
        if(firstName != null && lastName != null) fullNameLc = firstName.toLowerCase() + " " + lastName.toLowerCase();
        else if (firstName != null) fullNameLc = firstName.toLowerCase();
        else if (lastName != null) fullNameLc = lastName.toLowerCase();
        else fullNameLc = null;
    }

    @Persistent(defaultFetchGroup = "true")
    @Embedded(members = {
            @Persistent(name="value", columns=@Column(name="lastNameValue")),
            @Persistent(name="visibility", columns=@Column(name="lastNameVisibility")),})
    private InfoPair lastName;

    @Persistent(defaultFetchGroup = "true")
    @Embedded(members = {
            @Persistent(name="value", columns=@Column(name="dateOfBirthValue")),
            @Persistent(name="visibility", columns=@Column(name="dateOfBirthVisibility")),})
    private InfoPair dateOfBirth;

    @Persistent(defaultFetchGroup = "true")
    @Embedded(members = {
            @Persistent(name="value", columns=@Column(name="cityValue")),
            @Persistent(name="visibility", columns=@Column(name="cityVisibility")),})
    private InfoPair city;

    @Persistent(defaultFetchGroup = "true")
    @Embedded(members = {
            @Persistent(name="value", columns=@Column(name="postalCodeValue")),
            @Persistent(name="visibility", columns=@Column(name="postalCodeVisibility")),})
    private InfoPair postalCode;

    @Persistent(defaultFetchGroup = "true")
    @Embedded(members = {
            @Persistent(name="value", columns=@Column(name="descriptionValue")),
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
        gender = Gender.NA.getId();
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

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
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
