package ws1415.ps1415.task;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Member;

import java.io.IOException;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.ps1415.ServiceProvider;

/**
 * Created by Pascal Otto on 09.12.14.
 */
public class QueryCurrentMemberEventTask extends ExtendedTask<Void, Void, Event> {
    private String email;

    public QueryCurrentMemberEventTask(ExtendedTaskDelegate delegate, String email) {
        super(delegate);
        this.email = email;
    }

    @Override
    protected Event doInBackground(Void... params) {
        try {
            Member m = ServiceProvider.getService().skatenightServerEndpoint().getMember(email).execute();
            Event e = ServiceProvider.getService().skatenightServerEndpoint().getEvent(m.getCurrentEventId()).execute();
            return e;
        }
        catch (IOException e) {
            publishError(e.getMessage());
        }
        return null;
    }
}
