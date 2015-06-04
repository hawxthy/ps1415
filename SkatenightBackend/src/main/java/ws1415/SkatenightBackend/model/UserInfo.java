package ws1415.SkatenightBackend.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Die UserInfo-Klasse speichert die allgemeinen Informationen zu einem Benutzer, wobei die Felder
 * dieser Klasse im Datastore im EndUser gespeichert werden.
 *
 * @author Martin Wrodarczyk
 */
@PersistenceCapable
@EmbeddedOnly
public class UserInfo{
    @Persistent
    private String firstName;
    @Persistent
    private Gender gender;

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
        private Visibility visibility;

        public InfoPair(String value, Visibility visibility) {
            this.value = value;
            this.visibility = visibility;
        }

        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public Visibility getVisibility() {
            return visibility;
        }
        public void setVisibility(Visibility visibility) {
            this.visibility = visibility;
        }
    }

    public UserInfo() {
    }

    public UserInfo(String firstName, String lastName) {
        this.firstName = firstName;
        gender = Gender.NA;
        this.lastName = new InfoPair(lastName , Visibility.PUBLIC);
        dateOfBirth = new InfoPair("", Visibility.PUBLIC);
        city = new InfoPair("", Visibility.PUBLIC);
        postalCode = new InfoPair("", Visibility.PUBLIC);
        description = new InfoPair("", Visibility.PUBLIC);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
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
