package ws1415.ps1415.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

    public void loadImage(ImageView view, BlobKey blobKey) {
        processRequest(view, blobKey, null);
    }

    public void loadImage(ImageView view, BlobKey blobKey, int cropSize) {
        Map<String, String> params = new HashMap<>();
        params.put("crop", Integer.toString(cropSize));
        processRequest(view, blobKey, params);
    }

    private void processRequest(final ImageView view, final BlobKey blobKey, final Map<String, String> params) {
        if (blobKey == null) {
            view.setImageBitmap(null);
            return;
        }

        new ExtendedTask<Void, Void, Drawable>(new ExtendedTaskDelegateAdapter<Void, Drawable>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Drawable drawable) {
                view.setImageDrawable(drawable);
            }
        }) {
            @Override
            protected Drawable doInBackground(Void... par) {
                String url = Constants.SERVER_URL + "/images/serve?key=" + blobKey.getKeyString();
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
                    Bitmap bmp;
                    InputStream is;
                    try {
                        String imageUrl = EntityUtils.toString(httpEntity, encoding);
                        HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
                        conn.connect();
                        is = conn.getInputStream();
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }

                    bmp = BitmapFactory.decodeStream(is);
                    return new BitmapDrawable(Resources.getSystem(), bmp);
                } else {
                    ByteArrayInputStream is = null;
                    try {
                        is = new ByteArrayInputStream(EntityUtils.toByteArray(httpEntity));
                        return Drawable.createFromStream(is, "Blobstore");
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
        }.execute();
    }
}
