package ws1415.SkatenightBackend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Die UserProfile-Klasse ist für die Übertragung der Nutzerprofildaten an die Anwendung
 * implementiert.
 *
 * @author Martin Wrodarczyk
 */
public class UserProfile {
    private String email;
    private UserInfo userInfo;
    private UserPicture userPicture;
    private List<UserGroup> myUserGroups;
    private List<Event> myEvents;

    public UserProfile(){
    }

    public UserProfile(String email, UserInfo userInfo, UserPicture userPicture) {
        this.email = email;
        this.userInfo = userInfo;
        this.userPicture = userPicture;
        myUserGroups = new ArrayList<>();
        myEvents = new ArrayList<>();
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

    public List<UserGroup> getMyUserGroups() {
        return myUserGroups;
    }

    public void setMyUserGroups(List<UserGroup> myUserGroups) {
        this.myUserGroups = myUserGroups;
    }

    public List<Event> getMyEvents() {
        return myEvents;
    }

    public void setMyEvents(List<Event> myEvents) {
        this.myEvents = myEvents;
    }
}
