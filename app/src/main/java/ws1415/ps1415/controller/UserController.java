package ws1415.ps1415.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.StringWrapper;
import com.skatenight.skatenightAPI.model.UserListData;
import com.skatenight.skatenightAPI.model.UserPrimaryData;
import com.skatenight.skatenightAPI.model.UserProfile;
import com.skatenight.skatenightAPI.model.UserProfileEdit;

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
import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.Constants;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;

/**
 * Der UserController steuert den Datenfluss zwischen den Benutzerdaten auf dem Server und den
 * Komponenten in der Anwendung.
 *
 * @author Martin Wrodarczyk
 */
public abstract class UserController {
    /**
     * Erstellt einen Benutzer, falls noch keiner zu der angegebenen E-Mail Adresse existiert.
     *
     * @param handler  Auszuführender Task
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void createUser(ExtendedTaskDelegate<Void, Boolean> handler, String userMail,
                                  final String firstName, final String lastName) {
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
     * Prüft, ob ein Benutzer mit der E-Mail Adresse existiert.
     *
     * @param handler  Auszuführender Task
     * @param userMail E-Mail Adressen des Benutzers
     */
    public static void existsUser(ExtendedTaskDelegate<Void, Boolean> handler, String userMail) {
        new ExtendedTask<String, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().existsUser(params[0]).execute().getValue();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Serververbindung fehlgeschlagen");
                    return null;
                }
            }
        }.execute(userMail);
    }

    /**
     * Gibt die primären Informationen Vorname, Nachname und Key des Profilbildes des Benutzers aus.
     *
     * @param handler  Auszuführender Task
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void getPrimaryData(ExtendedTaskDelegate<Void, UserPrimaryData> handler,
                                      final String userMail) {
        new ExtendedTask<Void, Void, UserPrimaryData>(handler) {
            @Override
            protected UserPrimaryData doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().getPrimaryData(userMail).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzer konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Gibt das Profil des Benutzers aus.
     *
     * @param handler  Auszuführender Task
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void getUserProfile(ExtendedTaskDelegate<Void, UserProfile> handler,
                                      final String userMail) {
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
     * Gibt eigene Profildaten eines Benutzers aus, die zur Bearbeitung des Profils dienen.
     *
     * @param handler  Auszuführender Task
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void getUserProfileEdit(ExtendedTaskDelegate<Void, UserProfileEdit> handler,
                                          final String userMail) {
        new ExtendedTask<Void, Void, UserProfileEdit>(handler) {
            @Override
            protected UserProfileEdit doInBackground(Void... voids) {
                try {
                    return ServiceProvider.getService().userEndpoint().getUserProfileEdit(userMail).execute();
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
     * @param handler  Auszuführender Task
     * @param userMail E-Mail Adresse des Benutzers
     * @param image    Neues Profilbild
     */
    public static void uploadUserPicture(ExtendedTaskDelegate<Void, Boolean> handler,
                                         final String userMail, final InputStream image) {
        new ExtendedTask<Void, Void, Boolean>(handler) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Profilbild konnte nicht hochgeladen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Lädt das Bild mit dem übergebenen BlobKey vom Blobstore runter und gibt das Bild als Bitmap
     * zurück.
     *
     * @param handler    Auszuführender Task
     * @param pictureKey BlobKey
     */
    public static void getUserPicture(ExtendedTaskDelegate<Void, Bitmap> handler, final BlobKey pictureKey) {
        new ExtendedTask<Void, Void, Bitmap>(handler) {
            @Override
            protected Bitmap doInBackground(Void... params) {
                ByteArrayInputStream is = null;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(Constants.SERVER_URL + "/userImages/serve?key=" +
                            pictureKey.getKeyString());
                    HttpResponse response = client.execute(get);
                    HttpEntity httpEntity = response.getEntity();

                    is = new ByteArrayInputStream(EntityUtils.toByteArray(httpEntity));
                    return BitmapFactory.decodeStream(is);
                } catch (Exception ex) {
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
     * Löscht das Profilbild eines Benutzers.
     *
     * @param handler  Auszuführender Task
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void removeUserPicture(ExtendedTaskDelegate<Void, Boolean> handler, final String userMail) {
        new ExtendedTask<Void, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().removeUserPicture(userMail).execute().getValue();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Profilbild konnte nicht gelöscht werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Listet Informationen der Benutzer auf, dessen E-Mail Adressen übergeben wurden.
     *
     * @param handler   Auszuführender Task
     * @param userMails Liste der E-Mail Adressen der Benutzer
     */
    public static void listUserInfo(ExtendedTaskDelegate<Void, List<UserListData>> handler,
                                    final List<String> userMails) {
        new ExtendedTask<Void, Void, List<UserListData>>(handler) {
            @Override
            protected List<UserListData> doInBackground(Void... params) {
                try {
                    List<UserListData> result = ServiceProvider.getService().userEndpoint().listUserInfo(userMails).execute().getItems();
                    if(result == null) return new ArrayList<UserListData>();
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Liste von Benutzern konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Gibt eine Liste mit den E-Mail Adressen der Freunde eines Benutzers aus.
     *
     * @param handler  Auszuführender Task
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void listFriends(ExtendedTaskDelegate<Void, List<String>> handler, String userMail) {
        new ExtendedTask<String, Void, List<String>>(handler) {
            @Override
            protected List<String> doInBackground(String... params) {
                try {
                    List<String> friends = ServiceProvider.getService().userEndpoint().listFriends(params[0]).execute().getStringList();
                    if(friends == null) friends = new ArrayList<>();
                    return friends;
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
     * @param handler Auszuführender Task
     * @param input   Eingabe nach der gesucht werden soll, wobei nach der E-Mail, dem Vornamen und
     *                dem Nachnamen gesucht wird
     */
    public static void searchUsers(ExtendedTaskDelegate<Void, List<String>> handler, final String input) {
        new ExtendedTask<String, Void, List<String>>(handler) {
            @Override
            protected List<String> doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().searchUsers(new StringWrapper().setString(input)).execute().getStringList();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Suche konnte nicht durchgeführt werden");
                    return null;
                }
            }
        }.execute(input);
    }

    /**
     * Aktualisiert die allgemeinen Informationen zu einem Benutzer.
     *
     * @param handler         Auszuführender Task
     * @param userProfileEdit Neue Nutzerprofilinformationen
     */
    public static void updateUserProfile(ExtendedTaskDelegate<Void, Boolean> handler, final UserProfileEdit userProfileEdit) {
        new ExtendedTask<Void, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return ServiceProvider.getService().userEndpoint().updateUserProfile(userProfileEdit).execute().getValue();
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
     * @param handler        Auszuführender Task
     * @param userMail       E-Mail Adresse des Benutzers
     * @param latitude       Latitude
     * @param longitude      Longitude
     * @param currentEventId Event Id
     */
    public static void updateUserLocation(ExtendedTaskDelegate<Void, Void> handler, final String userMail,
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
     * @param handler    Auszuführender Task
     * @param userMail   E-Mail Adresse des Benutzers, dessen Freundeliste ergänzt wird
     * @param friendMail E-Mail Adresse des zu hinzufügenden Freundes
     */
    public static void addFriend(ExtendedTaskDelegate<Void, Boolean> handler, final String userMail,
                                 final String friendMail) {
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
     * @param handler    Auszuführender Task
     * @param userMail   E-Mail Adresse des Benutzers, dessen Freundeliste bearbeitet wird
     * @param friendMail E-Mail Adresse des Freundes, der entfernt werden soll
     */
    public static void removeFriend(ExtendedTaskDelegate<Void, Boolean> handler, final String userMail,
                                    final String friendMail) {
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
     * Löscht einen Benutzer, falls dieser vorhanden ist.
     *
     * @param handler  Auszuführender Task
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void deleteUser(ExtendedTaskDelegate<Void, Void> handler, String userMail) {
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
