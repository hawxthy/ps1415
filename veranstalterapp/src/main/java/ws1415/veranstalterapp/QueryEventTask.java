package ws1415.veranstalterapp;

import android.os.AsyncTask;
import com.appspot.skatenight_ms.skatenightAPI.model.Event;

import java.io.IOException;

/**
 * Created by Richard on 21.10.2014.
 */
public class QueryEventTask extends AsyncTask<ShowInformationFragment, Void, Event> {
    private ShowInformationFragment view;

    /**
     * Ruft das aktuelle Event-Objekt vom Server ab und schreibt die Informationen in die
     * übergebenen Views.
     * @param params Die zu befüllenden Views in der Reihenfolge: Datum, Ort, Gebühr, Beschreibung
     * @return Das abgerufene Event-Objekt
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

    @Override
    protected void onPostExecute(Event e) {
        view.setEventInformation(e);
    }
}
