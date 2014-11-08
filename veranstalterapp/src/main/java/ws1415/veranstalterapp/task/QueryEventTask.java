package ws1415.veranstalterapp.task;

import android.os.AsyncTask;
import com.appspot.skatenight_ms.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.ShowInformationFragment;

/**
 * Created by Richard on 21.10.2014.
 */
public class QueryEventTask extends AsyncTask<ShowInformationFragment, Void, Event> {
    private ShowInformationFragment view;

    /**
     * Ruft das aktuelle Event-Objekt vom Server ab.
     * @param params Die zu befüllende Activity
     * @return Das abgerufene Event-Objekt.
     */
    @Override
    protected Event doInBackground(ShowInformationFragment... params) {
        view = params[0];
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().getEvent().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Übergibt das abgerufene Event-Objekt an die View.
     * @param e Das abgerufene Event-Objekt.
     */
    @Override
    protected void onPostExecute(Event e) {
        view.setEventInformation(e);
    }
}
