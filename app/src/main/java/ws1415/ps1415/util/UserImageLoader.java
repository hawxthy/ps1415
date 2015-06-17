package ws1415.ps1415.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.skatenight.skatenightAPI.model.BlobKey;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Der UserImageLoader wird zur Verwaltung der Benutzerbilder im MemoryCache genutzt.
 *
 * @author Martin Wrodarczyk
 */
public class UserImageLoader {
    private static UserImageLoader INSTANCE;
    private MemoryCache memoryCache = MemoryCache.getInstance();
    private Context mContext;

    private UserImageLoader(Context context){
        mContext = context.getApplicationContext();
    }

    public static UserImageLoader getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new UserImageLoader(context);
        }
        return INSTANCE;
    }

    /**
     * Setzt die Bitmap zu dem übergebenen Blobkey in die übergebene ImageView. Dabei wird vorerst
     * im MemoryCache nachgeschaut ob das Bild dort existiert. Existiert es nicht, so wird es
     * vom Blobstore runtergeladen und in das MemoryCache eingefügt. Zudem wird die Bitmap zu einem
     * Kreis geformt.
     *
     * @param blobKey Blobkey des Bildes
     * @param imageView ImageView
     */
    public void displayImage(final BlobKey blobKey, final ImageView imageView){
        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_picture);
        imageView.setImageBitmap(ImageUtil.getRoundedBitmap(bm));
        if(blobKey != null && !blobKey.getKeyString().equals("") && blobKey.getKeyString() != null) {
            Bitmap bitmap = memoryCache.get(blobKey.getKeyString());
            if (bitmap != null) {
                imageView.setImageBitmap(ImageUtil.getRoundedBitmap(bitmap));
            } else {
                UserController.getUserPicture(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                        if(bitmap != null) {
                            memoryCache.put(blobKey.getKeyString(), bitmap);
                            imageView.setImageBitmap(ImageUtil.getRoundedBitmap(bitmap));
                        }
                    }
                }, blobKey);
            }
        }
    }

    /**
     * Setzt die Bitmap zu dem übergebenen Blobkey in die übergebene ImageView. Dabei wird vorerst
     * im MemoryCache nachgeschaut ob das Bild dort existiert. Existiert es nicht, so wird es
     * vom Blobstore runtergeladen und in das MemoryCache eingefügt. Zudem wird die Bitmap zu einem
     * Kreis geformt und ein weißer Rand ergänzt.
     *
     * @param blobKey Blobkey des Bildes
     * @param imageView ImageView
     */
    public void displayImageFramed(final BlobKey blobKey, final ImageView imageView){
        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_picture);
        imageView.setImageBitmap(ImageUtil.getRoundedBitmapFramed(bm));
        if(blobKey != null && !blobKey.getKeyString().equals("") && blobKey.getKeyString() != null) {
            Bitmap bitmap = memoryCache.get(blobKey.getKeyString());
            if (bitmap != null) {
                imageView.setImageBitmap(ImageUtil.getRoundedBitmapFramed(bitmap));
            } else {
                UserController.getUserPicture(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                        if(bitmap != null) {
                            memoryCache.put(blobKey.getKeyString(), bitmap);
                            imageView.setImageBitmap(ImageUtil.getRoundedBitmapFramed(bitmap));
                        }
                    }
                }, blobKey);
            }
        }
    }

    /**
     * Leert den Cache.
     */
    public void clearCache() {
        memoryCache.clear();
    }

}
