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
 *
 * @author Richard Schulze, Martin Wrodarczyk
 */
@PersistenceCapable
public class RegistrationManager {
    /**
     * Die Liste aller zurzeit registrierter Benutzer.
     */
    @Persistent(defaultFetchGroup = "true")
    private Set<String> registeredUser = new HashSet<>();

    /**
     * Speichert für jede Mail-Adresse (für jeden Benutzer) seine zugehörige ID.
     */
    @Persistent(defaultFetchGroup = "true")
    private Map<String, String> MailToId = new HashMap<>();

    /**
     * Speichert für jede ID seine zugehörige E-Mail-Adresse (für jeden Benutzer).
     */
    @Persistent(defaultFetchGroup = "true")
    private Map<String, String> IdToMail = new HashMap<>();

    public List<String> getRegisteredUser() {
        return new ArrayList<>(registeredUser);
    }

    /**
     * Fügt die Registration-ID zum Manager hinzu, falls diese noch nicht registriert wurde.
     *
     * @param email Die E-Mail des Benutzers, der die ID registrieren möchte.
     * @param regid Die zu registrierende ID.
     */
    public void addRegistrationId(String email, String regid) {
        registeredUser.add(regid);
        String currentID = MailToId.get(email);
        if (currentID != null) {
            if(!currentID.equals(regid)) {
                registeredUser.remove(currentID);
                IdToMail.remove(currentID);
            }
        }
        String currentMail = IdToMail.get(regid);
        if(currentMail != null && !currentMail.equals(email)){
            if(!currentMail.equals(email))
                MailToId.remove(currentMail);
        }
        IdToMail.put(regid, email);
        MailToId.put(email, regid);
    }

    public String getUserIdByMail(String mail) {
        return MailToId.get(mail);
    }
}
