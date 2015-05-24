package ws1415.SkatenightBackend.transport;

import java.util.ArrayList;
import java.util.List;

import ws1415.SkatenightBackend.model.UserInfo;
import ws1415.SkatenightBackend.model.UserPicture;

/**
 * Die UserProfile-Klasse ist für die Übertragung der Nutzerprofildaten an die Anwendung
 * implementiert.
 *
 * @author Martin Wrodarczyk
 */
public class UserProfile{
    private String email;
    private boolean optOutSearch;
    private Integer showPrivateGroups;
    private UserInfo userInfo;
    private UserPicture userPicture;
    private List<UserGroupMetaData> myUserGroups;
    private List<EventMetaData> myEvents;

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

    public Integer getShowPrivateGroups() {
        return showPrivateGroups;
    }

    public void setShowPrivateGroups(Integer showPrivateGroups) {
        this.showPrivateGroups = showPrivateGroups;
    }

    public boolean isOptOutSearch() {
        return optOutSearch;
    }

    public void setOptOutSearch(boolean optOutSearch) {
        this.optOutSearch = optOutSearch;
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

    public List<UserGroupMetaData> getMyUserGroups() {
        return myUserGroups;
    }

    public void setMyUserGroups(List<UserGroupMetaData> myUserGroups) {
        this.myUserGroups = myUserGroups;
    }

    public List<EventMetaData> getMyEvents() {
        return myEvents;
    }

    public void setMyEvents(List<EventMetaData> myEvents) {
        this.myEvents = myEvents;
    }
}
