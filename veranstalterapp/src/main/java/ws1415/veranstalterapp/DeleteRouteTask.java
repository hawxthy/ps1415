package ws1415.veranstalterapp;

import android.os.AsyncTask;

import com.appspot.skatenight_ms.skatenightAPI.model.Route;

import java.io.IOException;
import java.util.List;

/**
 * Created by Bernd Eissing on 04.11.2014.
 */
public class DeleteRouteTask extends AsyncTask<Route, Void, Void> {

    /**
     * Löscht die Route vom Server
     * @param params Die Route die gelöscht werden soll
     */
    @Override
    protected Void doInBackground(Route... params) {
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().deleteRoute(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}