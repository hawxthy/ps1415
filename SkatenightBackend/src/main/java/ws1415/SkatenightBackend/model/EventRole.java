package ws1415.SkatenightBackend.model;

import java.util.Arrays;
import java.util.List;

/**
 * Enthält die möglichen Event-Rollen, die von Teilnehmern eines Events angenommen werden können.
 * @author Richard Schulze
 */
public enum EventRole {
    HOST(Arrays.asList(
            Privilege.ASSIGN_ROLE,
            Privilege.EDIT_EVENT,
            Privilege.DELETE_EVENT,
            Privilege.ADD_GALLERY,
            Privilege.EDIT_GALLERY,
            Privilege.REMOVE_GALLERY,
            Privilege.ADD_PICTURE,
            Privilege.REMOVE_PICTURES
    )),
    MEDIC(Arrays.asList(
            Privilege.ADD_PICTURE,
            Privilege.REMOVE_PICTURES
    )),
    MARSHALL(Arrays.asList(
            Privilege.ADD_PICTURE,
            Privilege.REMOVE_PICTURES
    )),
    PARTICIPANT(Arrays.asList(
            Privilege.ADD_PICTURE,
            Privilege.REMOVE_PICTURES
    ));

    private List<Privilege> privileges;

    EventRole(List<Privilege> privileges) {
        this.privileges = privileges;
    }

    public boolean hasPrivilege(Privilege p) {
        if (privileges == null) {
            return false;
        }
        return privileges.contains(p);
    }
}
