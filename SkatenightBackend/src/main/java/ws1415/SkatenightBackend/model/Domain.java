package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Key;

import java.util.List;

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
    private List<String> adminMails;

    public Domain(){
    }

    public Domain(List<Integer> possibleRoles){
        this.possibleRoles = possibleRoles;
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

    public List<String> getAdminMails() {
        return adminMails;
    }

    public void setAdminMails(List<String> adminMails) {
        this.adminMails = adminMails;
    }

    public void addAdmin(String adminMail){
        adminMails.add(adminMail);
    }

    public void removeAdmin(String adminMail){
        if(adminMails.contains(adminMail)) adminMails.remove(adminMail);
    }
}
