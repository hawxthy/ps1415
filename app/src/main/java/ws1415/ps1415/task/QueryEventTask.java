package ws1415.ps1415.task;

import android.os.AsyncTask;
import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;
import java.util.List;

import ws1415.ps1415.Activities.ShowEventsActivity;
import ws1415.ps1415.ServiceProvider;

/**
 * Created by Richard on 21.10.2014.
 */
public class QueryEventTask extends AsyncTask<ShowEventsActivity, Void, List<Event>> {
    private ShowEventsActivity view;

    /**
     * Ruft die aktuellen Event-Objekte vom Server ab.
     * @param params Das zu befüllende Fragment
     * @return Die abgerufene Event-Liste.
     */
    @Override
    protected List<Event> doInBackground(ShowEventsActivity... params) {
        view = params[0];
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().getAllEvents().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Übergibt die abgerufene Event-Liste an die View.
     * @param results Die abgerufene Event-Liste.
     */
    @Override
    protected void onPostExecute(List<Event> results) {
        view.setEventsToListView(results);
    }
}

