package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.EventData;
import com.skatenight.skatenightAPI.model.EventFilter;
import com.skatenight.skatenightAPI.model.EventMetaData;
import com.skatenight.skatenightAPI.model.EventMetaDataList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import ws1415.common.model.EventRole;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;

/**
 * Stellt Funktionen zur Verarbeitung von Events bereit.
 * @author Richard Schulze
 */
public abstract class EventController {

    /**
     * Weißt dem Benutzer mit der übergebenen Mail-Adresse die Rolle in dem Event mit der ID
     * {@code eventId} zu.
     * @param handler     Der Handler, der über den Status des Tasks informiert wird.
     * @param eventId     Die ID des Events, in dem die Rolle zugewiesen wird.
     * @param userMail    Die Mail des Benutzers, dem die Rolle zugewiesen wird.
     * @param role        Die zuzuweisende Rolle.
     */
    public static void assignRole(ExtendedTaskDelegate<Void, Void> handler, final long eventId, final String userMail, final EventRole role) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().eventEndpoint().assignRole(eventId, userMail, role.name()).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Ruft eine Liste aller Events ab, die auf dem Server gespeichert sind. Es werden dabei nur die
     * Metadaten der Events abgerufen.
     * @param handler Der Handler, der die abgerufene Liste übergeben bekommt.
     * @param filter  Der anzuwendende Filter.
     */
    public static void listEvents(ExtendedTaskDelegate<Void, List<EventMetaData>> handler, final EventFilter filter) {
        new ExtendedTask<Void, Void, List<EventMetaData>>(handler) {
            @Override
            protected List<EventMetaData> doInBackground(Void... params) {
                try {
                    EventMetaDataList result = ServiceProvider.getService().eventEndpoint().listEvents(filter).execute();
                    filter.setCursorString(result.getCursorString());
                    return result.getList();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Ruft das Event mit der angebenen ID ab.
     * @param handler   Der Handler, der das abgerufene Event übergeben bekommt.
     * @param eventId   Die ID des abzurufenden Events.
     */
    public static void getEvent(ExtendedTaskDelegate<Void, EventData> handler, final long eventId) {
        new ExtendedTask<Void, Void, EventData>(handler) {
            @Override
            protected EventData doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().eventEndpoint().getEvent(eventId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Erstellt das angegebene Event auf dem Server.
     * @param handler    Der Handler, der das erstellte Event übergeben bekommt.
     * @param event      Das zu erstellende Event.
     */
    public static void createEvent(ExtendedTaskDelegate<Void, Event> handler, final Event event,
                                   final InputStream icon, final InputStream headerImage,
                                   final List<InputStream> images) {
        // TODO Ggf. Event auf Gültigkeit prüfen

        new ExtendedTask<Void, Void, Event>(handler) {
            @Override
            protected Event doInBackground(Void... params) {
                try {
                    Event createdEvent = ServiceProvider.getService().eventEndpoint().createEvent(event).execute();

                    // POST-Anfrage für den Upload erstellen
                    try {
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(createdEvent.getImagesUploadUrl());

                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                        builder.addPart("files", new InputStreamBody(icon, "files"));
                        builder.addPart("files", new InputStreamBody(headerImage, "files"));
                        for (InputStream is : images) {
                            builder.addPart("files", new InputStreamBody(is, "files"));
                        }
                        builder.addTextBody("id", createdEvent.getId().toString());
                        builder.addTextBody("class", "Event");

                        HttpEntity entity = builder.build();
                        post.setEntity(entity);

                        HttpResponse response = client.execute(post);
                        HttpEntity httpEntity = response.getEntity();

                        String encoding;
                        if (httpEntity.getContentEncoding() == null) {
                            // UTF-8 verwenden, falls keine Kodierung für die Antwort übertragen wurde
                            encoding = "UTF-8";
                        } else {
                            encoding = httpEntity.getContentEncoding().getValue();
                        }

                        String[] keyStrings = EntityUtils.toString(httpEntity, encoding).split("\n");
                        BlobKey blobKey = new BlobKey();
                        blobKey.setKeyString(keyStrings[0]);
                        createdEvent.setIcon(blobKey);
                        blobKey = new BlobKey();
                        blobKey.setKeyString(keyStrings[1]);
                        createdEvent.setHeaderImage(blobKey);
                        createdEvent.setImages(new LinkedList<BlobKey>());
                        for (int i = 2; i < keyStrings.length; i++) {
                            blobKey = new BlobKey();
                            blobKey.setKeyString(keyStrings[i]);
                            createdEvent.getImages().add(blobKey);
                        }
                    } catch(IOException ex) {
                        // Bei Fehlern versuchen das bereits erstellte Picture-Objekt zu löschen
                        try {
                            ServiceProvider.getService().eventEndpoint().deleteEvent(createdEvent.getId()).execute();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        throw new RuntimeException(ex);
                    }

                    return createdEvent;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Editiert das angegebene Event auf dem Server. Das Event muss dazu bereits auf dem Server
     * existieren.
     * @param handler    Der Handler, der über den Status des Tasks informiert wird.
     * @param event      Das angepasste Event.
     */
    public static void editEvent(ExtendedTaskDelegate<Void, Event> handler, final Event event) {
        new ExtendedTask<Void, Void, Event>(handler) {
            @Override
            protected Event doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().eventEndpoint().editEvent(event).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Löscht das Event mit der angegebenen ID.
     * @param handler    Der Handler, der über den Status des Task informiert wird.
     * @param eventId    Die ID des zu löschenden Events.
     */
    public static void deleteEvent(ExtendedTaskDelegate<Void, Void> handler, final long eventId) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().eventEndpoint().deleteEvent(eventId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Lässt den eingeloggten Benutzer dem Event mit der angegebenen ID beitreten.
     * @param handler    Der Handler, der über den Status des Task informiert wird.
     * @param eventId    Die ID des Events, dem beigetreten wird.
     */
    public static void joinEvent(ExtendedTaskDelegate<Void, Void> handler, final long eventId) {
        new ExtendedTask<Void, Void, Void>(handler){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().eventEndpoint().joinEvent(eventId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Lässt den eingeloggten Benutzer das Event mit der angegebenen ID verlassen.
     * @param handler    Der Handler, der über den Status des Task informiert wird.
     * @param eventId    Die ID des Events, dem beigetreten wird.
     */
    public static void leaveEvent(ExtendedTaskDelegate<Void, Void> handler, final long eventId) {
        new ExtendedTask<Void, Void, Void>(handler){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().eventEndpoint().leaveEvent(eventId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

}
