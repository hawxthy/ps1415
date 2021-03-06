package ws1415.ps1415.controller;

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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.model.EventParticipationVisibility;
import ws1415.ps1415.model.EventRole;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;

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
                    e.printStackTrace();
                    publishError("Rolle konnte nicht zugewiesen werden. Zu wenig Rechte oder letzter Veranstalter?");
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
                    e.printStackTrace();
                    publishError("Verbindung zum Server konnte nicht hergestellt werden");
                }
                return null;
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
                    e.printStackTrace();
                    publishError("Die gewählte Veranstaltung existiert nicht mehr.");
                }
                return null;
            }
        }.execute();
    }

    /**
     * Erstellt das Event mit den angegebenen Daten auf dem Server und lädt die Dateien in den Blobstore hoch.
     * @param handler    Der Handler, der das erstellte Event übergeben bekommt.
     * @param event      Das zu erstellende Event.
     * @param icon           Die Datei, die das Bild des Icons enthält.
     * @param headerImage    Die Datei, die das Header-Bild enthält.
     * @param images         Die Dateien für weitere Bilder.
     * @throws IllegalArgumentException falls das Event ungültig ist
     */
    public static void createEvent(ExtendedTaskDelegate<Void, Event> handler, final Event event,
                                   final File icon, final File headerImage, final List<File> images)
            throws IllegalArgumentException {
        if (event == null || event.getId() != null || event.getDate() == null
                || event.getFee() == null || event.getFee() < 0
                || event.getMeetingPlace() == null || event.getMeetingPlace().isEmpty()
                || event.getTitle() == null || event.getTitle().isEmpty()
                || event.getRoute() == null || icon == null) {
            throw new IllegalArgumentException("invalid event");
        }

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
                        builder.addBinaryBody("files", icon);
                        if (headerImage != null) {
                            builder.addBinaryBody("files", headerImage);
                        }
                        if (images != null) {
                            if (headerImage == null) {
                                builder.addTextBody("nulls", "1");
                            }

                            for (File f : images) {
                                builder.addBinaryBody("files", f);
                            }
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
                        if (keyStrings.length > 1 && keyStrings[1] != null && !keyStrings[1].isEmpty()) {
                            blobKey = new BlobKey();
                            blobKey.setKeyString(keyStrings[1]);
                            createdEvent.setHeaderImage(blobKey);
                        }
                        if (keyStrings.length > 2) {
                            createdEvent.setImages(new LinkedList<BlobKey>());
                            for (int i = 2; i < keyStrings.length; i++) {
                                if (keyStrings[i] != null && !keyStrings[i].isEmpty()) {
                                    blobKey = new BlobKey();
                                    blobKey.setKeyString(keyStrings[i]);
                                    createdEvent.getImages().add(blobKey);
                                }
                            }
                        }
                    } catch(IOException ex) {
                        // Bei Fehlern versuchen das bereits erstellte Event-Objekt zu löschen
                        try {
                            ServiceProvider.getService().eventEndpoint().deleteEvent(createdEvent.getId()).execute();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        throw new RuntimeException(ex);
                    }

                    return createdEvent;
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Veranstaltung konnte nicht erstellt werden. Zu wenig Rechte?");
                }
                return null;
            }
        }.execute();
    }

    /**
     * Editiert das angegebene Event auf dem Server. Das Event muss dazu bereits auf dem Server
     * existieren.
     * @param handler    Der Handler, der über den Status des Tasks informiert wird.
     * @param event      Das angepasste Event.
     * @throws IllegalArgumentException falls das Event ungültig ist
     */
    public static void editEvent(ExtendedTaskDelegate<Void, Event> handler, final Event event) throws IllegalArgumentException {
        if (event == null || event.getId() == null || event.getDate() == null
                || event.getFee() == null || event.getFee() < 0
                || event.getMeetingPlace() == null || event.getMeetingPlace().isEmpty()
                || event.getTitle() == null || event.getTitle().isEmpty()
                || event.getRoute() == null) {
            throw new IllegalArgumentException("invalid event");
        }

        new ExtendedTask<Void, Void, Event>(handler) {
            @Override
            protected Event doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().eventEndpoint().editEvent(event).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Veranstaltung konnte nicht bearbeitet werden. Zu wenig Rechte?");
                }
                return null;
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
                    e.printStackTrace();
                    publishError("Veranstaltung konnte nicht gelöscht werden. Zu wenig Rechte?");
                }
                return null;
            }
        }.execute();
    }

    /**
     * Lässt den eingeloggten Benutzer dem Event mit der angegebenen ID beitreten.
     * @param handler    Der Handler, der über den Status des Task informiert wird. Ihm wird die
     *                   Rolle übergeben, die dem Teilnehmer zugeordnet wurde.
     * @param eventId    Die ID des Events, dem beigetreten wird.
     * @param visibility Die Sichtbarkeit für die Teilnahme an dem Event.
     */
    public static void joinEvent(ExtendedTaskDelegate<Void, EventRole> handler, final long eventId,
                                 final EventParticipationVisibility visibility) {
        new ExtendedTask<Void, Void, EventRole>(handler){
            @Override
            protected EventRole doInBackground(Void... params) {
                try {
                    return EventRole.valueOf(ServiceProvider.getService().eventEndpoint().joinEvent(eventId, visibility.name()).execute().getString());
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Verbindung zum Server konnte nicht hergestellt werden");
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
                    e.printStackTrace();
                    publishError("Verbindung zum Server konnte nicht hergestellt werden");
                }
                return null;
            }
        }.execute();
    }

    /**
     * Ändert die Sichtbarkeit des Teilnehmers für das Event mit der angegebenen ID.
     * @param handler       Der Handler, der über den Status des Task informiert wird.
     * @param eventId       Die ID des Events, für das die Sichtbarkeit geändert wird.
     * @param visibility    Die neue Sichtbarkeit.
     */
    public static void changeParticipationVisibility(ExtendedTaskDelegate<Void, Void> handler, final long eventId,
                                                     final EventParticipationVisibility visibility) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().eventEndpoint().changeParticipationVisibility(eventId, visibility.name()).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Verbindung zum Server konnte nicht hergestellt werden");
                }
                return null;
            }
        }.execute();
    }

    /**
     * Sendet die Durchsage an die gewählte Rolle in der Veranstaltung mit der angegebenen ID.
     * @param handler    Der Handler, der über den Status des Task informiert wird.
     * @param eventId    Die ID des Events, an das die Durchsage gesendet wird.
     * @param role       Die Rolle, die die Durchsage empfängt.
     * @param title      Der Titel der Durchsage.
     * @param content    Die Nachricht der Durchsage.
     */
    public static void sendBroadcast(ExtendedTaskDelegate<Void, Void> handler, final long eventId, final EventRole role, final String title, final String content) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().eventEndpoint().sendBroadcast(eventId, role.name(), title, content).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Die Durchsage konnte nicht gesendet werden");
                }
                return null;
            }
        }.execute();
    }

}
