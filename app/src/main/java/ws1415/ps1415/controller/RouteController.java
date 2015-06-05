package ws1415.ps1415.controller;

import com.skatenight.skatenightAPI.model.Route;

import java.io.IOException;
import java.util.List;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;

/**
 * Enthält Funktionen zur Verwaltung der Routen auf dem Server.
 * @author Richard Schulze
 */
// TODO Controller testen
public abstract class RouteController {

    /**
     * Ruft die Liste aller Routen vom Server ab.
     * @param handler    Der Handler, dem die Liste der Routen übergeben wird.
     */
    public static void getRoutes(ExtendedTaskDelegate<Void, List<Route>> handler) {
        new ExtendedTask<Void, Void, List<Route>>(handler) {
            @Override
            protected List<Route> doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().routeEndpoint().getRoutes().execute().getItems();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Erstellt die übergebene Route auf dem Server.
     * @param handler    Der Handler, dem die erstellte Route inkl. ID übergeben wird.
     * @param route      Die zu erstellende Route.
     */
    public static void addRoute(ExtendedTaskDelegate<Void, Route> handler, final Route route) {
        new ExtendedTask<Void, Void, Route>(handler){
            @Override
            protected Route doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().routeEndpoint().addRoute(route).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Löscht die Route mit der angegebenen ID.
     * @param handler    Der Handler, der über den Status des Tasks informiert wird.
     * @param routeId    Die ID der zu löschenden Route.
     */
    // TODO Löschen von Routen testen, wenn abhängige Events existieren
    public static void deleteRoute(ExtendedTaskDelegate<Void, Void> handler, final long routeId) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().routeEndpoint().deleteRoute(routeId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

}
