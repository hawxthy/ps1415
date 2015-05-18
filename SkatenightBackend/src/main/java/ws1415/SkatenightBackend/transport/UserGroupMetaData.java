package ws1415.SkatenightBackend.transport;

import java.util.HashSet;

/**
 * Created by Bernd Eissing on 02.05.2015.
 */
public class UserGroupMetaData {
    private String name;
    private String creator;
    private boolean isOpen;
    private int memberCount;

   public UserGroupMetaData(String name, String creator, boolean isOpen, int memberCount){
       this.name = name;
       this.creator = creator;
       this.isOpen = isOpen;
       this.memberCount = memberCount;
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

    public boolean isOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
}
