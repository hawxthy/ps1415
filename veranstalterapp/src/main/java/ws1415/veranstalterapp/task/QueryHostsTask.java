package ws1415.veranstalterapp.task;

import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Host;

import java.io.IOException;
import java.util.List;

import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.activity.PermissionManagementActivity;
import ws1415.veranstalterapp.fragment.ShowEventsFragment;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um alle Veranstalter vom Server abzurufen.
 *
 * @author Bernd Eissing
 */
public class QueryHostsTask extends AsyncTask<PermissionManagementActivity, Void, List<Host>> {
    private PermissionManagementActivity view;

    /**
     * Ruft die aktuellen Event-Objekte vom Server ab.
     *
     * @param params zu befüllende Fragment
     * @return abgerufene Event-Liste
     */
    @Override
    protected List<Host> doInBackground(PermissionManagementActivity... params) {
        view = params[0];
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().getHosts().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Übergibt die abgerufene Event-Liste an die View.
     *
     * @param results abgerufene Event-Liste
     */
    @Override
    protected void onPostExecute(List<Host> results) {
        view.setHostsToListView(results);
    }
}
