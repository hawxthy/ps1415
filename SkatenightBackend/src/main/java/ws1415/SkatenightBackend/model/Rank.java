package ws1415.SkatenightBackend.model;

import java.util.List;

/**
 * Diese Klasse wird von der Usergroup Klasse benötigt um Ränge darzustellen. Es wird
 * der Name die Beschreibung und eine Liste von Rechten gespeichert. Diese müssen
 * beim instanziiren angegeben werden.
 *
 * Created by Bernd Eissing on 02.05.2015.
 */
public class Rank {
    private String name;
    private String description;
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
}
