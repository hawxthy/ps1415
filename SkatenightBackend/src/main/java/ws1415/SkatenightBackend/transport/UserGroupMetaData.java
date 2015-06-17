package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;

/**
 * Created by Bernd Eissing on 02.05.2015.
 */
public class UserGroupMetaData {
    private String name;
    private String creator;
    private boolean isPrivat;
    private int memberCount;
    private BlobKey blobKey;

   public UserGroupMetaData(String name, String creator, boolean isPrivat, int memberCount, BlobKey blobKey){
       this.name = name;
       this.creator = creator;
       this.isPrivat = isPrivat;
       this.memberCount = memberCount;
       this.blobKey = blobKey;
   }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public boolean getPrivat() {
        return isPrivat;
    }

    public void setIsPrivat(boolean isPrivat) {
        this.isPrivat = isPrivat;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public BlobKey getBlobKey() {
        return blobKey;
    }

    public void setBlobKeyValue(BlobKey blobKeye) {
        this.blobKey = blobKey;
    }
}
