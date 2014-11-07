package ws1415.ps1415;

import com.appspot.skatenight_ms.skatenightAPI.SkatenightAPI;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Created by Richard on 21.10.2014.
 */
public abstract class ServiceProvider {
    private static SkatenightAPI service;

    public static SkatenightAPI getService() {
        if (service == null) {
            SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null);
            builder.setRootUrl("https://skatenight-ms.appspot.com/_ah/api");
            service = builder.build();
        }
        return service;
    }

    /**
     * Erstellt ein Skatenight-API Objekt, das eine Verbindung zum Test-Server aufbaut.
     * Kann für Testklassen benutzt werden.
     */
    public static void setupTestServerConnection() {
        SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null);
        builder.setRootUrl("http://10.66.56.83:8080/_ah/api");
        service = builder.build();
    }

    /**
     * Erstellt ein Skatenight-API Objekt, das eine Verbindung zum Production-Server aufbaut.
     */
    public static void setupProductionServerConnection() {
        SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null);
        builder.setRootUrl("https://skatenight-ms.appspot.com/_ah/api");
        service = builder.build();
    }

}
