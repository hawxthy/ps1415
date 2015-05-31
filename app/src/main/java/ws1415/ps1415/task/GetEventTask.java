package ws1415.ps1415.task;

import com.skatenight.skatenightAPI.model.EventData;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um ein Event vom Server abhzurufen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 04.11.2014.
 */
public class GetEventTask extends ExtendedTask<Long, Void, EventData> {

    public GetEventTask(ExtendedTaskDelegate<Void, EventData> delegate) {
        super(delegate);
    }

    /**
     * Fragt das Event vom Server ab.
     *
     * @param params Das Event, das vom Server abgefragt werden soll
     */
    @Override
    protected EventData doInBackground(Long... params) {
        Long keyId = params[0];
        try {
            return ServiceProvider.getService().eventEndpoint().getEvent(keyId).execute();
        } catch (IOException e) {
            e.printStackTrace();
            publishError(e.getMessage());
            return null;
        }
    }
}
