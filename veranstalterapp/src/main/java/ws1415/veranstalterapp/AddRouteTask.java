package ws1415.veranstalterapp;

import android.os.AsyncTask;

import com.appspot.skatenight_ms.skatenightAPI.model.Route;

import java.io.IOException;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um auf den Server zuzugreifen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 04.11.2014.
 */
public class AddRouteTask extends AsyncTask<Route, Void, Void> {
    @Override
    protected Void doInBackground(Route... params) {
        try{
            ServiceProvider.getService().skatenightServerEndpoint().addRoute(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
