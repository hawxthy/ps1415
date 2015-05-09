package ws1415.common.task;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um ein Event zu löschen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 18.11.2014.
 */
public class DeleteEventTask extends ExtendedTask<Event, Void, Boolean> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public DeleteEventTask(ExtendedTaskDelegate<Void, Boolean> delegate) {
        super(delegate);
    }

    /**
     * Löscht das Event vom Server
     *
     * @param params Die Route die gelöscht werden soll
     */
    @Override
    protected Boolean doInBackground(Event... params) {
        try {
            ServiceProvider.getService().eventEndpoint().deleteEvent(params[0].getId()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}