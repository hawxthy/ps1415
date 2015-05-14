package ws1415.common.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.api.client.util.Base64;
import com.skatenight.skatenightAPI.model.Text;

import java.io.ByteArrayOutputStream;

/**
 * @author Richard
 */
public abstract class ImageUtil {

    /**
     * Berechnet eine passende Sample-Size für Bilder mit der in options angegebenen Auflösung und
     * der Zielgröße reqWidht.
     * @param options Die Größe und Auflösung des Bildes.
     * @param reqWidth Die Zielbreite
     * @return Eine passende SampleSIze, die zum Dekodieren des Bildes genutzt werden kann
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        int width = options.outWidth;
        int inSampleSize = 1;

        if (width > reqWidth) {
            final int halfWidth = width / 2;

            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Wandelt eine Bitmap in ein byte-array um.
     *
     * @param bitmap Bitmap
     * @return Byte-Array der Bitmap
     */
    public static byte[] BitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return out.toByteArray();
    }

    /**
     * Enkodiert eine Bitmap in ein Text mit Hilfe von Base64.
     *
     * @param bitmap Bitmap
     * @return Bitmap als Text
     */
    public static Text EncodeBitmapToText(Bitmap bitmap){
        byte[] byteArray = ImageUtil.BitmapToByteArray(bitmap);
        String encodedImage = Base64.encodeBase64String(byteArray);
        return new Text().setValue(encodedImage);
    }

    /**
     * Dekodiert einen Text in eine Bitmap mit Hilfe von Base64.
     * @param text
     * @return
     */
    public static Bitmap DecodeTextToBitmap(Text text){
        byte[] byteArray = Base64.decodeBase64(text.getValue());
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

}
