package ws1415.SkatenightBackend.transport;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;

/**
 * Created by Bernd Eissing on 27.05.2015.
 */
@Entity
public class UserGroupVisibleMembers {
    @Id
    private String groupName;
    private ArrayList<String> visibleMembers;

    public UserGroupVisibleMembers(){
        // Konstruktor für GAE
    }

    /**
     * Konstruktor für das erstellen.
     *
     * @param groupName
     * @param visibleMember
     */
    public UserGroupVisibleMembers(String groupName, String visibleMember){
        this.groupName = groupName;
        addVisibleMember(visibleMember);
    }

    /**
     * Konstruktor für den Transport.
     *
     * @param groupName
     * @param visibleMembers
     */
    public UserGroupVisibleMembers(String groupName, ArrayList<String> visibleMembers){
        this.groupName = groupName;
        this.visibleMembers = visibleMembers;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<String> getVisibleMembers() {
        return visibleMembers;
    }

    public void setVisibleMembers(ArrayList<String> visibleMembers) {
        this.visibleMembers = visibleMembers;
    }

    public void addVisibleMember(String member){
        if(visibleMembers == null){
            visibleMembers = new ArrayList<>();
        }
        visibleMembers.add(member);
    }

    public void removeVisibleMember(String member){
        if(visibleMembers != null && visibleMembers.contains(member)){
            visibleMembers.remove(member);
        }
    }
}
