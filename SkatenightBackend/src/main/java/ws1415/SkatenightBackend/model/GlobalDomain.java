package ws1415.SkatenightBackend.model;

import java.util.List;

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
}
