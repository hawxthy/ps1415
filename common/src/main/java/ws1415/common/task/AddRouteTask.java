package ws1415.common.task;

import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.Route;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um eine Route auf dem Server zu erstellen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 04.11.2014.
 */
public class AddRouteTask extends ExtendedTask<Route, Void, Void> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public AddRouteTask(ExtendedTaskDelegate<Void, Void> delegate) {
        super(delegate);
    }

    /**
     * Erstellt eine neue Route auf dem Server.
     *
     * @param params Route, die erstellt werden soll
     * @return NULL
     */
    @Override
    protected Void doInBackground(Route... params) {
        try{
            ServiceProvider.getService().routeEndpoint().addRoute(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
