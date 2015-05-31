package ws1415.ps1415.task;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.io.IOException;
import java.util.List;

import ws1415.ps1415.ServiceProvider;

/**
 * Klasse welche mit dem SkatenightBackend kommuniziert um alle Nutzergruppen vom Server
 * abzurufen.
 *
 * @author Bernd Eissing
 */
public class QueryUserGroupsTask extends ExtendedTask<Void, Void, List<UserGroup>> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public QueryUserGroupsTask(ExtendedTaskDelegate<Void, List<UserGroup>> delegate) {
        super(delegate);
    }

    /**
     * Ruft die aktuellen UserGroup-Objekte vom Server ab.
     *
     * @param params Das zu befüllende Fragment
     * @return abgerufene UserGroup-Liste.
     */
    @Override
    protected List<UserGroup> doInBackground(Void... params) {
        try {
            return ServiceProvider.getService().groupEndpoint().getAllUserGroups().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
