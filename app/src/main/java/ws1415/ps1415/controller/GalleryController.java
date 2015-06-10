package ws1415.ps1415.controller;

import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.ObjectCodec;
import com.google.api.client.json.Json;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.ExpandableGalleriesWrapper;
import com.skatenight.skatenightAPI.model.Gallery;
import com.skatenight.skatenightAPI.model.GalleryContainerData;
import com.skatenight.skatenightAPI.model.GalleryMetaData;
import com.skatenight.skatenightAPI.model.JsonMap;
import com.skatenight.skatenightAPI.model.Picture;
import com.skatenight.skatenightAPI.model.PictureData;
import com.skatenight.skatenightAPI.model.PictureFilter;
import com.skatenight.skatenightAPI.model.PictureMetaData;
import com.skatenight.skatenightAPI.model.PictureMetaDataList;
import com.skatenight.skatenightAPI.model.UserGalleryContainer;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.model.PictureVisibility;
import ws1415.ps1415.model.Privilege;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;

/**
 * Ruft Methoden des GalleryEndpoints auf und führt Datei-Uploads durch.
 * @author Richard Schulze
 */
public abstract class GalleryController {

    /**
     * Ruft die Metadaten der Gallerien ab, die im angegebenen Container enthalten sind.
     * @param handler          Der Handler, der über den Status des Tasks informiert wird.
     * @param containerKind    Der Datastore-Kind des Containers.
     * @param containerId      Die ID des Containers.
     */
    public static void getGalleries(ExtendedTaskDelegate<Void, List<GalleryMetaData>> handler, final String containerKind, final long containerId) {
        new ExtendedTask<Void, Void, List<GalleryMetaData>>(handler) {
            @Override
            protected List<GalleryMetaData> doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().galleryEndpoint().getGalleries(containerKind, containerId).execute().getItems();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Ruft die Metadaten der Gallery mit der angegebenen ID ab und übergibt sie dem Handler.
     * @param handler      Der Handler, der die Gallery übergeben bekommt.
     * @param galleryId    Die ID der abzurufenden Gallery.
     */
    public static void getGalleryMetaData(ExtendedTaskDelegate<Void, GalleryMetaData> handler, final long galleryId) {
        new ExtendedTask<Void, Void, GalleryMetaData>(handler) {
            @Override
            protected GalleryMetaData doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().galleryEndpoint().getGalleryMetaData(galleryId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Erstellt die angegebene Gallery auf dem Server.
     * @param handler    Der Handler, der die erstellte Gallery übergeben bekommt.
     * @param gallery    Die zu erstellende Gallery.
     * @throws IllegalArgumentException falls die Galerie ungültig ist
     */
    public static void createGallery(ExtendedTaskDelegate<Void, Gallery> handler, final Gallery gallery)
            throws IllegalArgumentException {
        if (gallery == null || gallery.getId() != null|| gallery.getContainerId() == null
                || gallery.getContainerClass() == null || gallery.getContainerClass().isEmpty()
                || gallery.getTitle() == null || gallery.getTitle().isEmpty()) {
            throw new IllegalArgumentException("invalid gallery");
        }

        new ExtendedTask<Void, Void, Gallery>(handler) {
            @Override
            protected Gallery doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().galleryEndpoint().createGallery(gallery).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Editiert die angegebene Gallery.
     * @param handler    Der Handler, der die editierte Gallery übergeben bekommt.
     * @param gallery    Die zu editierende Gallery.
     * @throws IllegalArgumentException falls die Galerie ungültig ist
     */
    public static void editGallery(ExtendedTaskDelegate<Void, Gallery> handler, final Gallery gallery) {
        if (gallery == null || gallery.getId() == null|| gallery.getContainerId() == null
                || gallery.getContainerClass() == null || gallery.getContainerClass().isEmpty()
                || gallery.getTitle() == null || gallery.getTitle().isEmpty()) {
            throw new IllegalArgumentException("invalid gallery");
        }

        new ExtendedTask<Void, Void, Gallery>(handler) {
            @Override
            protected Gallery doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().galleryEndpoint().editGallery(gallery).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Löscht die Gallery mit der angegebenen ID.
     * @param handler      Der Handler, der über den Status des Task informiert wird.
     * @param galleryId    Die ID der zu löschenden Gallery.
     */
    public static void deleteGallery(ExtendedTaskDelegate<Void, Void> handler, final long galleryId) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().galleryEndpoint().deleteGallery(galleryId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Ruft den GalleryContainer für den Benutzer mit der angegebenen E-Mail ab.
     * @param handler    Der Handler, der über den Status des Tasks informiert wird.
     * @param mail       Die Mail des Benutzers, für den der Container abgerufen wird.
     */
    public static void getGalleryContainerForMail(ExtendedTaskDelegate<Void, UserGalleryContainer> handler, final String mail) {
        new ExtendedTask<Void, Void, UserGalleryContainer>(handler) {
            @Override
            protected UserGalleryContainer doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().galleryEndpoint().getGalleryContainerForMail(mail).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Gibt eine Liste aller Galerien zurück, denen der aufrufende Benutzer das angegebene Bild hinzufügen kann.
     * @param handler    Der Handler, dem die Liste übergeben wird.
     *
     */
    public static void getExpandableGalleries(ExtendedTaskDelegate<Void, Map<GalleryMetaData, String>> handler, final long pictureId) {
        new ExtendedTask<Void, Void, Map<GalleryMetaData, String>>(handler) {
            @Override
            protected Map<GalleryMetaData, String> doInBackground(Void... params) {
                try {
                    Map<GalleryMetaData, String> result = new HashMap<>();
                    ExpandableGalleriesWrapper wrapper = ServiceProvider.getService().galleryEndpoint().getExpandableGalleries(pictureId).execute();
                    for (int i = 0; i < wrapper.getGalleries().size(); i++) {
                        result.put(wrapper.getGalleries().get(i), wrapper.getAdditionalTitles().get(i));
                    }
                    return result;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Ruft eine Liste von PictureMetaData-Objekten ab. Diese werden zusammen mit der String-Repräsentation
     * des Cursors, der die Daten aus dem Datastore gelesen hat, zurückgegeben. Bei einem weiteren
     * Aufruf kann dieser String übergeben werden, um das abrufen der Daten an dieser Stelle fortzusetzen.
     * @param handler        Der Handler, der die abgerufenen Daten übergeben bekommt.
     * @param filter         Die Filter-Optionen, die beim Abrufen der Bilder angewandt werden.
     */
    public static void listPictures(ExtendedTaskDelegate<Void, List<PictureMetaData>> handler, final PictureFilter filter) {
        new ExtendedTask<Void, Void, List<PictureMetaData>>(handler) {
            @Override
            protected List<PictureMetaData> doInBackground(Void... params) {
                try {
                    PictureMetaDataList result = ServiceProvider.getService().galleryEndpoint().listPictures(filter).execute();
                    filter.setCursorString(result.getCursorString());
                    return result.getList();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Lädt das angegebene Bild in den Google Blobstore hoch. Der Task gibt ebenfalls das dafür
     * angelegte Picture-Objekt inklusive des BlobKeys zurück.
     * @param handler    Der Handler, der beim Abschluss des Vorgangs aufgerufen werden soll.
     * @param image      Das hochzuladende Bild.
     * @param title          Der Titel des Bildes.
     * @param description    Die Beschreibung des Bildes.
     * @param visibility     Die Sichtbarkeit des Bildes.
     * @param galleryId      Die ID der Gallery, der das Bild hinzugefügt werden soll oder null,
     *                       falls das Bild keiner Galerie hinzugefügt werden soll.
     * @throws IllegalArgumentException falls das Bild ungültig ist
     */
    public static void uploadPicture(ExtendedTaskDelegate<Void, Picture> handler, final File image,
                                     final String title, final String description,
                                     final PictureVisibility visibility, final Long galleryId)
            throws IllegalArgumentException {
        if (image == null || visibility == null || title == null || title.isEmpty()
                || description == null || description.isEmpty()) {
            throw new IllegalArgumentException("invalid picture");
        }

        new ExtendedTask<Void, Void, Picture>(handler) {
            @Override
            protected Picture doInBackground(Void... params) {
                // Picture-Objekt auf dem Server erstellen lassen
                Picture picture = null;
                try {
                    picture = ServiceProvider.getService().galleryEndpoint().createPicture(
                            title, description, visibility.name()).set("galleryId", galleryId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // POST-Anfrage für den Upload erstellen
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(picture.getUploadUrl());

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addBinaryBody("files", image);
                    builder.addTextBody("id", picture.getId().toString());
                    builder.addTextBody("class", "Picture");

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

                    String keyStrings = EntityUtils.toString(httpEntity, encoding);
                    BlobKey blobKey = new BlobKey();
                    blobKey.setKeyString(keyStrings);
                    picture.setImageBlobKey(blobKey);
                } catch(IOException ex) {
                    // Bei Fehlern versuchen das bereits erstellte Picture-Objekt zu löschen
                    try {
                        ServiceProvider.getService().galleryEndpoint().deletePicture(picture.getId()).execute();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    throw new RuntimeException(ex);
                }

                return picture;
            }
        }.execute();
    }

    /**
     * Ändert die Daten des Bildes mit der angegebenen ID.
     * @param handler        Der Handler, der über den Status des Tasks informiert wird.
     * @param pictureId      Die ID des Bildes, das geändert wird.
     * @param title          Der neue Titel des Bildes.
     * @param description    Die neue Beschreibung des Bildes.
     * @param visibility     Die neue Sichtbarkeit des Bildes.
     * @throws IllegalArgumentException falls das Bild ungültig ist
     */
    // TODO R: Testen
    public static void editPicture(ExtendedTaskDelegate<Void, Void> handler, final long pictureId,
                                   final String title, final String description, final PictureVisibility visibility) {
        if (visibility == null || title == null || title.isEmpty() || description == null || description.isEmpty()) {
            throw new IllegalArgumentException("invalid picture");
        }

        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().galleryEndpoint().editPicture(pictureId, title, description, visibility.name()).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Ruft das Bild mit der angegebenen ID ab und übergibt es dem Handler.
     * @param handler      Der Handler, der das abgerufene Bild übergeben bekommt.
     * @param pictureId    Die ID des abzurufenden Bildes.
     */
    public static void getPicture(ExtendedTaskDelegate<Void, PictureData> handler, final long pictureId) {
        new ExtendedTask<Void, Void, PictureData>(handler) {
            @Override
            protected PictureData doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().galleryEndpoint().getPicture(pictureId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Löscht das Bild mit der angegebenen ID.
     * @param handler      Der Handler, der über den Status des Task informiert wird.
     * @param pictureId    Die ID des zu löschenden Bildes.
     */
    public static void deletePicture(ExtendedTaskDelegate<Void, Void> handler, final long pictureId) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().galleryEndpoint().deletePicture(pictureId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Bewertet das Bild mit der angegebenen ID. Die Bewertung {@code rating} ist dabei die Anzahl
     * Sterne, also eine Zahl zwischen 0 und 5.
     * @param handler      Der Handler, der über den Status des Task informiert wird.
     * @param pictureId    Die ID des zu bewertenden Bildes.
     * @param rating       Die Bewertung in Sternen (zwischen 0 und 5 inklusive)
     */
    public static void ratePicture(ExtendedTaskDelegate<Void, Void> handler, final long pictureId, final int rating) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().galleryEndpoint().ratePicture(pictureId, rating).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Fügt das Bild mit der angegebenen ID der Gallery hinzu.
     * @param handler      Der Handler, der über den Status des Tasks informiert wird.
     * @param pictureId    Die ID des Bildes, das zur Gallery hinzugefügt werden soll.
     * @param galleryId    Die ID der Gallery zu der das Bild hinzugefügt wird.
     */
    public static void addPictureToGallery(ExtendedTaskDelegate<Void, Void> handler, final long pictureId, final long galleryId) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().galleryEndpoint().addPictureToGallery(pictureId, galleryId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Löscht das Bild mit der angegebenen ID aus der Gallery.
     * @param handler      Der Handler, der über den Status des Tasks informiert wird.
     * @param pictureId    Die ID des Bildes, das aus der Gallery entfernt wird.
     * @param galleryId    Die ID der Gallery aus der das Bild entfernt wird.
     */
    public static void removePictureFromGallery(ExtendedTaskDelegate<Void, Void> handler, final long pictureId, final long galleryId) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().galleryEndpoint().removePictureFromGallery(pictureId, galleryId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Gibt eine Liste der Rechte zurück, die der eingeloggte Benutzer in dem angegebenen GalleryContainer hat.
     * @param handler           Der Handler, dem die Liste übergeben wird.
     * @param containerClass    Der Datastore-Kind des Containers.
     * @param containerId       Die ID des Containers.
     */
    // TODO R: Testen
    public static void getGalleryContainer(ExtendedTaskDelegate<Void, GalleryContainerData> handler,
                                                        final String containerClass, final long containerId) {
        new ExtendedTask<Void, Void, GalleryContainerData>(handler) {
            @Override
            protected GalleryContainerData doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().galleryEndpoint().getGalleryContainer(containerClass, containerId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

}
