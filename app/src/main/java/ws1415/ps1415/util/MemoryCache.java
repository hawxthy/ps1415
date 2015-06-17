package ws1415.ps1415.util;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * MemoryCache für Bitmaps, der mit dem LRU-Verfahren arbeitet.
 *
 * Basiert auf: http://androidexample.com/Download_Images_From_Web_And_Lazy_Load_In_ListView_-_Android_Example/index.php?view=article_discription&aid=112&aaid=134
 * @author Martin Wrodarczyk
 */
public class MemoryCache {
    private static MemoryCache instance;

    private MemoryCache(){
    }

    public static MemoryCache getInstance(){
        if(instance == null){
            instance = new MemoryCache();
        }
        return instance;
    }

    private static final String TAG = "MemoryCache";

    // Cache mit LRU-Verfahren
    private Map<String, Bitmap> cache = Collections.synchronizedMap(
            new LinkedHashMap<String, Bitmap>(10,1.5f,true));

    // Zugewiesene Größe
    private long size = 0;

    // Maximale Größe des Speichers der geladenen Bitmaps in bytes
    private long limit = Runtime.getRuntime().maxMemory()/4;

    /**
     * Setzt die maximale Größe des Speichers
     * @param limit Begrenzung in bytes
     */
    public void setLimit(long limit){
        this.limit = limit;
    }

    /**
     * Gibt die Bitmap mit dem angegebenen Blobkey zurück.
     *
     * @param key Blobkey
     * @return Bitmap des Blobkeys
     */
    public Bitmap get(String key){
        try{
            if(!cache.containsKey(key)) return null;
            Log.i(TAG, "Got bitmap of: " + key);
            return cache.get(key);
        } catch (NullPointerException ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Fügt einen neuen Bitmap-Eintrag in den Cache ein.
     * @param key Blobkey zu der Bitmap
     * @param bitmap Bitmap
     */
    public void put(String key, Bitmap bitmap){
        try{
            if(cache.containsKey(key)) size -= getSizeInBytes(cache.get(key));
            cache.put(key, bitmap);
            Log.i(TAG, "Put bitmap of: " + key);
            size += getSizeInBytes(bitmap);
            checkSize();
        } catch (Throwable th){
            th.printStackTrace();
        }
    }

    /**
     * Prüft die Größe des Caches.
     */
    private void checkSize() {
        if(size>limit){

           // erster iterierte Eintrag wird der älteste zugegriffene Eintrag sein
            Iterator<Entry<String, Bitmap>> iter = cache.entrySet().iterator();

            while(iter.hasNext()){
                Entry<String, Bitmap> entry = iter.next();
                size -= getSizeInBytes(entry.getValue());
                iter.remove();
                if(size<=limit)
                    break;
            }
        }
    }

    /**
     * Leert den Cache.
     */
    public void clear() {
        try{
            cache.clear();
            size=0;
        }catch(NullPointerException ex){
            ex.printStackTrace();
        }
    }

    private long getSizeInBytes(Bitmap bitmap) {
        if(bitmap==null) return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}
