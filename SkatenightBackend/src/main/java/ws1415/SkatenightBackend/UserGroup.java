package ws1415.SkatenightBackend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    @Persistent
    private String name;            // Eindeutiger Name der Gruppe
    @Persistent
    private Member creator;
    @Persistent
    private Set<String> members = new HashSet<>();

    public UserGroup() {
        // Konstruktor für GAE
    }

    protected UserGroup(Member creator) {
        if (creator == null) {
            throw new IllegalArgumentException("creator can not be null");
        }
        this.creator = creator;
        creator.addGroup(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Member getCreator() {
        return creator;
    }

    public Set<String> getMembers() {
        return members;
    }
}
