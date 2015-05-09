package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.annotations.Unowned;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private UserInfo userInfo;
    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private UserLocation userLocation;
    @Persistent
    private Map<Key, Role> myRoles;
    @Persistent
    private List<String> myUserGroups;
    @Persistent
    private List<Long> myEvents;

    public EndUser() {
    }

    public EndUser(String email, UserInfo userInfo, UserLocation userLocation) {
        this.email = email;
        this.userInfo = userInfo;
        this.userLocation = userLocation;
        myRoles = new HashMap<Key, Role>();
        myUserGroups = new ArrayList<>();
        myEvents = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
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

    public Map<Key, Role> getMyRoles() {
        return myRoles;
    }

    public void setMyRoles(Map<Key, Role> myRoles) {
        this.myRoles = myRoles;
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
