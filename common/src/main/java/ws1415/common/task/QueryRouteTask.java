package ws1415.common.task;

import com.skatenight.skatenightAPI.model.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ws1415.common.net.ServiceProvider;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um alle Routen vom Server abzurufen.
 *
 * @author Bernd Eissing, Martin Wrodarczyk
 */
public class QueryRouteTask extends ExtendedTask<Void, Void, List<Route>> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public QueryRouteTask(ExtendedTaskDelegate<Void, List<Route>> delegate) {
        super(delegate);
    }

    /**
     * Ruft die Liste der Routen vom Server ab und gibt diese aus.
     *
     * @return Liste der Routen vom Server
     */
    @Override
    protected List<Route> doInBackground(Void... params){
        try{
            return ServiceProvider.getService().routeEndpoint().getRoutes().execute().getItems();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}


