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
    private ArrayList<String> list;

    public UserGroupVisibleMembers(){
        // Konstruktor für GAE
    }

    public UserGroupVisibleMembers(String groupName, ArrayList<String> list){
        this.groupName = groupName;
        this.list = list;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<String> getList() {
        return list;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }

    public void addVisibleMember(String member){
        if(list == null){
            list = new ArrayList<>();
        }
        list.add(member);
    }

    public void removeVisibleMember(String member){
        if(list != null && list.contains(member)){
            list.remove(member);
        }
    }
}
