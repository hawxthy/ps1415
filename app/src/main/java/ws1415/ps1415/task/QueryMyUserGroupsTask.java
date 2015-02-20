package ws1415.ps1415.task;

import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.io.IOException;
import java.util.List;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.fragment.AllUsergroupsFragment;
import ws1415.ps1415.fragment.MyUsergroupsFragment;

/**
 * Klasse welche mit dem SkatenightBackend kommuniziert um die die eigenen Nutzergruppen und
 * beigetretene Gruppen vom Server abzurufen.
 *
 * @author Martin Wrodarczyk
 */
public class QueryMyUserGroupsTask extends AsyncTask<Void, Void, List<UserGroup>> {
    private MyUsergroupsFragment view;

    public QueryMyUserGroupsTask(MyUsergroupsFragment view){
        this.view = view;
    }

    /**
     * Ruft die aktuellen UserGroup-Objekte vom Server ab.
     *
     * @return abgerufene UserGroup-Liste.
     */
    @Override
    protected List<UserGroup> doInBackground(Void... voids) {
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().fetchMyUserGroups().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Übergibt die abgerufene UserGroup-Liste an die View.
     *
     * @param results abgerufene UserGroup-Liste.
     */
    @Override
    protected void onPostExecute(List<UserGroup> results) {
        view.setUserGroupsToListView(results);
    }
}
