package ws1415.common.task;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.io.IOException;
import java.util.List;

import ws1415.common.net.ServiceProvider;

/**
 * Klasse welche mit dem SkatenightBackend kommuniziert um die die eigenen Nutzergruppen und
 * beigetretene Gruppen vom Server abzurufen.
 *
 * @author Martin Wrodarczyk
 */
public class QueryMyUserGroupsTask extends ExtendedTask<Void, Void, List<UserGroup>> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public QueryMyUserGroupsTask(ExtendedTaskDelegate<Void, List<UserGroup>> delegate) {
        super(delegate);
    }

    /**
     * Ruft die aktuellen UserGroup-Objekte vom Server ab.
     *
     * @return abgerufene UserGroup-Liste.
     */
    @Override
    protected List<UserGroup> doInBackground(Void... voids) {
        try {
            return ServiceProvider.getService().groupEndpoint().fetchMyUserGroups().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
