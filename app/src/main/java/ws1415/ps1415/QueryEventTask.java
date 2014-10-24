package ws1415.ps1415;

import android.os.AsyncTask;
import android.widget.TextView;

import com.appspot.skatenight_ms.skatenightAPI.model.Event;

import java.io.IOException;

/**
 * Created by Richard on 21.10.2014.
 */
public class QueryEventTask extends AsyncTask<ShowInformationActivity, Void, Event> {
    private ShowInformationActivity view;

    /**
     * Ruft das aktuelle Event-Objekt vom Server ab und schreibt die Informationen in die
     * übergebene View.
     * @param params Die zu befüllende View
     */
    @Override
    protected Event doInBackground(ShowInformationActivity... params) {
        view = params[0];
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().getEvent().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Event e) {
        view.setEventInformation(e);
    }
}
