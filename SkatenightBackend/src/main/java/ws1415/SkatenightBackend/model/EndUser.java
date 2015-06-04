package ws1415.SkatenightBackend.model;

import com.google.appengine.api.blobstore.BlobKey;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die EndUser-Klasse repräsentiert einen Benutzer.
 *
 * @author Martin Wrodarczyk
 */
@PersistenceCapable(detachable="true")
public class EndUser implements javax.jdo.listener.StoreCallback {
    @PrimaryKey
    @Persistent
    private String email;
    @Persistent
    private GlobalRole globalRole;
    @Persistent
    private Boolean optOutSearch;
    @Persistent
    private Visibility showPrivateGroups;
    @Persistent(defaultFetchGroup = "true")
    private BlobKey userPicture;
    @Persistent
    @Embedded
    private UserInfo userInfo;
    @Persistent
    @Embedded
    private UserLocation userLocation;
    @Persistent
    private List<String> myUserGroups;
    @Persistent
    private List<Long> myEvents;
    @Persistent
    private List<String> myFriends;

    public EndUser() {
    }

    public EndUser(String email, UserLocation userLocation, UserInfo userInfo) {
        this.email = email;
        this.userLocation = userLocation;
        this.userInfo = userInfo;
        globalRole = GlobalRole.USER;
        optOutSearch = false;
        showPrivateGroups = Visibility.PUBLIC;
        myUserGroups = new ArrayList<>();
        myEvents = new ArrayList<>();
        myFriends = new ArrayList<>();
    }

    // Wird für die Suche verwendet, da lowerCase und Zusammensetzung von Feldern bei JDO Queries nicht angeboten wird
    private String fullNameLc;
    @Override
    public void jdoPreStore() {
        UserInfo.InfoPair lastNamePair = userInfo.getLastName();
        String firstNameUserInfo = userInfo.getFirstName();
        String lastName = null;
        if(lastNamePair.getValue() != null && !lastNamePair.getValue().isEmpty() && lastNamePair.
                getVisibility().equals(Visibility.PUBLIC)) {
            lastName = lastNamePair.getValue();
        }
        String firstName = (firstNameUserInfo == null || firstNameUserInfo.isEmpty()) ? null : firstNameUserInfo;
        if(firstName != null && lastName != null) fullNameLc = firstName.toLowerCase() + " " + lastName.toLowerCase();
        else if (firstName != null) fullNameLc = firstName.toLowerCase();
        else if (lastName != null) fullNameLc = lastName.toLowerCase();
        else fullNameLc = null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public GlobalRole getGlobalRole() {
        return globalRole;
    }

    public void setGlobalRole(GlobalRole globalRole) {
        this.globalRole = globalRole;
    }

    public Boolean isOptOutSearch() {
        return optOutSearch;
    }

    public void setOptOutSearch(Boolean optOutSearch) {
        this.optOutSearch = optOutSearch;
    }

    public Visibility getShowPrivateGroups() {
        return showPrivateGroups;
    }

    public void setShowPrivateGroups(Visibility showPrivateGroups) {
        this.showPrivateGroups = showPrivateGroups;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public BlobKey getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(BlobKey userPicture) {
        this.userPicture = userPicture;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<String> getMyUserGroups() {
        if(myUserGroups == null) return new ArrayList<>();
        return myUserGroups;
    }

    public void setMyUserGroups(List<String> myUserGroups) {
        this.myUserGroups = myUserGroups;
    }

    public List<Long> getMyEvents() {
        if(myEvents == null) return new ArrayList<>();
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
