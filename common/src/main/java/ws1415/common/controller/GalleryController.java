package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Gallery;
import com.skatenight.skatenightAPI.model.GalleryViewOptions;
import com.skatenight.skatenightAPI.model.Picture;
import com.skatenight.skatenightAPI.model.PictureMetaDataList;

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

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;

/**
 * Ruft Methoden des GalleryEndpoints auf und führt Datei-Uploads durch.
 * @author Richard Schulze
 */
public abstract class GalleryController {

    /**
     * Ruft die Gallery mit der angegebenen ID ab und übergibt sie dem Handler.
     * @param handler      Der Handler, der die Gallery übergeben bekommt.
     * @param galleryId    Die ID der abzurufenden Gallery.
     */
    public static void getGallery(ExtendedTaskDelegate<Void, Gallery> handler, final long galleryId) {
        new ExtendedTask<Void, Void, Gallery>(handler) {
            @Override
            protected Gallery doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().galleryEndpoint().getGallery(galleryId).execute();
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
     */
    public static void createGallery(ExtendedTaskDelegate<Void, Gallery> handler, final Gallery gallery) {
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
     */
    public static void editGallery(ExtendedTaskDelegate<Void, Gallery> handler, final Gallery gallery) {
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
     * Ruft eine Liste von PictureMetaData-Objekten ab. Diese werden zusammen mit der String-Repräsentation
     * des Cursors, der die Daten aus dem Datastore gelesen hat, zurückgegeben. Bei einem weiteren
     * Aufruf kann dieser String übergeben werden, um das abrufen der Daten an dieser Stelle fortzusetzen.
     * @param handler        Der Handler, der die abgerufenen Daten übergeben bekommt.
     * @param viewOptions    Die Filter-Optionen, die beim Abrufen der Bilder angewandt werden.
     */
    public static void listPictures(ExtendedTaskDelegate<Void, PictureMetaDataList> handler, final GalleryViewOptions viewOptions) {
        new ExtendedTask<Void, Void, PictureMetaDataList>(handler) {
            @Override
            protected PictureMetaDataList doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().galleryEndpoint().listPictures(viewOptions).execute();
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
     */
    public static void uploadPicture(ExtendedTaskDelegate<Void, Picture> handler, final InputStream image,
                                     final String title, final String description) {
        new ExtendedTask<Void, Void, Picture>(handler) {
            @Override
            protected Picture doInBackground(Void... params) {
                // Picture-Objekt auf dem Server erstellen lassen
                Picture picture = null;
                try {
                    picture = ServiceProvider.getService().galleryEndpoint().createPicture(
                            title, description).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // POST-Anfrage für den Upload erstellen
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(picture.getUploadUrl());

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addPart("files", new InputStreamBody(image, "files"));
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
     * Ruft das Bild mit der angegebenen ID ab und übergibt es dem Handler.
     * @param handler      Der Handler, der das abgerufene Bild übergeben bekommt.
     * @param pictureId    Die ID des abzurufenden Bildes.
     */
    public static void getPicture(ExtendedTaskDelegate<Void, Picture> handler, final long pictureId) {
        new ExtendedTask<Void, Void, Picture>(handler) {
            @Override
            protected Picture doInBackground(Void... params) {
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

}
