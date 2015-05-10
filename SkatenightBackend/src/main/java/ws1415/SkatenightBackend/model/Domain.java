package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.repackaged.com.google.api.client.util.ArrayMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Die Domain-Klasse ist dafür da, die Umgebung in die der User sich befindet zu identifizieren.
 * Dies wird genutzt, um die Rolle für einen Benutzer innerhalb einer Umgebung zu ermitteln.
 *
 * @author Martin Wrodarczyk
 */
@PersistenceCapable
public class Domain {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private List<Integer> possibleRoles;
    @Persistent
    private Integer adminRole;
    @Persistent
    private Map<String, Integer> userRoles;

    public Domain() {
    }

    public Domain(List<Integer> possibleRoles, Integer adminRole) {
        this.possibleRoles = possibleRoles;
        this.adminRole = adminRole;
        this.userRoles = new ArrayMap<>();
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public List<Integer> getPossibleRoles() {
        return possibleRoles;
    }

    public void setPossibleRoles(List<Integer> possibleRoles) {
        this.possibleRoles = possibleRoles;
    }

    public Integer getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(Integer adminRole) {
        this.adminRole = adminRole;
    }

    public Map<String, Integer> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Map<String, Integer> userRoles) {
        this.userRoles = userRoles;
    }

    public Integer getRole(String userMail) {
        return userRoles.get(userMail);
    }

    public void setRole(String userMail, Integer role) {
        if(userRoles == null) userRoles = new HashMap<>();
        userRoles.put(userMail, role);
    }

    public void removeRole(String userMail){
        userRoles.remove(userMail);
    }

    public boolean existsUser(String userMail){
        if(userRoles == null) return false;
        return userRoles.containsKey(userMail);
    }
}
