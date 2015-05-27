package ws1415.SkatenightBackend.gcm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Verwaltet die Registration-IDs der Benutzer für GCM.
 * @author Richard
 */
@PersistenceCapable
public class RegistrationManager {
    /**
     * Die Liste aller zurzeit registrierter Benutzer.
     */
    @Persistent(defaultFetchGroup = "true")
    private Set<String> registeredUser = new HashSet<>();
    /**
     * Speichert für jede Mail-Adresse (für jeden Benutzer) eine Liste der zugehörigen IDs.
     */
    @Persistent(defaultFetchGroup = "true")
    private Map<String, String> userIDs = new HashMap<>();

    public List<String> getRegisteredUser() {
        return new ArrayList<>(registeredUser);
    }

    /**
     * Fügt die Registration-ID zum Manager hinzu, falls diese noch nicht registriert wurde.
     * @param email Die E-Mail des Benutzers, der die ID registrieren möchte.
     * @param regid Die zu registrierende ID.
     */
    public void addRegistrationId(String email, String regid) {
        registeredUser.add(regid);
        String oldID = userIDs.get(email);
        if (oldID != null) {
            if (oldID.equals(regid)) {
                // Nichts tun, wenn ID bereits bekannt
                return;
            }
            registeredUser.remove(oldID);
        }
        userIDs.put(email, regid);
    }

    public String getUserIdByMail(String mail) {
        return userIDs.get(mail);
    }
}
