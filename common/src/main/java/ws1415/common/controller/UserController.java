package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserInfoPicture;
import com.skatenight.skatenightAPI.model.UserLocation;
import com.skatenight.skatenightAPI.model.UserPicture;

import java.io.IOException;
import java.util.List;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.CreateUserTask;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.task.GetFullUserTask;

/**
 * Der UserController steuert den Datenfluss zwischen den Benutzerdaten und View-Komponenten.
 *
 * @author Martin Wrodarczyk
 */
public class UserController {

    /**
     * Erstellt einen Benutzer auf dem Server, falls noch keiner zu der angegebenen E-Mail Adresse
     * existiert.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void createUser(ExtendedTaskDelegate handler, String userMail) {
        new CreateUserTask(handler).execute(userMail);
    }

    /**
     * Gibt den Benutzer mit allen Informationen zu der angegebenen E-Mail Adresse aus.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void getFullUser(ExtendedTaskDelegate handler, String userMail) {
        new GetFullUserTask(handler).execute(userMail);
    }

    /**
     * Aktualisiert die allgemeinen Informationen zu einem Benutzer.
     *
     * @param handler
     * @param newInfo Neue allgemeine Informationen zu dem Benutzer
     */
    public static void updateUserInfo(ExtendedTaskDelegate handler, UserInfo newInfo) {
        new ExtendedTask<UserInfo, Void, UserInfo>(handler) {
            @Override
            protected UserInfo doInBackground(UserInfo... params) {
                UserInfo userInfo = params[0];
                try {
                    return ServiceProvider.getService().userEndpoint().updateUserInfo(userInfo).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Benutzerdaten konnten nicht geändert werden");
                    return null;
                }
            }
        }.execute(newInfo);
    }

    /**
     * Aktualisiert das Profilbild eines Benutzers.
     *
     * @param handler
     * @param newPicture Neues Profilbild eines Benutzers
     */
    public static void updateUserPicture(ExtendedTaskDelegate handler, UserPicture newPicture) {
        new ExtendedTask<UserPicture, Void, UserPicture>(handler) {
            @Override
            protected UserPicture doInBackground(UserPicture... params) {
                UserPicture userPicture = params[0];
                try {
                    return ServiceProvider.getService().userEndpoint().updateUserPicture(userPicture).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Profilbild konnte nicht geändert werden");
                    return null;
                }
            }
        }.execute(newPicture);
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
                } catch(IOException e) {
                    e.printStackTrace();
                    publishError("Standortinformationen konnten nicht geändert werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Listet die allgemeinen Informationen der Benutzer auf, dessen E-Mail Adressen übergeben
     * wurden.
     *
     * @param handler
     * @param userMails Liste der E-Mail Adressen der Benutzer
     */
    @SuppressWarnings("unchecked")
    public static void listUserInfo(ExtendedTaskDelegate handler, List<String> userMails) {
        new ExtendedTask<List<String>, Void, List<UserInfo>>(handler) {
            @Override
            protected List<UserInfo> doInBackground(List<String>... params) {
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
     * Listet die allgemeinen Informationen der Benutzer auf, dessen E-Mail Adressen übergeben
     * wurden.
     *
     * @param handler
     * @param userMails Liste der E-Mail Adressen der Benutzer
     */
    @SuppressWarnings("unchecked")
    public static void listUserInfoWithPicture(ExtendedTaskDelegate handler, List<String> userMails) {
        new ExtendedTask<List<String>, Void, List<UserInfoPicture>>(handler) {
            @Override
            protected List<UserInfoPicture> doInBackground(List<String>... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().listUserInfoWithPicture(params[0]).execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Liste von Benutzern konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(userMails);
    }

    /**
     * Gibt eine Liste von allen Nutzergruppen aus, bei denen der Benutzer als Mitglied eingetragen
     * ist.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    @SuppressWarnings("unchecked")
    public static void listUserGroups(ExtendedTaskDelegate handler, String userMail) {
        new ExtendedTask<String, Void, List<UserGroup>>(handler) {
            @Override
            protected List<UserGroup> doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().listUserGroups(params[0]).execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Liste von Nutzergruppen konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(userMail);
    }

    /**
     * Gibt eine Liste von allen Veranstaltungen aus, an denen der Benutzer teilgenommen hat bzw.
     * teilnimmt.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    @SuppressWarnings("unchecked")
    public static void listEvents(ExtendedTaskDelegate handler, String userMail) {
        new ExtendedTask<String, Void, List<Event>>(handler) {
            @Override
            protected List<Event> doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().listEvents(params[0]).execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Liste von Veranstaltungen konnte nicht abgerufen werden");
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
    public static void getUserLocation(ExtendedTaskDelegate handler, String userMail){
        new ExtendedTask<String, Void, UserLocation>(handler) {
            @Override
            protected UserLocation doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().getUserLocation(params[0]).execute();
                } catch (IOException e){
                    e.printStackTrace();
                    publishError("Standort des Benutzers konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(userMail);
    }

    /**
     *
     * Löscht einen Benutzer auf dem Server, falls dieser vorhanden ist. Wird zur Zeit nur für
     * die Tests verwendet.
     *
     * @param userMail E-Mail Adresse des Benutzers
     */
    @SuppressWarnings("unchecked")
    public static void deleteUser(ExtendedTaskDelegate handler, String userMail){
        new ExtendedTask<String, Void, Void>(handler) {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().userEndpoint().deleteUser(params[0]).execute();
                } catch(IOException e) {
                    e.printStackTrace();
                    publishError("Benutzer konnte nicht gelöscht werden");
                    return null;
                }
            }
        }.execute(userMail);
    }
}
