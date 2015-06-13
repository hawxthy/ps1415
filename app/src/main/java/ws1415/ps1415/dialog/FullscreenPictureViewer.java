package ws1415.ps1415.dialog;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;

import com.skatenight.skatenightAPI.model.BlobKey;

import ws1415.ps1415.util.DiskCacheImageLoader;

/**
 * Zeigt ein Bild im Vollbildmodus an.
 * @author Richard Schulze
 */
public class FullscreenPictureViewer extends Activity {
    public static final String EXTRA_BLOB_KEY = FullscreenPictureViewer.class.getName() + ".BlobKey";

    private static final String MEMBER_BITMAP = FullscreenPictureViewer.class.getName() + ".Bitmap";

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (savedInstanceState != null && savedInstanceState.containsKey(MEMBER_BITMAP)) {
            imageView.setImageBitmap((Bitmap) savedInstanceState.getParcelable(MEMBER_BITMAP));
        } else {
            String blobKeyString;
            if (getIntent().hasExtra(EXTRA_BLOB_KEY)) {
                blobKeyString = getIntent().getStringExtra(EXTRA_BLOB_KEY);
            } else {
                throw new RuntimeException("intent has to contain a blob key");
            }

            Display defaultDisplay = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            defaultDisplay.getSize(size);
            DiskCacheImageLoader.getInstance().loadScaledImage(imageView, new BlobKey().setKeyString(blobKeyString), size.y);
        }
        setContentView(imageView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageView.getDrawable() != null) {
            outState.putParcelable(MEMBER_BITMAP, ((BitmapDrawable) imageView.getDrawable()).getBitmap());
        }
    }
}
