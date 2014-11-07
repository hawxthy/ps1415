package ws1415.veranstalterapp;

import android.os.AsyncTask;

import com.appspot.skatenight_ms.skatenightAPI.model.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um auf den Server zuzugreifen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 04.11.2014.
 */
public class QueryRouteTask extends AsyncTask<ManageRoutesFragment, Void, List<Route>> {
    private ManageRoutesFragment view;

    /**
     * Ruft die Liste der Routen vom Server ab und gibt diese aus
     * @return die Liste der Routen vom Server
     */
    @Override
    protected List<Route> doInBackground(ManageRoutesFragment... params){
        view =params[0];
        try{
            return ServiceProvider.getService().skatenightServerEndpoint().getRoutes().execute().getItems();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Erhält die Liste von Routen vom Server und setzt diese mit setRouteToListView(...)
     * in die ListView in ManageRoutesActivity.
     *
     * @param results Liste mit Routen vom Server
     */
    @Override
    protected void onPostExecute(List<Route> results){
        view.setRoutesToListView((ArrayList<Route>) results);
    }
}


