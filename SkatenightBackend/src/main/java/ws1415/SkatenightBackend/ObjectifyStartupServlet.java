package ws1415.SkatenightBackend;

import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import ws1415.SkatenightBackend.model.Board;
import ws1415.SkatenightBackend.model.BoardEntry;
import ws1415.SkatenightBackend.model.Comment;
import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.model.Picture;
import ws1415.SkatenightBackend.model.Route;
import ws1415.SkatenightBackend.model.UserGalleryContainer;
import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.model.UserGroupPreviewPictures;
import ws1415.SkatenightBackend.transport.UserGroupVisibleMembers;

/**
 * Registriert die Klassen, die per Objectify persistiert werden.
 * @author Richard Schulze
 */
public class ObjectifyStartupServlet implements Servlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        ObjectifyService.register(Event.class);
        ObjectifyService.register(Route.class);
        ObjectifyService.register(Gallery.class);
        ObjectifyService.register(Picture.class);
        ObjectifyService.register(UserGroup.class);
        ObjectifyService.register(BoardEntry.class);
        ObjectifyService.register(Board.class);
        ObjectifyService.register(UserGroupVisibleMembers.class);
        ObjectifyService.register(UserGroupPreviewPictures.class);
        ObjectifyService.register(UserGalleryContainer.class);
        ObjectifyService.register(Comment.class);
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
