package ws1415.veranstalterapp.task;

import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.veranstalterapp.fragment.ShowEventsFragment;
import ws1415.veranstalterapp.ServiceProvider;

/**
 * Klasse welche mit dem Server kommunizert um die Attribute eins bestehenden Events zu
 * verändern.
 *
 * Created by Bernd Eissing on 18.11.2014.
 */
public class EditEventTask extends AsyncTask<Event, Void, Boolean> {
    private ShowEventsFragment sef;
    private Event event;

    /**
     * Editiert das Event vom Server und speichert die geänderten Daten ab.
     *
     * @param params Das zu ändernde Event
     * @return true, falls erfolgreich geändert wurde, false sonst.
     */
    @Override
    protected Boolean doInBackground(Event... params){
        event = params[0];
        try{
            return ServiceProvider.getService().skatenightServerEndpoint().editEvent(event).execute().getValue();
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
}
