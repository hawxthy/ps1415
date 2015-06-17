package ws1415.SkatenightBackend.model;

import com.google.appengine.api.users.User;

/**
 * Kennzeichnet Entitäten, die kommentiert werden können.
 * @author Richard Schulze
 */
public interface CommentContainer {

    /**
     * Fügt den Kommentar diesem Container hinzu.
     * @param comment    Der hinzuzufügende Kommentar.
     */
    void addComment(Comment comment);

    /**
     * Entfernt den Kommentar aus diesem Container.
     * @param comment    Der zu entfernende Kommentar.
     */
    void removeComment(Comment comment);

    /**
     * Prüft, ob der angegebene Benutzer in diesem Container kommentieren darf.
     * @param user    Der zu prüfende Benutzer.
     * @return true, wenn der Benutzer kommentieren darf, sonst false.
     */
    boolean canAddComment(User user);

    /**
     * Prüft, ob der angegebene Benutzer in diesem Container Kommentare löschen darf.
     * @param user    Der zu prüfende Benutzer.
     * @return true, wenn der Benutzer Kommentare entfernen darf, sonst false.
     */
    boolean canDeleteComment(User user);
}
