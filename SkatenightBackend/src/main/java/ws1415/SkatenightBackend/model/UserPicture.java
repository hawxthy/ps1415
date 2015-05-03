package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Blob;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die UserPicture-Klasse wird genutzt, um ein Profilbild eines Benutzers zu speichern.
 *
 * @author Martin Wrodarczyk
 */
@PersistenceCapable
public class UserPicture {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent
    private Blob picture;

    public UserPicture(String email){
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Blob getPicture() {
        return picture;
    }

    public void setPicture(Blob picture) {
        this.picture = picture;
    }
}