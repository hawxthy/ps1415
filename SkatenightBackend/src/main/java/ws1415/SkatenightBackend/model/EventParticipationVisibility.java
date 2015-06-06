package ws1415.SkatenightBackend.model;

/**
 * Enthält die möglichen Sichtbarkeitseinstellungen für den Beitritt bei einem Event.
 * @author Richard Schulze
 */
public enum EventParticipationVisibility {
    /**
     * Nur der Teilnehmer und Admins, sowie Veranstalter des entsprechenden Events können die Teilnahme sehen.
     */
    PRIVATE,
    /**
     * Zusätzlich zu PRIVATE können auch Freunde die Teilnahme sehen.
     */
    FRIENDS,
    /**
     * Jeder Benutzer kann die Teilnahme sehen.
     */
    PUBLIC
}
