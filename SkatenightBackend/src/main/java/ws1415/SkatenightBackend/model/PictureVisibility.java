package ws1415.SkatenightBackend.model;

/**
 * Enthält die möglichen Sichtbarkeitseinstellungen für Bilder.
 * @author Richard Schulze
 */
public enum PictureVisibility {
    /**
     * Das Bild ist nur für den Ersteller einsehbar.
     */
    PRIVATE,
    /**
     * Das Bild ist nur für Freunde einsehbar.
     */
    FRIENDS,
    /**
     * Jeder Benutzer kann das Bild sehen und in Gallerien referenzieren.
     */
    PUBLIC
}
