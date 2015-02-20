package ws1415.veranstalterapp.task;

import android.os.AsyncTask;
import android.util.Log;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.veranstalterapp.fragment.ShowEventsFragment;
import ws1415.veranstalterapp.ServiceProvider;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um ein Event zu löschen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 18.11.2014.
 */
public class DeleteEventTask extends AsyncTask<Event, Void, Boolean> {
    private ShowEventsFragment sef;
    private Event event;

    public DeleteEventTask(ShowEventsFragment sef) {
        this.sef = sef;
    }

    /**
     * Löscht das Event vom Server
     *
     * @param params Die Route die gelöscht werden soll
     */
    @Override
    protected Boolean doInBackground(Event... params) {
        event = params[0];
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().deleteEvent(
                    params[0].getKey().getId()).execute().getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Löscht das Event aus der Liste, falls diese nicht schon einer Versanstaltung zugewiesen ist,
     * andernfalls wird eine Fehlermeldung ausgegeben.
     *
     * @param result true, bei erfolgreicher Löschung, false andernfalls
     */
    @Override
    protected void onPostExecute(Boolean result) {
        if (result == true) {
            sef.deleteEventFromList(event);
        }
    }
}