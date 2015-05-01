package ws1415.common.task;

import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Klasse welche mit SkatenightBackend kommuniziert um ein neues Event zu erstellen.
 *
 * Created by Bernd Eissing, Marting Wrodarczyk on 24.10.2014.
 */
public class CreateEventTask extends ExtendedTask<Event, Void, Void> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public CreateEventTask(ExtendedTaskDelegate<Void, Void> delegate) {
        super(delegate);
    }

    /**
     * Erstellt ein neues Event auf dem Server.
     *
     * @param params Das in der Activity erstelle Event
     */
    protected Void doInBackground(Event... params){
        try{
            ServiceProvider.getService().eventEndpoint().createEvent(params[0]).execute();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
