package ws1415.common.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import ws1415.common.R;
import ws1415.common.net.Constants;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;

/**
 * Erweitert das Android-Widget ImageView um Funktionalität zum asynchronen Laden von Bildern aus
 * dem Blobstore. Während des Ladens wird ein Platzhalter-Bild angezeigt.
 * @author Richard Schulze
 */
public class BlobKeyImageView extends ImageView implements ExtendedTaskDelegate<Void, Drawable> {

    public BlobKeyImageView(Context context) {
        super(context);
    }

    public BlobKeyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlobKeyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Lädt das Bild mit dem angegebenen BlobKey herunter. Während der Ladezeit wird eine Platzhalter-
     * Grafik angezeigt.
     * @param blobKey    Der BlobKey des anzuzeigenden Bildes.
     */
    public void loadFromBlobKey(final BlobKey blobKey) {
        if (blobKey == null) {
            setImageBitmap(null);
            return;
        }

        setImageResource(R.drawable.ic_action_loading);
        Animation loading = AnimationUtils.loadAnimation(getContext(), R.anim.loading);
        startAnimation(loading);

        new ExtendedTask<Void, Void, Drawable>(this) {
            @Override
            protected Drawable doInBackground(Void... params) {

                ByteArrayInputStream is = null;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(Constants.SERVER_URL + "/images/serve?key=" + blobKey.getKeyString());
                    HttpResponse response = client.execute(get);
                    HttpEntity httpEntity = response.getEntity();

                    is = new ByteArrayInputStream(EntityUtils.toByteArray(httpEntity));
                    return Drawable.createFromStream(is, "Blobstore");
                } catch(Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        }.execute();
    }

    /**
     * Wird aufgerufen, sobald das Abrufen eines Bildes abgeschlossen wurde.
     * @param task        Der Task, der das Bild abgerufen hat.
     * @param drawable    Das Drawable-Objekt, das vom Task abgerufen wurde.
     */
    @Override
    public void taskDidFinish(ExtendedTask task, Drawable drawable) {
        clearAnimation();
        setRotation(0);
        setImageDrawable(drawable);
    }
    @Override
    public void taskDidProgress(ExtendedTask task, Void... progress) { }
    @Override
    public void taskFailed(ExtendedTask task, String message) {
        clearAnimation();
    }
}