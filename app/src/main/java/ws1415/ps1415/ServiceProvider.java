package ws1415.ps1415;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.SkatenightAPI;
import com.skatenight.skatenightAPI.SkatenightAPIRequest;

import java.io.IOException;

/**
 * Singleton-Klasse, die den Zweck eines zentralen Zugriffspunkts auf das Backend erfüllt.
 *
 * Created by Richard on 21.10.2014.
 */
public abstract class ServiceProvider {
    private static boolean testing = false;

    private static SkatenightAPI service;
    private static String email;

    /**
     * Gibt die aktuelle API-Instanz zurück. Falls noch keine Instanz erstellt wurde, wird eine
     * neue Verbindung zum Production-Server hergestellt.
     * @return
     */
    public static SkatenightAPI getService() {
        if (service == null) {
            login(null);
        }
        return service;
    }

    /**
     * Erstellt ein neues API Objekt mit den angegebenen Credentials.
     *
     * @param credential Die zu verwendenen Credentials
     */
    public static void login(GoogleAccountCredential credential) {
        testing = false;

        SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), credential);
        builder.setRootUrl(Constants.API_URL);
        service = builder.build();
        if (credential != null) {
            email = credential.getSelectedAccountName();
        } else {
            email = null;
        }
    }

    public static String getEmail(){
        return email;
    }

    /**
     * Baut eine Verbindung zum lokalen Testserver auf, der für Tests genutzt werden sollte.
     */
    public static void setupLocalTestserverConnection() {
        if (!testing) {
            testing = true;

            SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null);
            builder.setRootUrl("http://localhost:8080/_ah/api");
            service = builder.build();
        }
    }
}
