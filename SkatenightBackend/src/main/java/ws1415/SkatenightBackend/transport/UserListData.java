package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;

import ws1415.SkatenightBackend.model.UserInfo;

/**
 * Dient ausschließlich der Übertragung der Benutzerdaten, die für die Anzeige von
 * Benutzern in Listen wichtig sind.
 */
public class UserListData {
    private String email;
    private UserInfo userInfo;
    private BlobKey userPicture;

    public UserListData() {
    }

    public UserListData(String email, UserInfo userInfo) {
        this.email = email;
        this.userInfo = userInfo;
    }

    public UserListData(String email, UserInfo userInfo, BlobKey userPicture) {
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

    public BlobKey getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(BlobKey userPicture) {
        this.userPicture = userPicture;
    }
}
