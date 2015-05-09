package ws1415.common.task;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.net.ServiceProvider;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um ein Event vom Server abhzurufen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 04.11.2014.
 */
public class GetEventTask extends ExtendedTask<Long, Void, Event> {

    public GetEventTask(ExtendedTaskDelegate<Void, Event> delegate) {
        super(delegate);
    }

    /**
     * Fragt das Event vom Server ab.
     *
     * @param params Das Event, das vom Server abgefragt werden soll
     */
    @Override
    protected Event doInBackground(Long... params) {
        Long keyId = params[0];
        try {
            return ServiceProvider.getService().eventEndpoint().getEvent(0l, keyId).execute();
        } catch (IOException e) {
            e.printStackTrace();
            publishError(e.getMessage());
            return null;
        }
    }
}
