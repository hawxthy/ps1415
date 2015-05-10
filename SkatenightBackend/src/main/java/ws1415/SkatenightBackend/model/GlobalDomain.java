package ws1415.SkatenightBackend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;

/**
 * Hilfsklasse, um einzigartiges globales Umgebungsobjekt auf dem Server zu verwalten.
 */
@PersistenceCapable
@Inheritance(customStrategy = "complete-table")
public class GlobalDomain extends Domain {
    public GlobalDomain(List<Integer> possibleRoles, Integer adminRole) {
        super(possibleRoles, adminRole);
    }

    public List<String> listAdmins(){
        List<String> admins = new ArrayList<>();
        for (Map.Entry e : getUserRoles().entrySet()){
            e.getKey();
            if((Integer)e.getValue() == getAdminRole()){
                admins.add((String)e.getKey());
            }
        }
        return admins;
    }
}
