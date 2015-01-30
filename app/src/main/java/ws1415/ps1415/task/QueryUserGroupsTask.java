package ws1415.ps1415.task;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.fragment.AllUsergroupsFragment;

/**
 * Created by Bernd Eissing on 30.01.2015.
 */
public class QueryUserGroupsTask extends AsyncTask<AllUsergroupsFragment, Void, List<UserGroup>> {
    private AllUsergroupsFragment view;

    /**
     * Ruft die aktuellen UserGroup-Objekte vom Server ab.
     * @param params Das zu befüllende Fragment
     * @return Die abgerufene UserGroup-Liste.
     */
    @Override
    protected List<UserGroup> doInBackground(AllUsergroupsFragment... params) {
        view = params[0];
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().getAllUserGroups().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Übergibt die abgerufene UserGroup-Liste an die View.
     * @param results Die abgerufene UserGroup-Liste.
     */
    @Override
    protected void onPostExecute(List<UserGroup> results) {
        view.setUserGroupsToListView(results);
    }
}
}
