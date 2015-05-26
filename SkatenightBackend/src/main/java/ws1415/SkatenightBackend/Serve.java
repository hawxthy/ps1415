package ws1415.SkatenightBackend;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Bernd Eissing on 26.05.2015.
 */
public class Serve extends HttpServlet {
    private BlobstoreService  blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        BlobKey blobKey = new BlobKey(request.getParameter("blob-key"));
        blobstoreService.serve(blobKey, response);
    }
}
