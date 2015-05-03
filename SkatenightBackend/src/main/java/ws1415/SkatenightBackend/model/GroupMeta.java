package ws1415.SkatenightBackend.model;

import com.google.appengine.datanucleus.annotations.Unowned;

import java.util.List;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by Bernd Eissing on 02.05.2015.
 */
public class GroupMeta {
    @Persistent
    @PrimaryKey
    private String name;

    @Persistent
    private String creator;

    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private List<String> members;

    public GroupMeta(String name, String creator, List<String> members){
        this.name = name;
        this.creator = creator;
        this.members = members;
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

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
