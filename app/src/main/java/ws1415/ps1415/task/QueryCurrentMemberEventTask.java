package ws1415.ps1415.task;

import com.skatenight.skatenightAPI.model.EventData;
import com.skatenight.skatenightAPI.model.Member;

import ws1415.ps1415.ServiceProvider;

/**
 * Created by Pascal Otto on 09.12.14.
 */
public class QueryCurrentMemberEventTask extends ExtendedTask<Void, Void, EventData> {
    private String email;

    public QueryCurrentMemberEventTask(ExtendedTaskDelegate delegate, String email) {
        super(delegate);
        this.email = email;
    }

    @Override
    protected EventData doInBackground(Void... params) {
        try {
            Member m = ServiceProvider.getService().userEndpoint().getMember(email).execute();
            // TODO: Fehler, wenn Teilnehmer zu keinem Event angemeldet ist
            EventData e = ServiceProvider.getService().eventEndpoint().getEvent(m.getCurrentEventId()).execute();
            return e;
        }
        catch (Exception e) {
            publishError(e.getMessage());
        }
        return null;
    }
}
