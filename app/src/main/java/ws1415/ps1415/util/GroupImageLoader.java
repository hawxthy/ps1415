package ws1415.ps1415.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.BlobKey;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Klasse zum Laden von Gruppenbildern. Bilder werden zunächste im Cache gesucht, und erst dann
 * vom Server geladen wenn diese nicht vorhanden sind. Singleton Muster.
 *
 * @author Bernd Eissing on 02.06.2015.
 */
public class GroupImageLoader {
    private static GroupImageLoader instance;

    private GroupImageLoader(){}

    public static GroupImageLoader getInstance(){
        if(instance== null){
            instance = new GroupImageLoader();
        }
        return instance;
    }

    /**
     * Prüft, ob die übergebene blobKeyValue angegeben ist, falls nicht wird ein default Bild gesetzt. Ist ein
     * Key angegeben, so wird überprüft ob sich die Bitmap dazu bereits im Cache befindet. Falls ja so wird diese
     * aus dem Cache geladen, falls nein wird das Bild also die Bitmap erneut runtergeladen.
     *
     * @param context Die View
     * @param blobKeyValue Der Wert des BlobKeys
     * @param imageView Die ImageView in die die Bitmp gesetzt werden soll.
     */
    public void setGroupImageToImageView(final Context context, final String blobKeyValue, final ImageView imageView){
        if(blobKeyValue == null || blobKeyValue.isEmpty()){
            imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_group));
        }
        // Bild im cache?
        Bitmap cachedImage = MemoryCache.getInstance().get(blobKeyValue);
        if(cachedImage != null){
            imageView.setImageBitmap(cachedImage);
        } else{
            BlobKey blobKey = new BlobKey();
            blobKey.setKeyString(blobKeyValue);
            GroupController.getInstance().loadImageForPreview(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        // setze die Bitmap in den Cache fürs nächste mal
                        MemoryCache.getInstance().put(blobKeyValue, bitmap);
                    } else {
                        Toast.makeText(context, "Didn't find any Bitmap", Toast.LENGTH_LONG).show();
                        imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_group));
                    }
                }
                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }

            }, blobKey);
        }
    }
}
