package ws1415.SkatenightBackend;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.model.BoardEntry;
import ws1415.SkatenightBackend.model.UserGroupPreviewPictures;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Bernd Eissing on 26.05.2015.
 */
public class Upload extends HttpServlet {
    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String objectId = request.getParameter("id");
        String checkIfBlackBoard = request.getParameter("blackboard");
        String blobKeyString = request.getParameter("blobKeyString");
        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
        List<BlobKey> blobKeys = blobs.get("file");

        if (objectId == null || objectId.isEmpty()) {
            if (!blobKeys.isEmpty()) {
                BlobKey blobKey = blobKeys.get(0);
                try {
                    UserGroupPreviewPictures previews = new GroupEndpoint().getUserGroupPreviewPictures();
                    previews.addBlobKeyValue(blobKey.getKeyString());
                    if (blobKeyString != null && !blobKeyString.equals("nothingToDelete")) {
                        BlobKey ImageToDelete = new BlobKey(blobKeyString);
                        previews.removeBlobKeyValue(ImageToDelete);
                        blobstoreService.delete(ImageToDelete);
                    }
                    ofy().save().entity(previews).now();
                    // Den BlobKey für das Bild wieder an den Clienten senden, damit das Bild in der app verwendet werden kann
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().print(blobKey.getKeyString());
                    response.getWriter().flush();
                    response.getWriter().close();
                } catch (IOException e) {
                    // Beim Fehler den BlobKey löschen
                    blobstoreService.delete(blobKey);
                    e.printStackTrace();
                }
            }
        }else if(checkIfBlackBoard != null){
            if (!blobKeys.isEmpty()) {
                BlobKey blobKey = blobKeys.get(0);
                try {
                    // Den BoardEntry vom Server laden, damit der BlobKey gesetzt werden kann
                    BoardEntry boardEntry = ofy().load().type(BoardEntry.class).id(Long.parseLong(objectId)).safe();
                    boardEntry.setBlobKey(blobKey);
                    ofy().save().entities(boardEntry).now();

                    // Nun den BlobKey für das Bild wieder an den Clienten senden, damit das Bild in der app verwendet werden kann
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().print(blobKey.getKeyString());
                    response.getWriter().flush();
                    response.getWriter().close();
                } catch (IOException e) {
                    // Beim Fehler den BlobKey löschen
                    blobstoreService.delete(blobKey);
                    e.printStackTrace();
                }
            }
        } else {
            if (!blobKeys.isEmpty()) {
                BlobKey blobKey = blobKeys.get(0);
                try {
                    // Die Gruppe vom Server laden, damit der BlobKey gesetzt werden kann
                    UserGroup group = ofy().load().type(UserGroup.class).id(objectId).safe();
                    group.setBlobKey(blobKey);
                    ofy().save().entities(group).now();

                    // Nun den BlobKey für das Bild wieder an den Clienten senden, damit das Bild in der app verwendet werden kann
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().print(blobKey.getKeyString());
                    response.getWriter().flush();
                    response.getWriter().close();
                } catch (IOException e) {
                    // Beim Fehler den BlobKey löschen
                    blobstoreService.delete(blobKey);
                    e.printStackTrace();
                }
            }
        }
    }
}
