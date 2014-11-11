package ws1415.veranstalterapp;

import com.appspot.skatenight_ms.skatenightAPI.SkatenightAPI;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Singleton-Klasse, die den Zweck eines zentralen Zugriffspunkts auf das Backend erfüllt.
 *
 * Created by Richard on 21.10.2014.
 */
public abstract class ServiceProvider {
    private static SkatenightAPI service;

    /**
     * Stellt eine Instanz der SkatenightAPI zur verfügung.
     *
     * @return die Instanz der SkatenightAPI
     * @param credential der Berechtigungsnachweis
     */
    public static SkatenightAPI getService(GoogleAccountCredential credential){
        if(service == null){
            SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), credential);
            builder.setRootUrl("https://skatenight-ms.appspot.com/_ah/api");
            service = builder.build();
        }
        return service;
    }

    /**
     * Stellt eine Instanz der SkatenightAPI zur verfügung, ohne credentials
     *
     * @return die SkatenightAPI
     */
    public static SkatenightAPI getService(){
        return getService(null);
    }

    /**
     * Loggt den Veranstalter in die Veranstalterapp ein.
     *
     * @param credential GoogleAccountCredential, das den ausgewählten Google-Account enthält.
     */
    public static void login(GoogleAccountCredential credential) {
        SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), credential);
        builder.setRootUrl("https://skatenight-ms.appspot.com/_ah/api");
        service = builder.build();
    }

    /**
     * Erstellt ein Skatenight-API Objekt, das eine Verbindung zum Test-Server aufbaut.
     * Kann für Testklassen benutzt werden.
     */
    public static void setupTestServerConnection() {
        SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null);
        builder.setRootUrl("http://10.66.20.74:8080/_ah/api");
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
