package ws1415.SkatenightBackend;

import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import ws1415.SkatenightBackend.model.BoardEntry;
import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.transport.UserGroupMetaData;
import ws1415.SkatenightBackend.model.Picture;
import ws1415.SkatenightBackend.model.PictureMetaData;
import ws1415.SkatenightBackend.model.UserGroup;

/**
 * TODO JavaDoc + von anderem Interface oder Klasse erben, damit nicht alle Methoden implementiert werden müssen
 * @author Richard Schulze
 */
public class ObjectifyStartupServlet implements Servlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        ObjectifyService.register(Event.class);
        ObjectifyService.register(Gallery.class);
        ObjectifyService.register(Picture.class);
        ObjectifyService.register(PictureMetaData.class);
        ObjectifyService.register(UserGroup.class);
        ObjectifyService.register(UserGroupMetaData.class);
        ObjectifyService.register(BoardEntry.class);
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
