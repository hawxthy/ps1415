package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Key;

import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Repräsentiert eine Benutzergruppe.
 * @author Richard
 */
@PersistenceCapable
public class UserGroup {
    @PrimaryKey
    private String name;

    @Persistent
    private Member creator;

    @Persistent
    private Set<Key> members;

    public UserGroup() {
        // Konstruktor für GAE
    }

    protected UserGroup(Member creator) {
        if (creator == null) {
            throw new IllegalArgumentException("creator can not be null");
        }
        this.creator = creator;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Key> getMembers() {
        return members;
    }

    public void setMembers(Set<Key> members) {
        this.members = members;
    }
}
