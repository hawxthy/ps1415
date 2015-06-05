package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;

import ws1415.SkatenightBackend.model.UserInfo;
import ws1415.SkatenightBackend.model.Visibility;

/**
 * Dient der Übertragung der Nutzerprofildaten zur Bearbeitung eines Nutzerprofils an die Anwendung.
 *
 * @author Martin Wrodarczyk
 */
public class UserProfileEdit {
    private String email;
    private boolean optOutSearch;
    private Visibility showPrivateGroups;
    private BlobKey userPicture;
    private UserInfo userInfo;

    public UserProfileEdit(){
    }

    public UserProfileEdit(String email, boolean optOutSearch, Visibility showPrivateGroups,
                           BlobKey userPicture, UserInfo userInfo) {
        this.email = email;
        this.optOutSearch = optOutSearch;
        this.showPrivateGroups = showPrivateGroups;
        this.userPicture = userPicture;
        this.userInfo = userInfo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Visibility getShowPrivateGroups() {
        return showPrivateGroups;
    }

    public void setShowPrivateGroups(Visibility showPrivateGroups) {
        this.showPrivateGroups = showPrivateGroups;
    }

    public boolean isOptOutSearch() {
        return optOutSearch;
    }

    public void setOptOutSearch(boolean optOutSearch) {
        this.optOutSearch = optOutSearch;
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
}
