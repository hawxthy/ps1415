package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.users.User;

import ws1415.SkatenightBackend.RoleEndpoint;
import ws1415.SkatenightBackend.model.CommentContainer;

/**
 * Enthält die Daten eines CommentContainers inkl. der Berechtigungen, die der aufrufende Benutzer
 * in diesem Container hat.
 * @author Richard Schulze
 */
public class CommentContainerData {
    private boolean canAddComment;
    private boolean canDeleteComment;

    public CommentContainerData(User user, CommentContainer container) {
        canAddComment = container.canAddComment(user);
        canDeleteComment = container.canDeleteComment(user) || new RoleEndpoint().isAdmin(user.getEmail()).value;
    }

    public boolean isCanAddComment() {
        return canAddComment;
    }

    public void setCanAddComment(boolean canAddComment) {
        this.canAddComment = canAddComment;
    }

    public boolean isCanDeleteComment() {
        return canDeleteComment;
    }

    public void setCanDeleteComment(boolean canDeleteComment) {
        this.canDeleteComment = canDeleteComment;
    }
}
