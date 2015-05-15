package ws1415.common.controller;

import android.util.Log;

import com.skatenight.skatenightAPI.model.Gallery;
import com.skatenight.skatenightAPI.model.Picture;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;

/**
 * Ruft Methoden des GalleryEndpoints auf und führt Datei-Uploads durch.
 * @author Richard Schulze
 */
public abstract class GalleryController {

    /**
     * Lädt das angegebene Bild in den Google Blobstore hoch.
     * @param handler    Der Handler, der beim Abschluss des Vorgangs aufgerufen werden soll.
     * @param image      Das hochzuladende Bild.
     */
    public static void uploadImage(ExtendedTaskDelegate<Void, Void> handler, final File image,
                                   final String title, final String description, final Gallery gallery) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                // Picture-Objekt auf dem Server erstellen lassen
                Picture picture = null;
                try {
                    picture = ServiceProvider.getService().galleryEndpoint().createPicture(
                            title, description, gallery).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // POST-Anfrage für den Upload erstellen
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(picture.getUploadUrl());

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addBinaryBody("file", image);
                    builder.addTextBody("pictureId", picture.getId().toString());

                    HttpEntity entity = builder.build();
                    post.setEntity(entity);

                    HttpResponse response = client.execute(post);
                    HttpEntity httpEntity = response.getEntity();

                    Log.d("result", EntityUtils.toString(httpEntity));
                } catch(IOException ex) {
                    // TODO ServiceProvider.getService().galleryEndpoint().deletePicture();

                    throw new RuntimeException(ex);
                }

                return null;
            }
        }.execute();
    }

}
