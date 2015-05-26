package ws1415.ps1415.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.skatenight.skatenightAPI.model.BlobKey;

import ws1415.common.controller.UserController;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.util.ImageUtil;

/**
 * Der UserImageLoader wird zur Verwaltung der Benutzerbilder im MemoryCache genutzt.
 *
 * @author Martin Wrodarczyk
 */
public class UserImageLoader {
    MemoryCache memoryCache = new MemoryCache();

    private static UserImageLoader INSTANCE;

    private UserImageLoader(){
    }

    public static UserImageLoader getInstance(){
        if(INSTANCE == null){
            INSTANCE = new UserImageLoader();
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
        if(blobKey != null && !blobKey.getKeyString().equals("") && blobKey.getKeyString() != null) {
            Bitmap bitmap = memoryCache.get(blobKey.getKeyString());
            if (bitmap != null) {
                imageView.setImageBitmap(ImageUtil.getRoundedBitmap(bitmap));
            } else {
                UserController.getUserPicture(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                        memoryCache.put(blobKey.getKeyString(), bitmap);
                        imageView.setImageBitmap(ImageUtil.getRoundedBitmap(bitmap));
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
        if(blobKey != null && !blobKey.getKeyString().equals("") && blobKey.getKeyString() != null) {
            Bitmap bitmap = memoryCache.get(blobKey.getKeyString());
            if (bitmap != null) {
                imageView.setImageBitmap(ImageUtil.getRoundedBitmapFramed(bitmap));
            } else {
                UserController.getUserPicture(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                        memoryCache.put(blobKey.getKeyString(), bitmap);
                        imageView.setImageBitmap(ImageUtil.getRoundedBitmapFramed(bitmap));
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
