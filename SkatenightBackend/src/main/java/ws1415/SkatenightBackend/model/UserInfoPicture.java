package ws1415.SkatenightBackend.model;

import com.google.appengine.datanucleus.annotations.Unowned;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die UserInfo-Klasse speichert die allgemeinen Informationen und das Profilbild zu einem Benutzer.
 *
 * @author Martin Wrodarczyk
 */
@PersistenceCapable
public class UserInfoPicture {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private UserInfo userInfo;
    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private UserPicture userPicture;

    public UserInfoPicture(String email, UserInfo userInfo, UserPicture userPicture) {
        this.email = email;
        this.userInfo = userInfo;
        this.userPicture = userPicture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserPicture getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(UserPicture userPicture) {
        this.userPicture = userPicture;
    }
}
