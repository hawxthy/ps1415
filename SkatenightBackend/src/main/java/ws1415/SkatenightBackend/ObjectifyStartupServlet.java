package ws1415.SkatenightBackend;

import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.EventMetaData;
import ws1415.SkatenightBackend.model.Gallery;

/**
 * TODO JavaDoc + von anderem Interface oder Klasse erben, damit nicht alle Methoden implementiert werden müssen
 * @author Richard Schulze
 */
public class ObjectifyStartupServlet implements Servlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        ObjectifyService.register(Gallery.class);
        ObjectifyService.register(Event.class);
        ObjectifyService.register(EventMetaData.class);
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