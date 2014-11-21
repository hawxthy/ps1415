package ws1415.ps1415.task;

import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;
import ws1415.ps1415.ServiceProvider;

/**
 * Created by Daniel on 21.11.2014.
 */
public class AddEventToMemberTask extends AsyncTask<String, Void, Void> {
    private Event event;

    public AddEventToMemberTask(Event event) {
        this.event = event;
    }

    @Override
    protected Void doInBackground(String... params) {
        String email = params[0];
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().addEventToMember(this.event,email).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
