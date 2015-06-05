package ws1415.SkatenightBackend.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Bernd Eissing
 */
public class UserGroupMembers {
    String groupName;
    private HashMap<String, ArrayList<String>> memberRights;

    public UserGroupMembers(String groupName,  HashMap<String, ArrayList<String>> memberRights){
        this.groupName = groupName;
        this.memberRights = memberRights;
    }

    public HashMap<String, ArrayList<String>> getGroupMembers() {
        return memberRights;
    }

    public String getGroupName() {
        return groupName;
    }
}
