package ws1415.ps1415.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.skatenight.skatenightAPI.model.BlobKey;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import ws1415.ps1415.Constants;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Utility-Klasse, die das Abrufen von Bildern aus dem Blobstore übernimmt. Die Bilder werden auf dem
 * Speicher des Geräts gecached.
 * @author Richard Schulze
 */
public class DiskCacheImageLoader {
    private static DiskCacheImageLoader instance;

    public static DiskCacheImageLoader getInstance() {
        if (instance == null) {
            synchronized (DiskCacheImageLoader.class) {
                if (instance == null) {
                    instance = new DiskCacheImageLoader();
                }
            }
        }
        return instance;
    }

    private DiskCacheImageLoader() {

    }

    /**
     * Lädt das Bild mit dem angegebenen BlobKey in der Originalgröße in die ImageView.
     * @param view       Die View, in der das Bild angeziegt wird.
     * @param blobKey    Der BlobKey des anzuzeigenden Bildes.
     */
    public void loadImage(ImageView view, BlobKey blobKey) {
        if (blobKey != null) {
            processRequest(view, DiskCache.DiskKey.createDiskKey(blobKey.getKeyString()), null);
        }
    }

    /**
     * Lädt das Bild mit dem angegebenen BlobKey in die ImageView und "cropped" es auf die angegebene Größe.
     * @param view       Die View, in der das Bild angeziegt wird.
     * @param blobKey    Der BlobKey des anzuzeigenden Bildes.
     * @param cropSize   Die Größe des auszuschneidenden Bildes.
     */
    public void loadCroppedImage(ImageView view, BlobKey blobKey, int cropSize) {
        if (blobKey != null && cropSize > 0) {
            Map<String, String> params = new HashMap<>();
            params.put("crop", Integer.toString(cropSize));
            processRequest(view, DiskCache.DiskKey.createCropKey(blobKey.getKeyString(), cropSize), params);
        }
    }

    /**
     * Lädt das Bild mit dem angegebenen BlobKey in einer skalierten Größe in die ImageView.
     * @param view       Die View, in der das Bild angeziegt wird.
     * @param blobKey    Der BlobKey des anzuzeigenden Bildes.
     * @param width      Die skalierte Breite des Bildes.
     */
    public void loadScaledImage(ImageView view, BlobKey blobKey, int width) {
        if (blobKey != null && width > 0) {
            Map<String, String> params = new HashMap<>();
            params.put("scale", Integer.toString(width));
            processRequest(view, DiskCache.DiskKey.createScaleKey(blobKey.getKeyString(), width), params);
        }
    }

    /**
     * Versucht die Bitmap mit dem angegebenen Key aus dem DiskCache zu laden. Falls es dort nicht vorhanden
     * ist, wird das Bild vom Backend abgerufen.
     * @param view      Die View, in die das Bild geladen wird.
     * @param key       Der Key des abzurufenden Bildes.
     * @param params    Die Parameter, die bei einem Cache-Miss für den Aufruf an den Server verwendet werden.
     */
    private void processRequest(final ImageView view, final DiskCache.DiskKey key, final Map<String, String> params) {
        if (key == null) {
            view.setImageBitmap(null);
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... par) {
                if (view != null) {
                    final Bitmap bitmap = DiskCache.getInstance(view.getContext()).getBitmapFromDiskCache(key);
                    if (bitmap != null) {
                        // Bild ist im Cache
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                view.setImageBitmap(bitmap);
                            }
                        });
                        view.forceLayout();
                    } else {
                        // Bild ist nicht im Cache, also abrufen
                        String url = Constants.SERVER_URL + "/images/serve?key=" + key.getBlobKey();
                        if (params != null) {
                            for (String key : params.keySet()) {
                                url += "&" + key + "=" + params.get(key);
                            }
                        }

                        HttpClient client = new DefaultHttpClient();
                        HttpGet get = new HttpGet(url);
                        HttpResponse response;
                        try {
                            response = client.execute(get);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        HttpEntity httpEntity = response.getEntity();
                        if (httpEntity.getContentType().getValue().equals("text/html")) {
                            String encoding;
                            if (httpEntity.getContentEncoding() == null) {
                                // UTF-8 verwenden, falls keine Kodierung für die Antwort übertragen wurde
                                encoding = "UTF-8";
                            } else {
                                encoding = httpEntity.getContentEncoding().getValue();
                            }
                            InputStream is;
                            try {
                                String imageUrl = EntityUtils.toString(httpEntity, encoding);
                                HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
                                conn.connect();
                                is = conn.getInputStream();
                            } catch(IOException e) {
                                throw new RuntimeException(e);
                            }

                            final Bitmap bmp = BitmapFactory.decodeStream(is);
                            DiskCache.getInstance(view.getContext()).addBitmapToCache(key, bmp);
                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    view.setImageBitmap(bmp);
                                }
                            });
                        } else {
                            ByteArrayInputStream is = null;
                            try {
                                is = new ByteArrayInputStream(EntityUtils.toByteArray(httpEntity));
                                final Bitmap bmp = BitmapFactory.decodeStream(is);
                                DiskCache.getInstance(view.getContext()).addBitmapToCache(key, bmp);
                                view.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        view.setImageBitmap(bmp);
                                    }
                                });
                            } catch(Exception ex) {
                                throw new RuntimeException(ex);
                            } finally {
                                if (is != null) {
                                    try {
                                        is.close();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }
        }.execute();
    }
}
