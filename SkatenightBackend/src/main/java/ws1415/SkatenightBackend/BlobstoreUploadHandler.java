package ws1415.SkatenightBackend;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
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

import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.model.Picture;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Nimmt POST-Anfragen für einen Upload in den Blobstore entgegen.
 * @author Richard Schulze
 */
public class BlobstoreUploadHandler extends HttpServlet {
    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Wird aufgerufen, wenn der Upload in den Blobstore fertiggestellt wurde.
     * Im Request wird die ID des Picture-Objekts erwartet, für das der Upload durchgeführt wurde.
     * Die ID wird als Parameter {@code pictureId} ausgelesen. Die Antwort, die von diesem Servlet
     * generiert wird, enthält den BlobKey des Bildes, der außerdem im Picture-Objekt im Datastore
     * gespeichert wird.
     * @param req     Die HTTP-Anfrage, die die ID des Picture-Objekts enthält.
     * @param resp    Die HTTP-Antwort, die den BlobKey enthält.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, List<BlobKey>> uploads = blobstoreService.getUploads(req);
        BlobKey blobKey = uploads.get("file").get(0);

        try {
            Picture picture = new GalleryEndpoint().getPicture(Long.parseLong(req.getParameter("pictureId")));
            picture.setImageBlobKey(blobKey);
            ofy().save().entity(picture).now();

            // BlobKey zurück an den Client senden
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().print(blobKey.getKeyString());
            resp.getWriter().flush();
            resp.getWriter().close();
        } catch(Exception ex) {
            // Im Falle eines Fehlers den gespeicherten Blob löschen
            blobstoreService.delete(blobKey);
            throw ex;
        }
    }

}
