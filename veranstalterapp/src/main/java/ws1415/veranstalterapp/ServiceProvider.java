package ws1415.veranstalterapp;

import com.appspot.skatenight_ms.skatenightAPI.SkatenightAPI;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

/**
 * Created by Bernd Eissing, Martin Wrodarczyk on 24.10.2014.
 */
public abstract class ServiceProvider {
    private static SkatenightAPI service;

    /**
     * Stellt eine Instanz der SkatenightAPI zur verfügung
     * @return die Instanz der SkatenightAPI
     */
    public static SkatenightAPI getService(){
        if(service == null){
            SkatenightAPI.Builder builder = new SkatenightAPI.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null);
            builder.setRootUrl("https://skatenight-ms.apspot.com/_ah/api");
            service = builder.build();
        }
        return service;
    }

}
