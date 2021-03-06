package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ws1415.SkatenightBackend.model.Comment;
import ws1415.SkatenightBackend.model.CommentContainer;
import ws1415.SkatenightBackend.transport.CommentContainerData;
import ws1415.SkatenightBackend.transport.CommentData;
import ws1415.SkatenightBackend.transport.CommentDataList;
import ws1415.SkatenightBackend.transport.CommentFilter;
import ws1415.SkatenightBackend.transport.UserListData;
import ws1415.SkatenightBackend.transport.UserPrimaryData;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Stellt Methoden für das Kommentieren von Entitäten bereit.
 * @author Richard Schulze
 */
public class CommentEndpoint extends SkatenightServerEndpoint {
    private static final Logger log = Logger.getLogger(CommentEndpoint.class.getName());

    /**
     * Fügt den Kommentar mit angegebenem Inhalt dem Container zu, der durch die Klasse und ID identifiziert wird.
     * Der User muss im betreffenden Container Kommentare hinzufügen können.
     * @param user              Der User, der den Kommentar hinzufügen möchte.
     * @param containerClass    Der Datastore-Kind des Containers, dem der Kommentar hinzugefügt wird.
     * @param containerId       Die ID des Containers, dem der Kommentar hinzugefügt wird.
     * @param commentString     Der Kommentar als String.
     * @return Gibt den erstellten Kommentar inkl. ID zurück.
     */
    public CommentData addComment(User user, @Named("containerClass") String containerClass,@Named("containerId") long containerId,
                                  @Named("commentString") String commentString) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (containerClass == null || containerClass.isEmpty() || commentString == null || commentString.isEmpty()) {
            throw new IllegalArgumentException("invalid comment");
        }
        CommentContainer container = (CommentContainer) ofy().load().kind(containerClass).id(containerId).safe();
        if (!container.canAddComment(user)) {
            throw new OAuthRequestException("insufficient privileges");
        }
        Comment comment = new Comment();
        comment.setContainerClass(containerClass);
        comment.setContainerId(containerId);
        comment.setAuthor(user.getEmail());
        comment.setDate(new Date());
        comment.setComment(commentString);
        ofy().save().entity(comment).now();
        container.addComment(comment);
        ofy().save().entity(container).now();

        CommentData data = new CommentData(comment);
        UserPrimaryData userData = new UserEndpoint().getPrimaryData(user, user.getEmail());
        data.setVisibleAuthor(userData.getFirstName() + " " + userData.getLastName());
        return data;
    }

    /**
     * Editiert den Kommentar mit der angegebenen ID.
     * @param user             Der Benutzer, der den Kommentar editieren möchte.
     * @param commentId        Die ID des zu editierenden Kommentars.
     * @param commentString    Der neue Inhalt des Kommentars.
     */
    public void editComment(User user, @Named("commentId") long commentId, @Named("commentString") String commentString)
            throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Comment comment = ofy().load().type(Comment.class).id(commentId).safe();
        if (!comment.getAuthor().equals(user.getEmail()) && !new RoleEndpoint().isAdmin(user.getEmail()).value) {
            throw new OAuthRequestException("insufficient privileges");
        }
        comment.setComment(commentString);
        ofy().save().entity(comment).now();
    }

    /**
     * Löscht den Kommentar mit der angegebenen ID.
     * @param user         Der Benutzer, der den Kommentar löschen möchte.
     * @param commentId    Die ID des zu löschenden Kommentars.
     */
    public void deleteComment(User user, @Named("commentId") long commentId) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        Comment comment = ofy().load().type(Comment.class).id(commentId).now();
        if (comment != null) {
            CommentContainer container = (CommentContainer) ofy().load()
                    .kind(comment.getContainerClass())
                    .id(comment.getContainerId())
                    .safe();
            if (!user.getEmail().equals(comment.getAuthor()) && !container.canDeleteComment(user) && !new RoleEndpoint().isAdmin(user.getEmail()).value) {
                throw new OAuthRequestException("insufficient privileges");
            }
            container.removeComment(comment);
            ofy().save().entity(container).now();
            ofy().delete().entity(comment).now();
        }
    }

    /**
     * Ruft die Kommentare ab, die auf den angegebenen Filter passen.
     * @param user      Der Benutzer, der die Kommentare abruft.
     * @param filter    Der Filter, der zum Abrufen der Kommentare genutzt wird.
     */
    @ApiMethod(httpMethod = "POST")
    public CommentDataList listComments(User user, CommentFilter filter) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (filter == null) {
            throw new NullPointerException("no filter submitted");
        }
        if (filter.getLimit() <= 0 || filter.getContainerClass() == null || filter.getContainerClass().isEmpty()) {
            throw new IllegalArgumentException("invalid filter");
        }

        Query<Comment> q = ofy().load().type(Comment.class).limit(filter.getLimit());
        q = q.filter("containerClass", filter.getContainerClass());
        q = q.filter("containerId", filter.getContainerId());
        q = q.order("-date");
        if (filter.getCursorString() != null) {
            q = q.startAt(Cursor.fromWebSafeString(filter.getCursorString()));
        }

        // Daten werden zunächst zusätzlich in einer Map mit der Mail des Authors gespeichert,
        // um anschließend in einem Schritt die sichtbaren Benutzernamen für den aufrufenden
        // Benutzer abzurufen.
        Map<String, List<CommentData>> data = new HashMap<>();
        QueryResultIterator<Comment> commentIterator = q.iterator();
        CommentDataList result = new CommentDataList();
        result.setComments(new LinkedList<CommentData>());
        List<CommentData> comments;
        while (commentIterator.hasNext() && result.getComments().size() < filter.getLimit()) {
            CommentData comment = new CommentData(commentIterator.next());
            comments = data.get(comment.getAuthor());
            if (comments == null) {
                comments = new LinkedList<>();
                data.put(comment.getAuthor(), comments);
            }
            comments.add(comment);
            result.getComments().add(comment);
        }
        result.setCursorString(commentIterator.getCursor().toWebSafeString());

        List<UserListData> userData = new UserEndpoint().listUserInfo(user, new LinkedList<>(data.keySet()));
        String visibleName;
        for (UserListData u : userData) {
            visibleName = u.getFirstName();
            if (u.getLastName() != null && !u.getLastName().isEmpty()) {
                visibleName += " " + u.getLastName();
            }
            for (CommentData c : data.get(u.getEmail())) {
                c.setVisibleAuthor(visibleName);
            }
        }
        return result;
    }


    public CommentContainerData getCommentContainer(User user, @Named("containerClass") String containerClass,
                                                    @Named("containerId") long containerId)
            throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        CommentContainer container = (CommentContainer) ofy().load().kind(containerClass).id(containerId).safe();
        return new CommentContainerData(user, container);
    }

}
