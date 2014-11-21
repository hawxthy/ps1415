package ws1415.ps1415.task;

import android.os.AsyncTask;
import android.util.Log;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;

/**
 * Created by Daniel on 21.11.2014.
 */
public class AddMemberToEventTask extends AsyncTask<String, Void, Void> {
    private Event event;

    public AddMemberToEventTask(Event event) {
        this.event = event;
    }

    @Override
    protected Void doInBackground(String... params) {
        String email = params[0];
        try {
            ServiceProvider.getService().skatenightServerEndpoint().addMemberToEvent(this.event.getKey().getId(), email).execute();
        } catch (IOException e) {
            Log.e("ERRR", "error");
            e.printStackTrace();
        }
        return null;
    }


}
