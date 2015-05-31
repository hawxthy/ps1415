package ws1415.ps1415.task;

import com.skatenight.skatenightAPI.model.Event;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;

/**
 * Klasse welche mit dem Server kommunizert um die Attribute eins bestehenden Events zu
 * verändern.
 *
 * Created by Bernd Eissing on 18.11.2014.
 */
public class EditEventTask extends ExtendedTask<Event, Void, Boolean> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public EditEventTask(ExtendedTaskDelegate<Void, Boolean> delegate) {
        super(delegate);
    }

    /**
     * Editiert das Event vom Server und speichert die geänderten Daten ab.
     *
     * @param params Das zu ändernde Event
     * @return true, falls erfolgreich geändert wurde, false sonst.
     */
    @Override
    protected Boolean doInBackground(Event... params){
        try{
            ServiceProvider.getService().eventEndpoint().editEvent(params[0]).execute();
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
}
