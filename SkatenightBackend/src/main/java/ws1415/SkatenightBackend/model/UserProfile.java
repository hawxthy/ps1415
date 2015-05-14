package ws1415.SkatenightBackend.model;

import com.google.appengine.datanucleus.annotations.Unowned;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die UserInfo-Klasse speichert die allgemeinen Informationen und das Profilbild zu einem Benutzer.
 *
 * @author Martin Wrodarczyk
 */
@PersistenceCapable(detachable="true")
public class UserProfile {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent
    private Integer groupVisibility;
    @Unowned
    private UserInfo userInfo;
    @Unowned
    private UserPicture userPicture;
    @Persistent
    private List<String> myUserGroups;
    @Persistent
    private List<Long> myEvents;

    public UserProfile(String email, UserInfo userInfo, UserPicture userPicture) {
        this.email = email;
        groupVisibility = Visibility.PUBLIC.getId();
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

    public Integer getGroupVisibility() {
        return groupVisibility;
    }

    public void setGroupVisibility(Integer groupVisibility) {
        this.groupVisibility = groupVisibility;
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

    public List<String> getMyUserGroups() {
        return myUserGroups;
    }

    public void setMyUserGroups(List<String> myUserGroups) {
        this.myUserGroups = myUserGroups;
    }

    public List<Long> getMyEvents() {
        return myEvents;
    }

    public void setMyEvents(List<Long> myEvents) {
        this.myEvents = myEvents;
    }

    public void addUserGroup(UserGroup userGroup) {
        if (userGroup != null) getMyUserGroups().add(userGroup.getName());
    }

    public void removeUserGroup(UserGroup userGroup) {
        if (userGroup != null) getMyUserGroups().remove(userGroup.getName());
    }

    public void addEvent(Event event) {
        if (event != null) getMyEvents().add(event.getId());
    }

    public void removeEvent(Event event) {
        if (event != null) getMyEvents().remove(event.getId());
    }
}
