package ws1415.SkatenightBackend;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
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

import ws1415.SkatenightBackend.model.BlobKeyContainer;
import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.model.Picture;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Nimmt POST-Anfragen für einen Upload in den Blobstore entgegen und bietet Blobstore-Daten per GET
 * anhand des BlobKeys an.
 * @author Richard Schulze
 */
public class BlobstoreHandler extends HttpServlet {
    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private static final ImagesService imagesService = ImagesServiceFactory.getImagesService();

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
        String containerClass = req.getParameter("class");
        String containerId = req.getParameter("id");

        if (containerClass == null || containerClass.isEmpty() || containerId == null || containerId.isEmpty()) {
            throw new IllegalArgumentException("containerClass and containerId have to be specified");
        }

        Map<String, List<BlobKey>> uploads = blobstoreService.getUploads(req);
        List<BlobKey> blobKeys = uploads.get("files");

        if (!blobKeys.isEmpty()) {
            try {
                BlobKeyContainer container = (BlobKeyContainer) ofy().load().kind(containerClass).id(Long.parseLong(containerId)).safe();
                container.consumeBlobKeys(blobKeys);
                ofy().save().entity(container).now();

                // BlobKey zurück an den Client senden
                resp.setCharacterEncoding("UTF-8");
                for (BlobKey key : blobKeys.subList(0, blobKeys.size() - 1)) {
                    resp.getWriter().println(key.getKeyString());
                }
                resp.getWriter().print(blobKeys.get(blobKeys.size() - 1).getKeyString());
                resp.getWriter().flush();
                resp.getWriter().close();
            } catch(Exception ex) {
                // Im Falle eines Fehlers die gespeicherten Blobs löschen
                for (BlobKey key : blobKeys) {
                    blobstoreService.delete(key);
                }

                throw ex;
            }
        }
    }

    /**
     * TODO Kommentieren
     * @param req
     * @param resp
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BlobKey key = new BlobKey(req.getParameter("key"));

        if (req.getParameter("crop") != null) {
            // Crop
            int imageSize = Integer.parseInt(req.getParameter("crop"));
            String servingUrl = imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(key).imageSize(imageSize).crop(true));
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().print(servingUrl);
            resp.getWriter().flush();
            resp.getWriter().close();
        } else if (req.getParameter("scale") != null) {
            int size = Integer.parseInt(req.getParameter("scale"));
            String servingUrl = imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(key).imageSize(size).crop(false));
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().print(servingUrl);
            resp.getWriter().flush();
            resp.getWriter().close();
        } else {
            blobstoreService.serve(key, resp);
        }
    }

}
