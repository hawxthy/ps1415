package ws1415.veranstalterapp.util;

import android.graphics.BitmapFactory;

/**
 * @author Richard Schulze
 */
public abstract class ImageUtil {

    /**
     * Berechnet eine passende Sample-Size für Bilder mit der in options angegebenen Auflösung und
     * der Zielgröße reqWidht.
     *
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
}
