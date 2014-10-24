package ws1415.veranstalterapp;

import android.os.AsyncTask;
import com.appspot.skatenight_ms.skatenightAPI.model.Event;
import java.io.IOException;

/**
 * Created by Bernd Eissing, Marting Wrodarczyk on 24.10.2014.
 */
public class CreateEventTask extends AsyncTask<Event, Void, Void> {
    /**
     * Schreibt die neuen Informationen in das Event.
     * @param params Das in der Activity erstelle Event
     */
    protected Void doInBackground(Event... params){
        try{
            ServiceProvider.getService().skatenightServerEndpoint().setEvent(params[0]);
        }catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
