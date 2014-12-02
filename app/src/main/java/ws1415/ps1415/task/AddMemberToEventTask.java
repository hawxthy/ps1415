package ws1415.ps1415.task;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.ps1415.ServiceProvider;

/**
 * Created by Daniel on 21.11.2014.
 */
public class AddMemberToEventTask extends ExtendedTask<Void, Void, Void> {
    private Event event;
    private String email;

    public AddMemberToEventTask(ExtendedTaskDelegate delegate, Event event, String email) {
        super(delegate);
        this.event = event;
        this.email = email;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (email == null) {
            publishError("Email should not be null.");
            return null;
        }
        try {
            ServiceProvider.getService().skatenightServerEndpoint().addMemberToEvent(this.event.getKey().getId(), email).execute();
        } catch (IOException e) {
            e.printStackTrace();
            publishError(e.getMessage());
        }
        return null;
    }


}
