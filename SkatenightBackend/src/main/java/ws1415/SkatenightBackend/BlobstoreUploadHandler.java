package ws1415.SkatenightBackend;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Nimmt POST-Anfragen für einen Upload in den Blobstore entgegen.
 * @author Richard Schulze
 */
public class BlobstoreUploadHandler extends HttpServlet {
    private static final Logger log = Logger.getLogger(BlobstoreUploadHandler.class.getName());

    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * TODO Kommentieren
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, List<BlobKey>> uploads = blobstoreService.getUploads(req);
        log.info("[DEBUG]: " + uploads.toString());
        for (List<BlobKey> keys : uploads.values()) {
            for (BlobKey key : keys) {
                blobstoreService.delete(key);
            }
        }
    }

}
