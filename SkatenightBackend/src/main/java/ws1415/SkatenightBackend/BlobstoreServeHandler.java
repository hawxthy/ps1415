package ws1415.SkatenightBackend;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ws1415.SkatenightBackend.model.BlobKeyContainer;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Stellt Daten aus dem Blobstore anhand der BlobKeys zur Verfügung.
 * @author Richard Schulze
 */
public class BlobstoreServeHandler extends HttpServlet {
    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BlobKey key = new BlobKey(req.getParameter("key"));
        blobstoreService.serve(key, resp);
    }

}
