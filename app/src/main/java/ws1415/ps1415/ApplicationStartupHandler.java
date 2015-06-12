package ws1415.ps1415;

import android.app.Application;

import ws1415.ps1415.util.DiskCache;

/**
 * Wird beim Start der App ausgeführt und führt wichtige Initialisierungen durch.
 * @author Richard Schulze
 */
public class ApplicationStartupHandler extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // DiskCache initialisieren, da sonst bei zu frühem Abrufen von Bildern aus dem Cache ein
        // Deadlock entstehen kann
        DiskCache.getInstance(this);
    }
}
