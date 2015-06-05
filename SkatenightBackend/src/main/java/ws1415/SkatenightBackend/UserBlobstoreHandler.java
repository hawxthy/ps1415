package ws1415.SkatenightBackend;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ws1415.SkatenightBackend.model.EndUser;

/**
 * Dieser Handler nimmt Anfragen an den Blobstore für Benutzerbilder entgegen.
 *
 * @author Martin Wrodarczyk
 */
public class UserBlobstoreHandler extends HttpServlet {
    private static final BlobstoreService blobstoreService =
            BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Wird beim Upload eines neuen Benutzerbildes aufgerufen, dabei werden im Request die Blobs
     * und die Benutzermail entgegengenommen und als Response das Ergebnis des Uploads gesendet.
     *
     * @param req Request mit Blobs und Usermail
     * @param resp true, falls Upload erfolgreich, false andernfalls
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userMail = req.getParameter("id");

        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
        List<BlobKey> blobKeys = blobs.get("myFile");

        if (blobKeys != null && !blobKeys.isEmpty()) {
            resp.setCharacterEncoding("UTF-8");
            PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
                    "transactions-optional").getPersistenceManager();
            try {
                EndUser endUser = pm.getObjectById(EndUser.class, userMail);
                BlobKey oldBlobKey = endUser.getUserPicture();
                endUser.setUserPicture(blobKeys.get(0));
                if(oldBlobKey != null) blobstoreService.delete(oldBlobKey);
                resp.getWriter().print("true");
            } catch (Exception e) {
                resp.getWriter().print("false");
                blobstoreService.delete(blobKeys.get(0));
            } finally {
                pm.close();
                resp.getWriter().flush();
                resp.getWriter().close();
            }
        }
    }

    /**
     * Wird bei einer Anfrage eines Benutzerbild-Downloads aufgerufen.
     *
     * @param req Request mit Blobkey als String
     * @param resp Response in das die Bitmap geladen und gesendet wird
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BlobKey key = new BlobKey(req.getParameter("key"));
        blobstoreService.serve(key, resp);
    }

}
