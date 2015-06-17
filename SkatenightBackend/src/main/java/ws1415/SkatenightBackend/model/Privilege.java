package ws1415.SkatenightBackend.model;

/**
 * Enthält die möglichen Privilegien, die einer Event-Rolle zugeordnet werden können.
 * @author Richard Schulze
 */
public enum Privilege {
    ASSIGN_ROLE,
    EDIT_EVENT,
    DELETE_EVENT,
    ADD_GALLERY,
    EDIT_GALLERY,
    REMOVE_GALLERY,
    ADD_PICTURE,
    REMOVE_PICTURES,
    SEND_BROADCAST
}
