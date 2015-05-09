package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.EventMetaData;

import java.io.IOException;
import java.util.List;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;

/**
 * Stellt Funktionen zur Verarbeitung von Events bereit.
 * @author Richard Schulze
 */
public abstract class EventController {

    /**
     * Ruft eine Liste aller Events ab, die auf dem Server gespeichert sind. Es werden dabei nur die
     * Metadaten der Events abgerufen.
     * @param handler Der Handler, der die abgerufene Liste übergeben bekommt.
     */
    public static void listEventsMetaData(ExtendedTaskDelegate<Void, List<EventMetaData>> handler) {
        new ExtendedTask<Void, Void, List<EventMetaData>>(handler) {
            @Override
            protected List<EventMetaData> doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().eventEndpoint().getEventsMetaData().execute().getItems();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Gibt das Event inklusive Metadaten und Gallery-Objekt zurück. Die Bilder der Gallerie werden
     * dabei nicht mit abgerufen.
     * @param handler       Der Handler, der das abgerufene Event übergeben bekommt.
     * @param metaDataId    Die ID der Metadaten des abzurufenden Events.
     * @param eventId       Die ID des abzurufenden Events.
     */
    public static void getEvent(ExtendedTaskDelegate<Void, Event> handler, final long metaDataId, final long eventId) {
        new ExtendedTask<Void, Void, Event>(handler) {
            @Override
            protected Event doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().eventEndpoint().getEvent(metaDataId, eventId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

}
