package ws1415.ps1415.task;

import com.skatenight.skatenightAPI.model.Route;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;


/**
 * Klasse, welche mit SkatenightBackend kommunizert um eine Route vom Server zu löschen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 04.11.2014.
 */
public class DeleteRouteTask extends ExtendedTask<Route, Void, Boolean> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public DeleteRouteTask(ExtendedTaskDelegate<Void, Boolean> delegate) {
        super(delegate);
    }

    /**
     * Löscht die Route vom Server
     *
     * @param params Die Route die gelöscht werden soll
     */
    @Override
    protected Boolean doInBackground(Route... params) {
        try {
             return ServiceProvider.getService().routeEndpoint().deleteRoute(
                    params[0].getId()).execute().getValue();
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

}