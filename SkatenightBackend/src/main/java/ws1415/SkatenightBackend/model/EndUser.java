package ws1415.SkatenightBackend.model;

import com.google.appengine.datanucleus.annotations.Unowned;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die EndUser-Klasse repräsentiert einen Benutzer.
 *
 * @author Martin Wrodarczyk
 */
@PersistenceCapable
public class EndUser {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent
    private Integer globalRole;
    @Persistent
    private Boolean optOutSearch;
    @Persistent
    private Integer groupVisibility;
    @Unowned
    private UserInfoPicture userInfoPicture;
    @Unowned
    private UserLocation userLocation;
    @Persistent
    private List<String> myUserGroups;
    @Persistent
    private List<Long> myEvents;
    @Persistent
    private List<String> myFriends;

    public EndUser() {
    }

    public EndUser(String email, UserInfoPicture userInfoPicture, UserLocation userLocation) {
        this.email = email;
        this.userInfoPicture = userInfoPicture;
        this.userLocation = userLocation;
        globalRole = GlobalRole.USER.getId();
        optOutSearch = false;
        groupVisibility = Visibility.PUBLIC.getId();
        myUserGroups = new ArrayList<>();
        myEvents = new ArrayList<>();
        myFriends = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getGlobalRole() {
        return globalRole;
    }

    public void setGlobalRole(Integer globalRole) {
        this.globalRole = globalRole;
    }

    public Boolean isOptOutSearch() {
        return optOutSearch;
    }

    public void setOptOutSearch(Boolean optOutSearch) {
        this.optOutSearch = optOutSearch;
    }

    public Integer getGroupVisibility() {
        return groupVisibility;
    }

    public void setGroupVisibility(Integer groupVisibility) {
        this.groupVisibility = groupVisibility;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public UserInfoPicture getUserInfoPicture() {
        return userInfoPicture;
    }

    public void setUserInfoPicture(UserInfoPicture userInfoPicture) {
        this.userInfoPicture = userInfoPicture;
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

    public List<String> getMyFriends() {
        return myFriends;
    }

    public void setMyFriends(List<String> myFriends) {
        this.myFriends = myFriends;
    }

    public void addUserGroup(UserGroup userGroup) {
        if (userGroup != null) myUserGroups.add(userGroup.getName());
    }

    public void removeUserGroup(UserGroup userGroup) {
        if (userGroup != null) myUserGroups.remove(userGroup.getName());
    }

    public void addEvent(Event event) {
        if (event != null) myEvents.add(event.getId());
    }

    public void removeEvent(Event event) {
        if (event != null) myEvents.remove(event.getId());
    }
}
