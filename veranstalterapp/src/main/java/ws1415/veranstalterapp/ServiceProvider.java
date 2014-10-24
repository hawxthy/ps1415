package ws1415.veranstalterapp;

import com.appspot.skatenight_ms.skatenightAPI.SkatenightAPI;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Created by Bernd Eissing, Martin Wrodarczyk on 24.10.2014.
 */
public abstract class ServiceProvider {
    private static SkatenightAPI service;

    /**
     * Stellt eine Instanz der SkatenightAPI zur verfügung
     * @return die Instanz der SkatenightAPI
     */
    public static SkatenightAPI getService(GoogleAccountCredential credential){
        if(service == null){
            SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), credential);
            builder.setRootUrl("https://skatenight-ms.apspot.com/_ah/api");
            service = builder.build();
        }
        return service;
    }

    public static SkatenightAPI getService(){
        return getService(null);
    }

    public static void login(GoogleAccountCredential credential) {
        getService(credential);
    }

}
