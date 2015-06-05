package ws1415.ps1415.task;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;
import java.util.List;

import ws1415.ps1415.ServiceProvider;

/**
 * Ruft alle Events vom Server ab.
 *
 * @author Richard Schulze
 */
public class QueryEventsTask extends ExtendedTask<Void, Void, List<Event>> {

    public QueryEventsTask(ExtendedTaskDelegate<Void, List<Event>> delegate) {
        super(delegate);
    }

    /**
     * Ruft die aktuellen Event-Objekte vom Server ab.
     *
     * @param params
     * @return abgerufene Event-Liste
     */
    @Override
    protected List<Event> doInBackground(Void... params) {
        try {
            return ServiceProvider.getService().eventEndpoint().getAllEvents().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
            publishError(e.getMessage());
            return null;
        }
    }
}
