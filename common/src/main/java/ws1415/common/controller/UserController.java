package ws1415.common.controller;

import android.graphics.Bitmap;

import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.Text;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserListData;
import com.skatenight.skatenightAPI.model.UserLocation;
import com.skatenight.skatenightAPI.model.UserPicture;
import com.skatenight.skatenightAPI.model.UserProfile;

import java.io.IOException;
import java.util.List;

import ws1415.common.model.Visibility;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.util.ImageUtil;

/**
 * Der UserController steuert den Datenfluss zwischen den Benutzerdaten und den View-Komponenten.
 *
 * @author Martin Wrodarczyk
 */
public class UserController {
    /**
     * Keine Instanziierung ermöglichen.
     */
    private UserController() {
    }

    /**
     * Erstellt einen Benutzer auf dem Server, falls noch keiner zu der angegebenen E-Mail Adresse
     * existiert.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void createUser(ExtendedTaskDelegate handler, String userMail, final String firstName, final String lastName, Bitmap picture) {
        Text encodedPicture = new Text().setValue(null);
        if(picture != null) encodedPicture = ImageUtil.EncodeBitmapToText(picture);
        final Text pictureFinal = encodedPicture;
        new ExtendedTask<String, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().createUser(params[0], pictureFinal)
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
     * Gibt die Standordinformationen eines Benutzers mit der angegebenen E-Mail Adresse aus.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    @SuppressWarnings("unchecked")
    public static void getUserLocation(ExtendedTaskDelegate handler, String userMail) {
        new ExtendedTask<String, Void, UserLocation>(handler) {
            @Override
            protected UserLocation doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().getUserLocation(params[0]).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Standort konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(userMail);
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
     * Gibt das Profilbild eines Benutzers aus.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void getUserPicture(ExtendedTaskDelegate handler, final String userMail) {
        new ExtendedTask<Void, Void, UserPicture>(handler) {
            @Override
            protected UserPicture doInBackground(Void... voids) {
                try {
                    return ServiceProvider.getService().userEndpoint().getUserPicture(userMail).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Profilbild konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Listet Informationen der Benutzer auf, dessen E-Mail Adressen übergeben wurden.
     *
     * @param handler
     * @param userMails Liste der E-Mail Adressen der Benutzer
     * @param withPicture true, falls Profilbilder mit abgerufen werden sollen, false andernfalls
     */
    @SuppressWarnings("unchecked")
    public static void listUserInfo(ExtendedTaskDelegate handler, List<String> userMails, final Boolean withPicture) {
        new ExtendedTask<List<String>, Void, List<UserListData>>(handler) {
            @Override
            protected List<UserListData> doInBackground(List<String>... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().listUserInfo(params[0], withPicture).execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Liste von Benutzern konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(userMails);
    }

    /**
     * Listet die Profilbilder der Benutzer auf, dessen E-Mail Adressen übergeben wurden.
     *
     * @param handler
     * @param userMails Liste der E-Mail Adressen der Benutzer
     */
    @SuppressWarnings("unchecked")
    public static void listUserPicture(ExtendedTaskDelegate handler, List<String> userMails) {
        new ExtendedTask<List<String>, Void, List<UserPicture>>(handler) {
            @Override
            protected List<UserPicture> doInBackground(List<String>... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().listUserPicture(params[0]).execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Liste von Profilbildern konnte nicht abgerufen werden");
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
        new ExtendedTask<String, Void, List<UserListData>>(handler) {
            @Override
            protected List<UserListData> doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().listFriends(params[0]).execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Freundesliste konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(userMail);
    }

    /**
     * Durchsucht die Benutzer nach der Eingabe und gibt die allgemeinen Nutzerinformationen
     * zu den Ergebnissen in einer Liste aus.
     *
     * @param handler
     * @param input   Eingabe
     */
    public static void searchUsers(ExtendedTaskDelegate handler, final String input) {
        new ExtendedTask<String, Void, List<UserListData>>(handler) {
            @Override
            protected List<UserListData> doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().searchUsers(input).execute().getItems();
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
    public static void updateUserProfile(ExtendedTaskDelegate handler, final UserInfo newInfo, final Boolean optOutSearch, final Visibility showPrivateGroups) {
        new ExtendedTask<Void, Void, UserInfo>(handler) {
            @Override
            protected UserInfo doInBackground(Void... voids) {
                try {
                    return ServiceProvider.getService().userEndpoint().updateUserProfile(optOutSearch, showPrivateGroups.getId(), newInfo).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzerdaten konnten nicht geändert werden");
                    return null;
                }
            }
        }.execute();
    }


    /**
     * Aktualisiert das Profilbild eines Benutzers.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     * @param picture  Bitmap des neuen Profilbildes
     */
    public static void updateUserPicture(ExtendedTaskDelegate handler, final String userMail, final Bitmap picture) {
        Text encodedImage = null;
        if(picture != null) encodedImage = ImageUtil.EncodeBitmapToText(picture);
        final Text encodedImageFinal = encodedImage;
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().updateUserPicture(userMail, encodedImageFinal).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Profilbild konnte nicht geändert werden");
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
                    return ServiceProvider.getService().userEndpoint().addFriend(userMail, friendMail).execute().getValue();
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
                    return ServiceProvider.getService().userEndpoint().removeFriend(userMail, friendMail).execute().getValue();
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
