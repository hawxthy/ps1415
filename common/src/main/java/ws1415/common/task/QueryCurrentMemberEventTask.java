package ws1415.common.task;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Member;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.net.ServiceProvider;

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
            Member m = ServiceProvider.getService().userEndpoint().getMember(email).execute();
            // TODO: Fehler, wenn Teilnehmer zu keinem Event angemeldet ist
            Event e = ServiceProvider.getService().eventEndpoint().getEvent(0l, m.getCurrentEventId()).execute();
            return e;
        }
        catch (Exception e) {
            publishError(e.getMessage());
        }
        return null;
    }
}
