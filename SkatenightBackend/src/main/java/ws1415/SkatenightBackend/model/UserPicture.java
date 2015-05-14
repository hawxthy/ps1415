package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Text;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die UserPicture-Klasse wird genutzt, um ein Profilbild eines Benutzers zu speichern.
 *
 * @author Martin Wrodarczyk
 */
@PersistenceCapable(detachable="true")
public class UserPicture {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent
    private Text picture;

    public UserPicture(String email){
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Text getPicture() {
        return picture;
    }

    public void setPicture(Text picture) {
        this.picture = picture;
    }
}
