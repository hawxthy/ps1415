package ws1415.SkatenightBackend.model;

import com.google.appengine.datanucleus.annotations.Unowned;

import java.util.List;
import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Diese Klasse wird von der Usergroup Klasse benötigt um Ränge darzustellen. Es wird
 * der Name die Beschreibung und eine Liste von Rechten gespeichert. Diese müssen
 * beim instanziiren angegeben werden.
 *
 * Created by Bernd Eissing on 02.05.2015.
 */
@PersistenceCapable
public class Rank {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private String name;

    @Persistent
    private String description;

    @Persistent
    @Unowned
    private List<Right> rights;

    public Rank(String name, String description, List<Right> rights){
        this.name = name;
        this.description = description;
        this.rights = rights;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Right> getRights() {
        return rights;
    }

    public void setRights(List<Right> rights) {
        this.rights = rights;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
}
