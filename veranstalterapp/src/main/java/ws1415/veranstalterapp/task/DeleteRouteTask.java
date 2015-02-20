package ws1415.veranstalterapp.task;

import android.os.AsyncTask;
import android.widget.Toast;
import com.skatenight.skatenightAPI.model.Route;

import java.io.IOException;

import ws1415.veranstalterapp.fragment.ManageRoutesFragment;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.ServiceProvider;


/**
 * Klasse, welche mit SkatenightBackend kommunizert um eine Route vom Server zu löschen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 04.11.2014.
 */
public class DeleteRouteTask extends AsyncTask<Route, Void, Boolean> {
    private ManageRoutesFragment mrf;
    private Route route;

    public DeleteRouteTask(ManageRoutesFragment mrf){
        this.mrf = mrf;
    }

    /**
     * Löscht die Route vom Server
     *
     * @param params Die Route die gelöscht werden soll
     */
    @Override
    protected Boolean doInBackground(Route... params) {
        route = params[0];
        try {
             return ServiceProvider.getService().skatenightServerEndpoint().deleteRoute(
                    params[0].getKey().getId()).execute().getValue();
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Löscht Route aus der Liste, falls diese nicht schon einer Versanstaltung zugewiesen ist,
     * andernfalls wird eine Fehlermeldung ausgegeben.
     *
     * @param result true, bei erfolgreicher Löschung, false andernfalls
     */
    @Override
    protected void onPostExecute(Boolean result){
        if(result == true) {
            mrf.deleteRouteFromList(route);
        } else {
            Toast.makeText(mrf.getActivity(), mrf.getString(R.string.route_delete_failed), Toast.LENGTH_LONG).show();
        }
    }

}