package ws1415.common.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.widget.Toast;

import com.google.api.client.util.Base64;
import com.skatenight.skatenightAPI.model.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author Richard
 */
public abstract class ImageUtil {

    /**
     * Berechnet eine passende Sample-Size für Bilder mit der in options angegebenen Auflösung und
     * der Zielgröße reqWidht.
     *
     * @param options  Die Größe und Auflösung des Bildes.
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
     * Wandelt eine Bitmap in ein byte-array und komprimiert es dabei mit dem JPEG-Format.
     *
     * @param bitmap Bitmap
     * @return Byte-Array der Bitmap
     */
    public static byte[] BitmapToByteArrayJPEG(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        return out.toByteArray();
    }

    /**
     * Wandelt eine Bitmap in ein byte-array verlustlos um.
     *
     * @param bitmap Bitmap
     * @return Byte-Array der Bitmap
     */
    public static byte[] BitmapToByteArray(Bitmap bitmap) {
        if(bitmap == null) return null;
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
    public static Text EncodeBitmapToText(Bitmap bitmap) {
        byte[] byteArray = ImageUtil.BitmapToByteArrayJPEG(bitmap);
        String encodedImage = Base64.encodeBase64String(byteArray);
        return new Text().setValue(encodedImage);
    }

    /**
     * Dekodiert einen Text in eine Bitmap mit Hilfe von Base64.
     *
     * @param text
     * @return
     */
    public static Bitmap DecodeTextToBitmap(Text text) {
        byte[] byteArray = Base64.decodeBase64(text.getValue());
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    /**
     * Erstellt aus einer Bitmap eine abgerundete Bitmap.
     * <p/>
     * Credit: http://stackoverflow.com/questions/17040475/adding-a-round-frame-circle-on-rounded-bitmap
     *
     * @param bitmap Bitmap
     * @return abgerundete Bitmap
     */
    public static Bitmap getRoundedBitmapFramed(Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final int radius = Math.min(height / 2, width / 2);

        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth() + 8, bitmap.getHeight() + 8, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle((width / 2) + 4, (height / 2) + 4, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(bitmap, 4, 4, paint);
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        canvas.drawCircle((width / 2) + 4, (height / 2) + 4, radius, paint);

        return output;
    }

    /**
     * Credit: http://evel.io/2013/07/21/rounded-avatars-in-android/
     *
     * @param bitmap Bitmap
     * @return abgerundete Bitmap
     */
    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    /**
     * Cropt das Bild auf 150x150.
     *
     * @param picUri      Uri des Bildes
     * @param context     Context
     * @param requestCode RequestCode
     */
    public static void performCrop(Uri picUri, Context context, int requestCode, Uri tempUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 150);
            cropIntent.putExtra("outputY", 150);
            cropIntent.putExtra("output", tempUri);
            cropIntent.putExtra("outputFormat", "PNG");
            cropIntent.putExtra("return-data", true);
            cropIntent.putExtra("scale", true);
            ((Activity) context).startActivityForResult(cropIntent, requestCode);
        } catch (ActivityNotFoundException anfe) {
            String errorMessage = "Your device doesn't support the crop action!";
            Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static InputStream BitmapToInputStream(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();
        return new ByteArrayInputStream(bitmapdata);
    }
}
