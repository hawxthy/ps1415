package ws1415.common.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import ws1415.common.net.Constants;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

/**
 * Ruft Methoden des GalleryEndpoints auf und führt Datei-Uploads durch.
 * @author Richard Schulze
 */
public abstract class GalleryController {

    /**
     * Lädt das angegebene Bild in den Google Blobstore hoch.
     * @param handler    Der Handler, der beim Abschluss des Vorgangs aufgerufen werden soll.
     * @param imagePath  Der Pfad des hochzuladenden Bildes.
     */
    public static void uploadImage(ExtendedTaskDelegate<Void, Void> handler, final String imagePath) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                File f = new File(imagePath);

                // Upload-URL abrufen
                String uploadUrl = null;
//                try {
//                    uploadUrl = ServiceProvider.getService().galleryEndpoint().getUploadUrl().execute().getUrl();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                // POST-Anfrage für den Upload erstellen
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(uploadUrl);

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addBinaryBody("file", f);

                    HttpEntity entity = builder.build();
                    post.setEntity(entity);

                    HttpResponse response = client.execute(post);
                    HttpEntity httpEntity = response.getEntity();

                    Log.d("result", EntityUtils.toString(httpEntity));
                } catch(IOException ex) {
                    ex.printStackTrace();
                }

                return null;
            }
        }.execute();
    }

}
