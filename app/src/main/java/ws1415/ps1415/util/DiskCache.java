package ws1415.ps1415.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ws1415.ps1415.BuildConfig;

/**
 * Stellt einen Cache auf dem Speicher des Geräts für Bitmaps zur Verfügung. Dieser Cache befindet
 * sich nicht im RAM sondern auf dem internen Speicher des Geräts, da er vor allem für größere Bilder
 * gedacht ist.
 *
 * Basiert auf: http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html#disk-cache
 * @author Richard Schulze
 */
public class DiskCache {
    private static DiskCache instance;

    public static DiskCache getInstance(Context context) {
        if (instance == null) {
            synchronized (DiskCache.class) {
                if (instance == null) {
                    instance = new DiskCache(context);
                }
            }
        }
        return instance;
    }


    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 100; // 100MB
    private static final String DISK_CACHE_SUBDIR = "skatenight";

    // Einstellungen für die Kompression der Bilder
    private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final int DISK_CACHE_INDEX = 0;

    private DiskCache(Context context) {
        // Cache initialisieren
        File cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(cacheDir);
    }

    class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            synchronized (mDiskCacheLock) {
                try {
                    File cacheDir = params[0];
                    mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
                    mDiskCacheStarting = false; // Finished initialization
                    mDiskCacheLock.notifyAll(); // Wake any waiting threads
                } catch (IOException e) {
                    throw new RuntimeException("DiskCache initialisation failed");
                }
            }
            return null;
        }
    }

    /**
     * Repräsentiert einen Key für eine Bitmap im Cache.
     */
    public static class DiskKey {
        private String blobKey;
        private String manipulation;

        private DiskKey(String blobKey, String manipulation) {
            this.blobKey = blobKey;
            this.manipulation = manipulation;
        }

        /**
         * Erstellt einen DiskKey für einen BlobKey.
         * @param blobKey         Der BlobKey des Bildes.
         * @return Der DiskKey für das Bild mit dem angegeben BlobKey.
         */
        public static DiskKey createDiskKey(String blobKey) {
            return new DiskKey(blobKey, null);
        }

        /**
         * Erstellt einen DiskKey für einen BlobKey, dessen Bild auf die angegebene Größe zugeschnitten wurde.
         * @param blobKey         Der BlobKey des Bildes.
         * @param size            Die Größe des zugeschnittenen Bildes.
         * @return Der DiskKey für das Bild mit der angegeben Manipulation.
         */
        public static DiskKey createCropKey(String blobKey, int size) {
            return new DiskKey(blobKey, "crop" + size);
        }

        /**
         * Erstellt einen DiskKey für einen BlobKey, dessen Bild auf die angegebene Größe skaliert wurde.
         * @param blobKey         Der BlobKey des Bildes.
         * @param size            Die Größe des skalierten Bildes.
         * @return Der DiskKey für das Bild mit der angegeben Manipulation.
         */
        public static DiskKey createScaleKey(String blobKey, int size) {
            return new DiskKey(blobKey, "scale" + size);
        }

        public String getBlobKey() {
            return blobKey;
        }

        public String getManipulation() {
            return manipulation;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof DiskKey) {
                DiskKey k = (DiskKey) o;
                return (blobKey == null ? k.blobKey == null : blobKey.equals(k.blobKey))
                        && (manipulation == null ? k.manipulation == null : manipulation.equals(k.manipulation));
            }
            return false;
        }

        /**
         * Wandelt diesen Key in einen Hash um, der als Dateiname genutzt werden kann.
         * @return Der Hash-Wert für diesen Key.
         */
        public String getHashForDisk() {
            String key = blobKey + manipulation;
            String cacheKey;
            try {
                final MessageDigest mDigest = MessageDigest.getInstance("MD5");
                mDigest.update(key.getBytes());
                cacheKey = bytesToHexString(mDigest.digest());
            } catch (NoSuchAlgorithmException e) {
                cacheKey = String.valueOf(key.hashCode());
            }
            return cacheKey;
        }

        private static String bytesToHexString(byte[] bytes) {
            // http://stackoverflow.com/questions/332079
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        }
    }

    /**
     * Ruft die Bitmap mit dem angegebenen Key aus dem DiskCache ab.
     * @param key    Der Key der abzurufenden Bitmap.
     * @return Die Bitmap für den angegebenen Key oder null, falls die Bitmap nicht im Cache
     * enthalten ist.
     */
    public Bitmap getBitmapFromDiskCache(DiskKey key) {
        final String keyString = key.getHashForDisk();
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(keyString);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d("Skatenight", "Disk cache hit");
                        }
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                            // Decode bitmap, but we don't want to sample so give
                            // MAX_VALUE as the target dimensions
                            bitmap = BitmapFactory.decodeFileDescriptor(fd);
                        }
                    }
                } catch (final IOException e) {
                    Log.e("Skatenight", "getBitmapFromDiskCache - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {}
                }
            }
            return bitmap;
        }
    }

    /**
     * Speichert die angegebene Bitmap unter dem Key im DiskCache.
     * @param key      Der Key zum Speichern der Bitmap.
     * @param value    Die zu speichernde Bitmap.
     */
    public void addBitmapToCache(DiskKey key, Bitmap value) {
        if (key == null || value == null) {
            return;
        }

        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String keyString = key.getHashForDisk();
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(keyString);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(keyString);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            value.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e("Skatenight", "addBitmapToCache - " + e);
                } catch (Exception e) {
                    Log.e("Skatenight", "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {}
                }
            }
        }
    }

    // -------------------- Utility-Funktionen --------------------

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

}
