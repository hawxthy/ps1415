package ws1415.common.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserListData;
import com.skatenight.skatenightAPI.model.UserPrimaryData;
import com.skatenight.skatenightAPI.model.UserProfile;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ws1415.common.model.Visibility;
import ws1415.common.net.Constants;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;

/**
 * Der UserController steuert den Datenfluss zwischen den Benutzerdaten und den View-Komponenten.
 *
 * @author Martin Wrodarczyk
 */
public abstract class UserController {
    /**
     * Erstellt einen Benutzer auf dem Server, falls noch keiner zu der angegebenen E-Mail Adresse
     * existiert.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void createUser(ExtendedTaskDelegate handler, String userMail, final String firstName, final String lastName) {
        new ExtendedTask<String, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().createUser(params[0])
                            .set("firstName", firstName)
                            .set("lastName", lastName)
                            .execute().getValue();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzer konnte nicht erstellt werden");
                    return null;
                }
            }
        }.execute(userMail);
    }

    /**
     * Prüft ob ein Benutzer mit der E-Mail Adresse existiert.
     *
     * @param handler
     * @param userMail E-Mail Adresse
     */
    public static void existsUser(ExtendedTaskDelegate handler, String userMail) {
        new ExtendedTask<String, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().existsUser(params[0]).execute().getValue();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Verbindung zum Server konnte nicht hergestellt werden");
                    return null;
                }
            }
        }.execute(userMail);
    }

    /**
     * Gibt den Benutzer mit allen Informationen zu der angegebenen E-Mail Adresse aus.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void getFullUser(ExtendedTaskDelegate handler, String userMail) {
        new ExtendedTask<String, Void, EndUser>(handler) {
            @Override
            protected EndUser doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().getFullUser(params[0]).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzer konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(userMail);
    }

    /**
     * Gibt die eigenen nötigsten Benutzerinformationen aus.
     *
     * @param handler
     */
    public static void getPrimaryData(ExtendedTaskDelegate handler){
        new ExtendedTask<Void, Void, UserPrimaryData>(handler){
            @Override
            protected UserPrimaryData doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().getPrimaryData().execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzer konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Gibt das Benutzerprofil aus.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void getUserProfile(ExtendedTaskDelegate handler, final String userMail) {
        new ExtendedTask<Void, Void, UserProfile>(handler) {
            @Override
            protected UserProfile doInBackground(Void... voids) {
                try {
                    return ServiceProvider.getService().userEndpoint().getUserProfile(userMail).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzerprofil konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Aktualisiert das Profilbild eines Benutzers mit Hilfe des Blobstores.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     * @param image Neues Profilbild
     */
    public static void uploadUserPicture(ExtendedTaskDelegate handler, final String userMail,
                                         final InputStream image) {
        new ExtendedTask<Void, Void, Boolean>(handler){
            @Override
            protected Boolean doInBackground(Void... params) {
                String uploadUrl;
                try {
                    uploadUrl = ServiceProvider.getService().userEndpoint().getUploadUrl().execute().getString();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Profilbild konnte nicht hochgeladen werden");
                    return null;
                }

                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(uploadUrl);

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addPart("myFile", new InputStreamBody(image, "myFile"));
                    builder.addTextBody("id", userMail);

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
                    String answer = EntityUtils.toString(httpEntity, encoding);
                    return answer.equals("true");
                } catch (IOException e){
                    e.printStackTrace();
                    publishError("Profilbild konnte nicht hochgeladen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Lädt das Bild mit dem übergebenen BlobKey vom Blobstore runter und gibt dieses als Bitmap
     * zurück.
     *
     * @param handler
     * @param pictureKey BlobKey
     */
    public static void getUserPicture(ExtendedTaskDelegate handler, final BlobKey pictureKey){
        new ExtendedTask<Void, Void, Bitmap>(handler) {
            @Override
            protected Bitmap doInBackground(Void... params) {
                ByteArrayInputStream is = null;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(Constants.SERVER_URL + "/userImages/serve?key=" + pictureKey.getKeyString());
                    HttpResponse response = client.execute(get);
                    HttpEntity httpEntity = response.getEntity();

                    is = new ByteArrayInputStream(EntityUtils.toByteArray(httpEntity));
                    return BitmapFactory.decodeStream(is);
                } catch(Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        }.execute();
    }

    /**
     * Listet Informationen der Benutzer auf, dessen E-Mail Adressen übergeben wurden.
     *
     * @param handler
     * @param userMails Liste der E-Mail Adressen der Benutzer
     */
    @SuppressWarnings("unchecked")
    public static void listUserInfo(ExtendedTaskDelegate handler, List<String> userMails) {
        new ExtendedTask<List<String>, Void, List<UserListData>>(handler) {
            @Override
            protected List<UserListData> doInBackground(List<String>... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().listUserInfo(params[0]).execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Liste von Benutzern konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(userMails);
    }

    /**
     * Erstellt eine Liste mit allgemeinen Informationen zu den Freunden eines Benutzers.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void listFriends(ExtendedTaskDelegate handler, String userMail) {
        new ExtendedTask<String, Void, List<String>>(handler) {
            @Override
            protected List<String> doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().listFriends(params[0]).execute().getStringList();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Freundesliste konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(userMail);
    }

    /**
     * Durchsucht die Benutzer nach der Eingabe und gibt das Ergebnis als Liste von E-Mail Adressen
     * der Benutzer aus.
     *
     * @param handler
     * @param input Eingabe
     */
    public static void searchUsers(ExtendedTaskDelegate handler, final String input) {
        new ExtendedTask<String, Void, List<String>>(handler) {
            @Override
            protected List<String> doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().searchUsers(input).execute().getStringList();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Suche nach Benutzern konnte nicht durchgeführt werden");
                    return null;
                }
            }
        }.execute(input);
    }

    /**
     * Aktualisiert die allgemeinen Informationen zu einem Benutzer.
     *
     * @param handler
     * @param newInfo         Neue allgemeine Informationen zu dem Benutzer
     * @param optOutSearch
     * @param showPrivateGroups
     */
    public static void updateUserProfile(ExtendedTaskDelegate handler, final UserInfo newInfo,
                                         final Boolean optOutSearch, final Visibility showPrivateGroups) {
        new ExtendedTask<Void, Void, UserInfo>(handler) {
            @Override
            protected UserInfo doInBackground(Void... voids) {
                try {
                    return ServiceProvider.getService().userEndpoint().updateUserProfile(optOutSearch,
                            showPrivateGroups.getId(), newInfo).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzerdaten konnten nicht geändert werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Aktualisiert die Standortinformationen eines Benutzers.
     *
     * @param handler
     * @param userMail       E-Mail Adresse des Benutzers
     * @param latitude       Neue Latitude
     * @param longitude      Neue Longitude
     * @param currentEventId Neue Event Id
     */
    public static void updateUserLocation(ExtendedTaskDelegate handler, final String userMail,
                                          final double latitude, final double longitude, final long currentEventId) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().updateUserLocation(currentEventId,
                            latitude, longitude, userMail).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Standortinformationen konnten nicht geändert werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Fügt einen Benutzer der eigenen Freundeliste hinzu.
     *
     * @param handler
     * @param userMail E-Mail Adresse dessen Freundeliste ergänzt wird
     * @param friendMail E-Mail Adresse des zu hinzufügenden Freundes
     */
    public static void addFriend(ExtendedTaskDelegate handler, final String userMail, final String friendMail) {
        new ExtendedTask<String, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().addFriend(friendMail, userMail).execute().getValue();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzer konnte nicht der Freundesliste hinzugefügt werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Entfernt einen Benutzer aus der eigenen Freundeliste.
     *
     * @param handler
     * @param userMail E-Mail Adresse dessen Freundeliste bearbeitet wird
     * @param friendMail E-Mail Adresse des Freundes, der entfernt werden soll
     */
    public static void removeFriend(ExtendedTaskDelegate handler, final String userMail, final String friendMail) {
        new ExtendedTask<String, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().removeFriend(friendMail, userMail).execute().getValue();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzer konnte nicht aus der Freundesliste entfernt werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Löscht einen Benutzer auf dem Server, falls dieser vorhanden ist. Wird zur Zeit nur für
     * die Tests verwendet.
     *
     * @param userMail E-Mail Adresse des Benutzers
     */
    @SuppressWarnings("unchecked")
    public static void deleteUser(ExtendedTaskDelegate handler, String userMail) {
        new ExtendedTask<String, Void, Void>(handler) {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().deleteUser(params[0]).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzer konnte nicht gelöscht werden");
                    return null;
                }
            }
        }.execute(userMail);
    }
}
