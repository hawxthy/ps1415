package ws1415.veranstalterapp.task;

import android.app.Activity;
import android.os.AsyncTask;

import com.appspot.skatenight_ms.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.veranstalterapp.Activities.EditEventActivity;
import ws1415.veranstalterapp.Fragments.ShowInformationActivity;
import ws1415.veranstalterapp.ServiceProvider;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um auf den Server zuzugreifen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 04.11.2014.
 */
public class GetEventTask extends AsyncTask<Long, Void, Event> {
    private Activity a;
    private Event event;

    public GetEventTask(Activity a) {
        this.a = a;
    }

    /**
     * Fragt das Event vom Server ab.
     *
     * @param params Das Event, das vom Server abgefragt werden soll
     */
    @Override
    protected Event doInBackground(Long... params) {
        Long keyId = params[0];
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().getEvent(keyId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Event event) {
        if (a instanceof ShowInformationActivity) {
            ((ShowInformationActivity) a).setEventInformation(event);
        }else if(a instanceof EditEventActivity){
            ((EditEventActivity)a).setEventDataToView(event);
        }
    }
}
