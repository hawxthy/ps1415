package ws1415.ps1415.task;

import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;

/**
 * Klasse welche mit SkatenightBackend kommuniziert um ein neues Event zu erstellen.
 *
 * Created by Bernd Eissing, Marting Wrodarczyk on 24.10.2014.
 */
public class CreateEventTask extends AsyncTask<Event, Void, Void> {
    /**
     * Erstellt ein neues Event auf dem Server.
     *
     * @param params Das in der Activity erstelle Event
     */
    protected Void doInBackground(Event... params){
        try{
            ServiceProvider.getService().skatenightServerEndpoint().createEvent(params[0]).execute();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
