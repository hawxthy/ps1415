package ws1415.ps1415;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.skatenight.skatenightAPI.SkatenightAPI;

/**
 * Singleton-Klasse, die den Zweck eines zentralen Zugriffspunkts auf das Backend erfüllt.
 *
 * Created by Richard on 21.10.2014.
 */
public abstract class ServiceProvider {
    private static SkatenightAPI service;

    /**
     * Gibt die aktuelle API-Instanz zurück. Falls noch keine Instanz erstellt wurde, wird eine
     * neue Verbindung zum Production-Server hergestellt.
     * @return
     */
    public static SkatenightAPI getService() {
        if (service == null) {
            SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null);
            builder.setRootUrl(Constants.API_URL);
            service = builder.build();
        }
        return service;
    }

}
