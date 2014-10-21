package ws1415.ps1415;

import com.appspot.myapplicationid.skatenightAPI.SkatenightAPI;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

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
            builder.setRootUrl("http://10.66.26.88:8080/_ah/api");
            service = builder.build();
        }
        return service;
    }

}
