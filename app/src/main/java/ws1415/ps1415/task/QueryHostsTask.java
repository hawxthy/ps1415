package ws1415.ps1415.task;

import com.skatenight.skatenightAPI.model.Host;

import java.io.IOException;
import java.util.List;

import ws1415.ps1415.ServiceProvider;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um alle Veranstalter vom Server abzurufen.
 *
 * @author Bernd Eissing
 */
public class QueryHostsTask extends ExtendedTask<Void, Void, List<Host>> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public QueryHostsTask(ExtendedTaskDelegate<Void, List<Host>> delegate) {
        super(delegate);
    }

    /**
     * Ruft die aktuellen Event-Objekte vom Server ab.
     *
     * @param params zu befüllende Fragment
     * @return abgerufene Event-Liste
     */
    @Override
    protected List<Host> doInBackground(Void... params) {
        try {
            return ServiceProvider.getService().hostEndpoint().getHosts().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
