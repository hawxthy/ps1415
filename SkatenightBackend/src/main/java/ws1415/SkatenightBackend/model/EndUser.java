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
@PersistenceCapable(detachable="true")
public class EndUser {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent
    private Integer globalRole;
    @Persistent
    private Boolean optOutSearch;
    @Unowned
    private UserProfile userProfile;
    @Unowned
    private UserLocation userLocation;
    @Persistent
    private List<String> myFriends;
    @Persistent
    private List<String> myUserGroups;

    public EndUser() {
    }

    public EndUser(String email, UserProfile userProfile, UserLocation userLocation) {
        this.email = email;
        this.userProfile = userProfile;
        this.userLocation = userLocation;
        globalRole = GlobalRole.USER.getId();
        optOutSearch = false;
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

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public List<String> getMyFriends() {
        return myFriends;
    }

    public void setMyFriends(List<String> myFriends) {
        this.myFriends = myFriends;
    }

    public List<String> getMyUserGroups() {
        return myUserGroups;
    }

    public void setMyUserGroups(List<String> myUserGroups) {
        this.myUserGroups = myUserGroups;
    }

    public void addUserGroup(UserGroup userGroup) {
        if (userGroup != null) getMyUserGroups().add(userGroup.getName());
    }

    public void removeUserGroup(UserGroup userGroup) {
        if (userGroup != null) getMyUserGroups().remove(userGroup.getName());
    }
}
