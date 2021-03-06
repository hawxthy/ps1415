package ws1415.ps1415.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.ListWrapper;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupBlackBoardTransport;
import com.skatenight.skatenightAPI.model.UserGroupFilter;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;
import com.skatenight.skatenightAPI.model.UserGroupMetaDataList;
import com.skatenight.skatenightAPI.model.UserGroupNewsBoardTransport;
import com.skatenight.skatenightAPI.model.UserGroupVisibleMembers;

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
import ws1415.ps1415.model.UserGroupType;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.PrefManager;

/**
 * Stellt Funktionen zur Verarbeitung von Nutzergruppen bereit.
 *
 * @author Bernd Eissing on 02.05.2015.
 */
public class GroupController {
    private static GroupController instance;

    private GroupController() {
    }

    public static GroupController getInstance() {
        if (instance == null) {
            instance = new GroupController();
        }
        return instance;
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um den Namen einer
     * Nutzergruppe zu prüfen.
     *
     * @param handler
     * @param groupName Der Name der Gruppe
     */
    public void checkGroupName(ExtendedTaskDelegate handler, String groupName) {
        new ExtendedTask<String, Void, BooleanWrapper>(handler) {
            @Override
            protected BooleanWrapper doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().checkGroupName(params[0]).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Der Name konnte nicht geprüft werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um eine neue private
     * Nutzergruppe mit vom angegebenen Typ mit Passwort zu erstellen
     *
     * @param handler       Der Task, der mit dem Server kommuniziert
     * @param groupName     Der Name der zu erstellenden UserGroup
     * @param groupType     Der Typ der Nutzergruppe
     * @param password      Das Passwort, kann nicht null oder leer sein
     * @param privacy       Die Öffentlichkeitseinstellung der Gruppe
     * @param description   Die Beschreibung der Gruppe
     * @param blobKeyValue  Der Link des Blobkeys, falls angegeben
     */
    public void createUserGroup(ExtendedTaskDelegate handler,
                                String groupName,
                                final UserGroupType groupType,
                                final String password,
                                final boolean privacy,
                                final String description,
                                final String blobKeyValue) {
        new ExtendedTask<String, Void, Void>(handler) {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().createUserGroup(params[0], groupType.name(), privacy)
                            .set("groupPassword", password)
                            .set("groupDescription", description)
                            .set("blobKeyValue", blobKeyValue).execute();
                } catch (IOException e) {
                    publishError("Fehler: Es konnte keine Gruppe mit dem Namen " + params[0] + " erstellt werden, versuchen Sie einen anderen");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommunizert um eine UserGroup
     * mit dem angegebenen Namen abruft.
     *
     * @param handler   Der Task, der mit dem Server kommunizert
     * @param groupName Der Name der UserGroup
     */
    public void getUserGroup(ExtendedTaskDelegate handler, String groupName) {
        new ExtendedTask<String, Void, UserGroup>(handler) {
            @Override
            protected UserGroup doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().getUserGroup(params[0]).execute();
                } catch (IOException e) {
                    publishError("Fehler: Die Gruppe " + params[0] + " konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Läd zunächste alle Nutzergruppen vom Server um diese dann nach den
     * sichtbaren Gruppen zu durchsuchen. Dies ist sehr ineffizient gelöst.
     *
     * @param context Die Aufrufende Activity, View etc.
     * @return Eine Liste der Metadaten zu den sichtbaren Gruppen.
     */
    public List<UserGroupMetaData> getVisibleUserGroups(final Context context) {
        // wahrscheinlich nicht nötig
        UserGroupFilter filter = new UserGroupFilter();
        filter.setLimit(1000);
        final List<UserGroupMetaData> visibleGroups = new ArrayList<>();
        getMyUserGroups(new ExtendedTaskDelegateAdapter<Void, List<UserGroupMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<UserGroupMetaData> metaDatas) {
                if (metaDatas == null) {
                    Toast.makeText(context, "Es existieren keine Gruppen, denen Sie beigetreten sind", Toast.LENGTH_LONG).show();
                } else {
                    for (UserGroupMetaData metaData : metaDatas) {
                        if (PrefManager.getGroupVisibility(context, metaData.getName())) {
                            visibleGroups.add(metaData);
                        }
                    }
                }
            }
        }, filter);
        return visibleGroups;
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um die Metadaten zu einer
     * Nutzergruppe abzurufen.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     */
    public void getUserGroupMetaData(ExtendedTaskDelegate handler, String groupName) {
        new ExtendedTask<String, Void, UserGroupMetaData>(handler) {
            @Override
            protected UserGroupMetaData doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().getUserGroupMetaData(params[0]).execute();
                } catch (IOException e) {
                    publishError("Fehler: Die Metadaten der Gruppe " + params[0] + " konnten nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um eine Liste von Metadaten zu allen
     * Nutzergruppen zu erhalten.
     *
     * @param handler
     */
    public void getUserGroupMetaDatas(ExtendedTaskDelegate handler) {
        new ExtendedTask<Void, Void, List<UserGroupMetaData>>(handler) {
            @Override
            protected List<UserGroupMetaData> doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().getAllUserGroupMetaDatas().execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Die Metadaten zu allen Gruppen konnten nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um Metadaten zu Nutzergruppen
     * herunter zu laden.
     *
     * @param handler
     * @param filter  Gibt an wie viele geladen werden sollen und von wo geladen werden soll
     *                siehe Doku im Backend
     */
    public void listAllUserGroupMetaDatas(ExtendedTaskDelegate handler, final UserGroupFilter filter) {
        new ExtendedTask<Void, Void, List<UserGroupMetaData>>(handler) {
            @Override
            protected List<UserGroupMetaData> doInBackground(Void... params) {
                try {
                    UserGroupMetaDataList result = ServiceProvider.getService().groupEndpoint().listUserGroups(filter).execute();
                    filter.setCursorString(result.getWebCursorString());
                    return result.getMetaDatas();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Die Metadatenliste konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um eine Liste von Gruppen zu laden.
     * Hier werden alle Gruppen geladen die in der Liste  angegeben sind, falls sie existieren.
     *
     * @param handler
     * @param groups Liste von Gruppennamen
     */
    public void fetchSpecificGroupDatas(ExtendedTaskDelegate handler, final List<String> groups){
        new ExtendedTask<List<String>, Void, List<UserGroupMetaData>>(handler){
            @Override
            protected List<UserGroupMetaData> doInBackground(List<String>... lists) {
                try{
                    return ServiceProvider.getService().groupEndpoint().fetchSpecificGroupDatas(new ListWrapper().setStringList(groups)).execute().getItems();
                }catch (IOException e){
                    e.printStackTrace();
                    publishError("Fehler: Die Gruppen konnten nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint
     * kommuniziert um alle Nutzergruppen zu laden
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void getAllUserGroups(ExtendedTaskDelegate handler) {
        new ExtendedTask<Void, Void, List<UserGroup>>(handler) {
            @Override
            protected List<UserGroup> doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().getAllUserGroups().execute().getItems();
                } catch (IOException e) {
                    publishError("Fehler: Die Gruppen konnten nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um
     * alle UserGroups zum angemeldeten EndUser zu laden.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void getMyUserGroups(ExtendedTaskDelegate handler, final UserGroupFilter filter) {
        new ExtendedTask<UserGroupFilter, Void, List<UserGroupMetaData>>(handler) {
            @Override
            protected List<UserGroupMetaData> doInBackground(UserGroupFilter... params) {
                try {
                    UserGroupMetaDataList result =  ServiceProvider.getService().groupEndpoint().fetchMyUserGroups(filter).execute();
                    filter.setCursorString(result.getWebCursorString());
                    return result.getMetaDatas();
                } catch (IOException e) {
                    publishError("Fehler: Ihre Gruppen konnten nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um nach Nutzergruppen
     * zu suchen, dabei gibt der searchKey an, wonach gesucht werden soll. Der Filter
     * gibt an, wie viel auf einmal maximal geladen werden soll.
     *
     * @param handler
     * @param searchKey String nach dem gesucht werden soll
     * @param filter Filter, der die maximal Anzahl an Ergebnissen beinhaltet
     */
    public void searchUserGroups(ExtendedTaskDelegate handler, final String searchKey, final UserGroupFilter filter){
        new ExtendedTask<UserGroupFilter, Void, List<UserGroupMetaData>>(handler){
            @Override
            protected List<UserGroupMetaData> doInBackground(UserGroupFilter... params) {
                try{
                    UserGroupMetaDataList result = ServiceProvider.getService().groupEndpoint().searchGroups(searchKey.toLowerCase(), filter).execute();
                    filter.setCursorString(result.getWebCursorString());
                    return result.getMetaDatas();
                }catch ( IOException e){
                    e.printStackTrace();
                    publishError("Fehler: Die Suche konnte nicht ausgeführt werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um eine UserGroup zu löschen.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void deleteUserGroup(ExtendedTaskDelegate handler, String groupName) {
        new ExtendedTask<String, Void, Void>(handler) {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().deleteUserGroup(params[0]).execute();
                } catch (IOException e) {
                    publishError("Fehler: " + params[0] + " konnte nicht gelöscht werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um
     * einen EndUser einer UserGroup zuzuordnen.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void joinUserGroup(ExtendedTaskDelegate handler, String groupName) {
        new ExtendedTask<String, Void, BooleanWrapper>(handler) {
            @Override
            protected BooleanWrapper doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().joinUserGroup(params[0]).execute();
                } catch (IOException e) {
                    publishError("Fehler: " + params[0] + " konnte nicht beigetreten werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um
     * einen EndUser einer UserGroup zuzuordnen.
     *
     * @param handler  Der Task, der mit dem Server kommuniziert
     * @param password Das Password, falls eins beötigt wird kann weder leer noch null sein
     */
    public void joinPrivateUserGroup(ExtendedTaskDelegate handler, String groupName, final String password) {
        new ExtendedTask<String, Void, BooleanWrapper>(handler) {
            @Override
            protected BooleanWrapper doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().joinPrivateUserGroup(params[0], password).execute();
                } catch (IOException e) {
                    publishError("Fehler: " + params[0] + " konnte nicht beigetreten werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint via den LeaveUserGroupTask
     * kommuniziert um einen EndUser aus einer UserGroup zu entfernen
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void leaveUserGroup(ExtendedTaskDelegate handler, String groupName) {
        final String groupNameFinal = groupName;
        new ExtendedTask<String, Void, Void>(handler) {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().leaveUserGroup(params[0]).execute();
                } catch (IOException e) {
                    publishError("Fehler: " + params[0] + " konnte nicht verlassen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um Einladungen in Form einer Notification
     * an den übergebenen EndUser zu schicken.
     *
     * @param handler   Der Task, der den Server anspricht
     * @param groupName die UserGroup zu der die Einladung verschickt wird
     * @param users     Die EndUser, die die Einladung erhalten soll
     */
    public void sendInvitation(ExtendedTaskDelegate handler, String groupName, final List<String> users, final String invitationMessage) {
        new ExtendedTask<String, Void, Void>(handler) {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().sendInvitation(params[0], invitationMessage, new ListWrapper().setStringList(users)).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Die Einladung konnte nicht versendet werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEnpoint kommuniziert um
     * den übergenen EndUser aus der übergebenen UseGroup löscht.
     *
     * @param handler   Task, der den Server anspricht
     * @param groupName Die UserGroup dessen EndUser gelöscht werden soll
     * @param user      Die E-Mail des EndUsers, der gelöscht werden soll
     * @return Meldung über Erfolg des Löschens
     * true = Löschen war erfolgreich
     * false = Löschen war nicht erfolgreich
     */
    public void removeMember(ExtendedTaskDelegate handler, String groupName, final String user) {
        new ExtendedTask<String, Void, Void>(handler) {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().removeMember(params[0], user).execute();
                } catch (IOException e) {
                    publishError("Fehler: Das Mitglied " + user + " von " + params[0] + " konnte nicht entfernt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndoint kommuniziert um eine Nachricht an das
     * BlackBoard der übergebenen UserGroup zu schicken.
     *
     * @param handler      Der Task, der mit dem Server kommuniziert
     * @param groupName    Die UserGroup, dessen BlackBoard eine neue Nachricht erhalten soll
     * @param boardMessage Die Nachricht
     */
    public void postBlackBoard(ExtendedTaskDelegate handler, String groupName, final String boardMessage, final InputStream pictureFile) {
        new ExtendedTask<String, Void, BoardEntry>(handler) {
            BoardEntry boardEntry;

            @Override
            protected BoardEntry doInBackground(String... params) {

                if (pictureFile != null) {
                    try {
                        boardEntry = ServiceProvider.getService().groupEndpoint().postBlackBoard(params[0], boardMessage).execute();
                        if (boardEntry != null) {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httppost = new HttpPost(ServiceProvider.getService().groupEndpoint().previewImageUploadUrl().execute().getString());

                            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                            builder.addPart("file", new InputStreamBody(pictureFile, "file"));
                            builder.addTextBody("blackboard", "true");
                            builder.addTextBody("id", boardEntry.getId().toString());

                            HttpEntity entity = builder.build();
                            httppost.setEntity(entity);
                            HttpResponse response = httpclient.execute(httppost);
                            HttpEntity httpEntity = response.getEntity();

                            String encoding;
                            if (httpEntity.getContentEncoding() == null) {
                                // UTF-8 verwenden, falls keine Kodierung für die Antwort übertragen wurde
                                encoding = "UTF-8";
                            } else {
                                encoding = httpEntity.getContentEncoding().getValue();
                            }

                            // Die BlobKeyValue (ein String) aus der httpEntity holen und daraus einen BlobKey erstellen
                            // und diesen zurück an den Clienten senden.
                            String keyString = EntityUtils.toString(httpEntity, encoding);
                            BlobKey blobKey = new BlobKey();
                            blobKey.setKeyString(keyString);
                            boardEntry.setBlobKey(blobKey);
                            return boardEntry;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        publishError("Fehler: Der Eintrag konnte nicht erstellt werden");
                    }
                    return null;

                } else {
                    try {
                        return ServiceProvider.getService().groupEndpoint().postBlackBoard(params[0], boardMessage).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        publishError("Fehler: Der Eintrag konnte nicht erstellt werden");
                        return null;
                    }
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziet um eine Nachricht vom BlackBoard
     * der übergebenen UserGroup zu löschen.
     *
     * @param handler      Der Task, der mit dem Server kommuniziert
     * @param group        Die UserGroup, deren BlackBoard Nachricht gelöscht werden soll
     * @param boardEntryId Der BoardEntry der gelöscht werden soll
     */
    public void deleteBoardMessage(ExtendedTaskDelegate handler, final String group, final Long boardEntryId) {
        new ExtendedTask<String, Void, Void>(handler) {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().deleteBlackBoardMessage(boardEntryId, params[0]).execute();
                } catch (IOException e) {
                    publishError("Fehler: Die Nachricht konnte nicht gelöscht werden");
                    return null;
                }
            }
        }.execute(group);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert ume eine Blackboardmessage
     * zu Kommentieren
     *
     * @param handler
     * @param groupName    Der Name der Nutzergruppe
     * @param id           Die Id der Blackboardmessage
     * @param boardComment Der Kommentar
     */
    public void commentBlackBoard(ExtendedTaskDelegate handler, Long id, final String groupName, final String boardComment) {
        new ExtendedTask<Long, Void, BoardEntry>(handler) {
            @Override
            protected BoardEntry doInBackground(Long... longs) {
                try {
                    return ServiceProvider.getService().groupEndpoint().commentBlackBoard(groupName, longs[0], boardComment).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Konnte die Boardmessage nicht Kommentieren");
                    return null;
                }
            }
        }.execute(id);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um einen Kommentar eines BoardEntry zu löschen
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     * @param boardId Die id des BoardEntry
     * @param commentId Die id des Kommentars
     */
    public void deleteComment(ExtendedTaskDelegate handler, final String groupName, final Long boardId, final long commentId){
        new ExtendedTask<Void, Void, Void>(handler){
            @Override
            protected Void doInBackground(Void... voids) {
                try{
                    return ServiceProvider.getService().groupEndpoint().removeComment(groupName, boardId, commentId).execute();
                }catch (IOException e){
                    e.printStackTrace();
                    publishError("Fehler: Der Kommentar konne nicht gelöscht werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um eine Boackboardmessage zu
     * editieren
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     * @param id Die Id der BoardEntry
     * @param newMessage Die neue Nachricht
     */
    public void editBoardEntry(ExtendedTaskDelegate handler, final Long id, final String groupName, final String newMessage){
        new ExtendedTask<Long, Void, Void>(handler){
            @Override
            protected Void doInBackground(Long... longs) {
                try{
                    return ServiceProvider.getService().groupEndpoint().editBoardEntry(groupName, id, newMessage).execute();
                }catch (IOException e){
                    e.printStackTrace();
                    publishError("Fehler: Die Message konnte nicht editiert werden");
                    return null;
                }
            }
        }.execute();
    }


    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um einen BoardEntry vom
     * Server zu laden
     *
     * @param handler
     * @param id      Die Id des Boardentries
     */
    public void getBoardEntry(ExtendedTaskDelegate handler, Long id) {
        new ExtendedTask<Long, Void, BoardEntry>(handler) {
            @Override
            protected BoardEntry doInBackground(Long... longs) {
                try {
                    return ServiceProvider.getService().groupEndpoint().getBoardEntry(longs[0]).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Der Boardentry konnte nicht geladern werden");
                    return null;
                }
            }
        }.execute(id);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommunizert um von eine Nutzergruppe
     * dessen BlackBoard abzurufen.
     *
     * @param handler   Der Task, der mit dem Server kommuniziert
     * @param groupName Der Name der Nutzergruppe
     */
    public void getBlackBoard(ExtendedTaskDelegate handler, String groupName) {
        new ExtendedTask<String, Void, UserGroupBlackBoardTransport>(handler) {
            @Override
            protected UserGroupBlackBoardTransport doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().getUserGroupBlackBoard(params[0]).execute();
                } catch (IOException e) {
                    publishError("Fehler: Das Blackboard von " + params[0] + " konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

//    /**
//     * Methode, welche mit dem GroupEndpoint kommuniziert um eine Nachricht an das NewsBoard
//     * einer Nutzergruppe zu posten.
//     *
//     * @param handler
//     * @param groupName   Der Name der Nutzergruppe
//     * @param newsMessage Der Inhalt der Nachricht
//     */
//    public void postNewsBoard(ExtendedTaskDelegate handler, String groupName, final String newsMessage) {
//        new ExtendedTask<String, Void, Void>(handler) {
//            @Override
//            protected Void doInBackground(String... params) {
//                try {
//                    return ServiceProvider.getService().groupEndpoint().postNewsBoard(params[0], newsMessage).execute();
//                } catch (IOException e) {
//                    publishError("Fehler: Die Nachricht konnte nicht gepostet werden");
//                    return null;
//                }
//            }
//        }.execute(groupName);
//    }
//
//    /**
//     * Methode, welche mit dem GroupEndoint kommunizert um eine NewsMessage einer
//     * Nutzergruppe zu löschen.
//     *
//     * @param handler
//     * @param groupName    Der Name der Nutzergruppe
//     * @param boardEntryId Die Id der BoardEntries
//     */
//    public void deleteNewsMessage(ExtendedTaskDelegate handler, String groupName, final Long boardEntryId) {
//        new ExtendedTask<String, Void, Void>(handler) {
//            @Override
//            protected Void doInBackground(String... params) {
//                try {
//                    return ServiceProvider.getService().groupEndpoint().deleteNewsBoardMessage(boardEntryId, params[0]).execute();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    publishError("Fehler: Die Nachricht konnte nicht gelöscht werden");
//                    return null;
//                }
//            }
//        }.execute(groupName);
//    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um das NewsBoard einer
     * Nutzergruppe abzurufen.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     */
    public void getNewsBoard(ExtendedTaskDelegate handler, String groupName) {
        new ExtendedTask<String, Void, UserGroupNewsBoardTransport>(handler) {
            @Override
            protected UserGroupNewsBoardTransport doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().getUserGroupNewsBoard(params[0]).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Das Newsboard von " + params[0] + " konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um Nachrichten an alle Mitglieder
     * der übergebenen UserGroup zu schicken.
     *
     * @param handler       Der Task, der mit dem Server kommuniziert
     * @param groupName     Der Name der Nutzergruppe
     * @param globalMessage Die Nachricht, nicht auf message ändern, sonst hat der Task probleme!
     */
    public void sendGlobalMessage(ExtendedTaskDelegate handler, String groupName, final String globalMessage) {
        new ExtendedTask<String, Void, Void>(handler) {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().sendGlobalMessage(params[0], globalMessage).execute();
                } catch (IOException e) {
                    publishError("Fehler: Eine Nachricht an alle Mitglieder konnte nicht versendet werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um das Bild einer Nutzergruppe zu ändern.
     * Dabei wird mit previewImageUploadUrl() eine Uploadurl auf dem Server zum Blobstore erstellt und
     * zurück gesendet. Diese Url zusammen mit dem InputStream(Der Datei, die hochgeladen werden soll)
     * und dem Namen der Gruppe werden an den Handler, die Server Klasse gesendet. Dort wird die Datei
     * hochgeladen und aus dem Resultat "response" kann man den BlobKey auslesen. Diese Key wird dann
     * in der Upload Klasse in der übergebenen Gruppe mit dem Namen groupName gespeichert und als String
     * wieder hier hin zurück geschickt. Alternative könnte man einfach erneut die Nutzergruppe groupName
     * erneut abrufen, man hat dadurch jedoch einen Serveraufruf mehr. Mit dem zurückgesendeten BlobKey
     * kann man mit der getUserGroupPicture() oder loadImageForPreview() Methode sich das hochgeladene
     * Bild herunterladen. Existiert keine Gruppe mit dem namen groupName, so schlägt der upload in der
     * Upload klasse fehl und der hochgeladene Blob, also das Bild wird gelöscht.
     *
     * @param handler   Der Task, der mit dem Server kommuniziert
     * @param groupName Die UserGroup dessen Bild geändert werden soll
     */
    public void changePicture(ExtendedTaskDelegate handler, String groupName, final InputStream pictureFile) {
        new ExtendedTask<String, Void, BlobKey>(handler) {
            @Override
            protected BlobKey doInBackground(String... params) {
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(ServiceProvider.getService().groupEndpoint().previewImageUploadUrl().execute().getString());

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addPart("file", new InputStreamBody(pictureFile, "file"));
                    builder.addTextBody("id", params[0]);

                    HttpEntity entity = builder.build();
                    httppost.setEntity(entity);
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity httpEntity = response.getEntity();

                    String encoding;
                    if (httpEntity.getContentEncoding() == null) {
                        // UTF-8 verwenden, falls keine Kodierung für die Antwort übertragen wurde
                        encoding = "UTF-8";
                    } else {
                        encoding = httpEntity.getContentEncoding().getValue();
                    }

                    // Die BlobKeyValue (ein String) aus der httpEntity holen und daraus einen BlobKey erstellen
                    // und diesen zurück an den Clienten senden.
                    String keyString = EntityUtils.toString(httpEntity, encoding);
                    BlobKey blobKey = new BlobKey();
                    blobKey.setKeyString(keyString);
                    return blobKey;
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Das Bild für " + params[0] + " konnte nicht geändert werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um das Bild einer
     * Nutzergruppe herunter zu laden.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     */
    public void getUserGroupPicture(ExtendedTaskDelegate handler, String groupName) {
        new ExtendedTask<String, Void, Bitmap>(handler) {
            BlobKey blobKey;
            ByteArrayInputStream is;

            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    blobKey = ServiceProvider.getService().groupEndpoint().getUserGroupPicture(params[0]).execute();
                    if(blobKey != null){
                        HttpClient client = new DefaultHttpClient();
                        HttpGet get = new HttpGet(Constants.SERVER_URL + "/Bernd/images/serve?key=" + blobKey.getKeyString());
                        HttpResponse response = client.execute(get);
                        HttpEntity httpEntity = response.getEntity();

                        is = new ByteArrayInputStream(EntityUtils.toByteArray(httpEntity));
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        return bitmap;
                    }else{
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Das Bild konnte nicht abgerufen werden");
                    return null;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um ein Image hochzuladen um es sich in
     * der app anzuschauen.
     *
     * @param handler
     * @param pictureFile
     * @param blobKeyString
     */
    public void uploadImageForPreview(ExtendedTaskDelegate handler, InputStream pictureFile, final String blobKeyString) {
        new ExtendedTask<InputStream, Void, BlobKey>(handler) {
            @Override
            protected BlobKey doInBackground(InputStream... params) {
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(ServiceProvider.getService().groupEndpoint().previewImageUploadUrl().execute().getString());

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addPart("file", new InputStreamBody(params[0], "file"));
                    builder.addTextBody("blobKeyString", blobKeyString);

                    HttpEntity entity = builder.build();
                    httppost.setEntity(entity);
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity httpEntity = response.getEntity();

                    String encoding;
                    if (httpEntity.getContentEncoding() == null) {
                        // UTF-8 verwenden, falls keine Kodierung für die Antwort übertragen wurde
                        encoding = "UTF-8";
                    } else {
                        encoding = httpEntity.getContentEncoding().getValue();
                    }

                    String keyString = EntityUtils.toString(httpEntity, encoding);
                    BlobKey blobKey = new BlobKey();
                    blobKey.setKeyString(keyString);
                    return blobKey;
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Das Bild konnte nicht hochgeladen werden");
                }
                return null;

            }
        }.execute(pictureFile);
    }

    /**
     * Methode, welche mit der Serve Klasse auf dem Endpoint kommuniziert um
     * ein Bild für den angegebenen BlobKey zu laden.
     *
     * @param handler
     * @param blobkey
     */
    public void loadImageForPreview(ExtendedTaskDelegate handler, final BlobKey blobkey) {
        new ExtendedTask<Void, Void, Bitmap>(handler) {
            ByteArrayInputStream is;

            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(Constants.SERVER_URL + "/Bernd/images/serve?key=" + blobkey.getKeyString());
                    HttpResponse response = client.execute(get);
                    HttpEntity httpEntity = response.getEntity();

                    is = new ByteArrayInputStream(EntityUtils.toByteArray(httpEntity));
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    return bitmap;
                } catch (Exception e) {
                    e.printStackTrace();
                    publishError("Fehler: Das Bild konnte nicht abgerufen werden");
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                return null;
            }
        }.execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um die sichtbaren Mitglieder einer
     * Nutzergruppe zu erhalten.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     */
    public void getUserGroupVisibleMembers(ExtendedTaskDelegate handler, String groupName) {
        new ExtendedTask<String, Void, UserGroupVisibleMembers>(handler) {
            @Override
            protected UserGroupVisibleMembers doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().getUserGroupVisibleMembers(params[0]).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Die sichtbaren Mitglieder von " + params[0] + " konnten nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um die Sichtbarkeit von einem
     * Mitglied der Nutzergruppe zu ändern. Diese Methode kann verwentet werden, wenn
     * ein Mitglied einer Nutzergruppe nicht von anderen Mitglieder auf der Karte bei
     * laufenden Event gesehen zu werden.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     */
    public void changeMyVisibility(ExtendedTaskDelegate handler, String groupName) {
        new ExtendedTask<String, Void, UserGroupVisibleMembers>(handler) {
            @Override
            protected UserGroupVisibleMembers doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().changeMyVisibility(params[0]).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Ihre Sichtbarkeit konnte nicht geändert werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um das Passwort einer Nutzergruppe
     * zu ändern. Dabei muss das derzeitige Passwort angegeben werden.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     * @param currentPw Das derzeitige Passwort der Nutzergruppe kann null sein
     * @param newPw     Das neue Passwort der Nutzergruppe kann nicht null sein
     */
    public void changeUserGroupPassword(ExtendedTaskDelegate handler, String groupName, final String currentPw, final String newPw) {
        new ExtendedTask<String, Void, BooleanWrapper>(handler) {
            @Override
            protected BooleanWrapper doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().changeUserGroupPassword(params[0], currentPw, newPw).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Das Passwort für " + params[0] + " konnte nicht geändert werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um die Öffentlichkeit einer Nutzergruppe
     * zu ändern.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     * @param password  Das Passwort für die private Nutzergruppe
     */
    public void makeUserGroupPrivat(ExtendedTaskDelegate handler, final String groupName, final String password) {
        new ExtendedTask<String, Void, BooleanWrapper>(handler) {
            @Override
            protected BooleanWrapper doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().makeUserGroupPrivat(params[0], password).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Die Gruppe " + groupName + "konnte nicht privat gemacht wreden");
                    return null;
                }

            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um die Öffentlichkeit einer Nutzergruppe
     * zu ändern.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     */
    public void makeUserGroupOpen(ExtendedTaskDelegate handler, final String groupName) {
        new ExtendedTask<String, Void, BooleanWrapper>(handler) {
            @Override
            protected BooleanWrapper doInBackground(String... params) {
                try {
                    return ServiceProvider.getService().groupEndpoint().makeUserGroupOpen(params[0]).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Fehler: Die Gruppe " + groupName + " konnte nicht öffentlich gemacht werden");
                    return null;
                }

            }
        }.execute(groupName);
    }

    /**
     * Methode zum aufräumen von unbenutzten blobkeys.
     *
     * @param handler
     */
    public void deleteUnusedBlobKeys(ExtendedTaskDelegate handler) {
        new ExtendedTask<String, Void, Void>(handler) {
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    return ServiceProvider.getService().groupEndpoint().cleanPreviewPictures().execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }.execute();
    }
}
