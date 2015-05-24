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
 * Created by Martin on 24.05.2015.
 */
public class UserBlobstoreHandler extends HttpServlet {
    private static final BlobstoreService blobstoreService =
            BlobstoreServiceFactory.getBlobstoreService();

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
                BlobKey oldBlobKey = endUser.getPictureBlobKey();
                endUser.setPictureBlobKey(blobKeys.get(0));
                if(oldBlobKey != null) blobstoreService.delete(oldBlobKey);
                resp.getWriter().print("true");
            } catch (Exception e) {
                resp.getWriter().print("false");
            } finally {
                pm.close();
                resp.getWriter().flush();
                resp.getWriter().close();
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BlobKey key = new BlobKey(req.getParameter("key"));
        blobstoreService.serve(key, resp);
    }

}
