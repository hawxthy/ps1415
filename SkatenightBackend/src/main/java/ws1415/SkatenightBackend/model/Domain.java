package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Key;

import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die Domain-Klasse ist dafür da, die Umgebung in die der User sich befindet zu identifizieren.
 * Dies wird genutz, um die Rolle für einen Benutzer innerhalb einer Umgebung zu ermitteln.
 */
@PersistenceCapable
public class Domain {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private List<Role> possibleRoles;

    public List<Role> getPossibleRoles() {
        return possibleRoles;
    }

    public void setPossibleRoles(List<Role> possibleRoles) {
        this.possibleRoles = possibleRoles;
    }
}
