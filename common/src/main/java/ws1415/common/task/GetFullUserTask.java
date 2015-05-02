package ws1415.common.task;

import com.skatenight.skatenightAPI.model.EndUser;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Ruft einen Benutzer mit allen Informationen vom Server ab.
 *
 * @author Martin Wrodarczyk
 */
public class GetFullUserTask extends ExtendedTask<String, Void, EndUser>  {

    public GetFullUserTask(ExtendedTaskDelegate<Void, EndUser> delegate) {
        super(delegate);
    }

    @Override
    protected EndUser doInBackground(String... params) {
        String email = params[0];
        try {
            return ServiceProvider.getService().userEndpoint().getFullUser(email).execute();
        } catch (IOException e) {
            e.printStackTrace();
            publishError("Benutzer konnte nicht abgerufen werden");
            return null;
        }
    }
}
