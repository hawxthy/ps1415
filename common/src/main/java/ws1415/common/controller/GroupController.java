package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.Picture;
import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupBlackBoardTransport;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;
import com.skatenight.skatenightAPI.model.UserGroupNewsBoardTransport;
import com.skatenight.skatenightAPI.model.UserGroupPicture;
import com.skatenight.skatenightAPI.model.UserGroupVisibleMembers;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

import ws1415.common.model.UserGroupType;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.task.QueryUserGroupsTask;

/**
 * Created by Bernd Eissing on 02.05.2015.
 */
public class GroupController {
    private static GroupController instance;

    private GroupController(){}

    public static GroupController getInstance(){
        if(instance == null){
            instance = new GroupController();
        }
        return instance;
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um eine neue
     * UserGroup mit dem übergebenen EndUser als Ersteller zu speichern.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     * @param groupName Der Name der zu erstellenden UserGroup
     */
    public void createUserGroup(ExtendedTaskDelegate handler, String groupName, final boolean isPrivat, final UserGroupType groupType, final String password){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().createUserGroup(params[0], isPrivat, groupType.name(), password).execute();
                }catch(IOException e){
                    publishError("Die Nutzergruppe konnte nicht erstellt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommunizert um eine UserGroup
     * mit dem angegebenen Namen abruft.
     *
     * @param handler Der Task, der mit dem Server kommunizert
     * @param groupName Der Name der UserGroup
     */
    public void getUserGroup(ExtendedTaskDelegate handler, String groupName){
        new ExtendedTask<String, Void, UserGroup>(handler){
            @Override
            protected UserGroup doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().getUserGroup(params[0]).execute();
                }catch(IOException e){
                    publishError("Die Nutzergruppe konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um die Metadaten zu einer
     * Nutzergruppe abzurufen.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     */
    public void getUserGroupMetaData(ExtendedTaskDelegate handler, String groupName){
        new ExtendedTask<String, Void, UserGroupMetaData>(handler){
            @Override
            protected  UserGroupMetaData doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().getUserGroupMetaData(params[0]).execute();
                }catch(IOException e){
                    publishError("Die Metadaten der Nutzergruppe konnte nnicht abgerufen werden");
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
    public void getUserGroupMetaDatas(ExtendedTaskDelegate handler){
        new ExtendedTask<Void, Void, List<UserGroupMetaData>>(handler){
            @Override
            protected  List<UserGroupMetaData> doInBackground(Void... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().getAllUserGroupMetaDatas().execute().getItems();
                }catch(IOException e){
                    e.printStackTrace();
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint via den QueryUserGroupsTask
     * kommuniziert um alle UserGroups zu laden
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void getAllUserGroups(ExtendedTaskDelegate handler){
        new ExtendedTask<Void, Void, List<UserGroup>>(handler){
            @Override
            protected List<UserGroup> doInBackground(Void... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().getAllUserGroups().execute().getItems();
                }catch(IOException e){
                    publishError("Die Nutzergruppen konnten nicht abgerufen werden");
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
    public void getMyUserGroups(ExtendedTaskDelegate handler){
        new ExtendedTask<Void, Void, List<UserGroup>>(handler){
            @Override
            protected List<UserGroup> doInBackground(Void... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().fetchMyUserGroups().execute().getItems();
                }catch(IOException e){
                    publishError("Die Nutzergruppen konnten nicht abgerufen werden");
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
    public void deleteUserGroup(ExtendedTaskDelegate handler, String groupName){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().deleteUserGroup(params[0]).execute();
                }catch(IOException e){
                    publishError("Die Nutzergruppe konnte nicht gelöscht werden");
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
     * @param password Das Password, falls eins beötigt wird kann leer aber nicht null sein
     */
    public void joinUserGroup(ExtendedTaskDelegate handler, String groupName, final String password){
        new ExtendedTask<String, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().joinUserGroup(params[0], password).execute();
                }catch(IOException e){
                    publishError("Der Nutzergruppe konnte beigetreten werden");
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
    public void leaveUserGroup(ExtendedTaskDelegate handler, String groupName){
        //TODO Eventuell statt den EndUser direkt nur die E-Mail übergeben
        final String groupNameFinal = groupName;
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().leaveUserGroup(params[0]).execute();
                }catch(IOException e){
                    publishError("Es war nicht möglich die Nutzergruppe zu verlassen");
                    return null;
                }
            }
        }.execute(groupName);
    }

    public void setVisibility(UserGroup u, boolean visibility){
        //TODO Lösung für den PrefManager finden.
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um Einladungen in Form einer Notification
     * an den übergebenen EndUser zu schicken.
     *
     * @param handler Der Task, der den Server anspricht
     * @param groupName die UserGroup zu der die Einladung verschickt wird
     * @param user Der EndUser, der die Nachricht erhalten soll
     * @param message Eine Mitteilung die der Nachricht mitgegeben wird
     */
    public void sendInvitation(ExtendedTaskDelegate handler, String groupName, final String user, final String message){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().sendInvitation(params[0], user, message).execute();
                }catch(IOException e){
                    publishError("Die Einladung konnte nicht versendet werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEnpoint kommuniziert um
     * den übergenen EndUser aus der übergebenen UseGroup löscht.
     *
     * @param handler Task, der den Server anspricht
     * @param groupName Die UserGroup dessen EndUser gelöscht werden soll
     * @param user Die E-Mail des EndUsers, der gelöscht werden soll
     * @return Meldung über Erfolg des Löschens
     *          true = Löschen war erfolgreich
     *          false = Löschen war nicht erfolgreich
     */
    public void removeMember(ExtendedTaskDelegate handler, String groupName, final String user){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().removeMember(params[0], user).execute();
                }catch (IOException e){
                    publishError("Member konnte nicht entfernt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndoint kommuniziert um eine Nachricht an das
     * BlackBoard der übergebenen UserGroup zu schicken.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     * @param groupName Die UserGroup, dessen BlackBoard eine neue Nachricht erhalten soll
     * @param boardMessage Die Nachricht
     */
    public void postBlackBoard(ExtendedTaskDelegate handler, String groupName, final String boardMessage){
        //TODO muss noch um Bilder erweitert werden
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().postBlackBoard(params[0], boardMessage).execute();
                }catch (IOException e){
                    publishError("Es konnte nicht auf dem Blackboard gepostet werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziet um eine Nachricht vom BlackBoard
     * der übergebenen UserGroup zu löschen.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     * @param group Die UserGroup, deren BlackBoard Nachricht gelöscht werden soll
     * @param boardEntryId Der BoardEntry der gelöscht werden soll
     */
    public void deleteBoardMessage(ExtendedTaskDelegate handler, final String group, final Long boardEntryId){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().deleteBlackBoardMessage(boardEntryId, params[0]).execute();
                }catch (IOException e){
                    publishError("Member konnte nicht entfernt werden");
                    return null;
                }
            }
        }.execute(group);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommunizert um von eine Nutzergruppe
     * dessen BlackBoard abzurufen.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     * @param groupName Der Name der Nutzergruppe
     */
    public void getBlackBoard(ExtendedTaskDelegate handler, String groupName){
        new ExtendedTask<String, Void, UserGroupBlackBoardTransport>(handler){
            @Override
            protected  UserGroupBlackBoardTransport doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().getUserGroupBlackBoard(params[0]).execute();
                }catch(IOException e){
                    publishError("Das Blackboard der Nutzergruppe konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um eine Nachricht an das NewsBoard
     * einer Nutzergruppe zu posten.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     * @param newsMessage Der Inhalt der Nachricht
     */
    public void postNewsBoard(ExtendedTaskDelegate handler, String groupName, final String newsMessage){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected  Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().postNewsBoard(params[0], newsMessage).execute();
                }catch(IOException e){
                    publishError("Die Nachricht konnte nicht gepostet werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndoint kommunizert um eine NewsMessage einer
     * Nutzergruppe zu löschen.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     * @param boardEntryId Die Id der BoardEntries
     */
    public void deleteNewsMessage(ExtendedTaskDelegate handler, String groupName, final Long boardEntryId){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().deleteNewsBoardMessage(boardEntryId, params[0]).execute();
                }catch(IOException e){
                    e.printStackTrace();
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um das NewsBoard einer
     * Nutzergruppe abzurufen.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     */
    public void getNewsBoard(ExtendedTaskDelegate handler, String groupName){
        new ExtendedTask<String, Void, UserGroupNewsBoardTransport>(handler){
            @Override
            protected  UserGroupNewsBoardTransport doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().getUserGroupNewsBoard(params[0]).execute();
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um Nachrichten an alle Mitglieder
     * der übergebenen UserGroup zu schicken.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     * @param group Die UserGroup deren Mitglieder benachricht werden sollen
     * @param message Die Nachricht
     */
    public void sendGlobalMessage(ExtendedTaskDelegate handler, UserGroup group, String message){
        final String messageFinal = message;
        new ExtendedTask<UserGroup, Void, Void>(handler){
            @Override
            protected Void doInBackground(UserGroup... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().sendGlobalMessage(messageFinal, params[0]).execute();
                }catch (IOException e){
                    publishError("Eine Nachricht an alle Mitglieder konnte nicht versendet werden");
                    return null;
                }
            }
        }.execute(group);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um eine Nachricht an, eventuell nutzlos
     *
     * @param handler
     * @param group
     * @param message
     * @param user
     * @return
     */
    public boolean sendMessage(ExtendedTaskDelegate handler, UserGroup group, String message, String user){
        final String messageFinal = message;
        final String userFinal = user;
        new ExtendedTask<UserGroup, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(UserGroup... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().sendMessage(messageFinal, userFinal).execute();
                }catch (IOException e){
                    publishError("Die Nachricht konnte nicht versendet werden");
                    return null;
                }
            }
        }.execute(group);
        return true;
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um das Bild einer
     * UserGroup zu ändern.
     * //TODO Kommentar anpassen
     * @param handler Der Task, der mit dem Server kommuniziert
     * @param groupName Die UserGroup dessen Bild geändert werden soll
     * @return Nachricht über das Ergebnis des änderns
     *          true = Das Bild wurde erfolgreich gelöscht
     *          false = Das Bild wurde nicht gelöscht, Fehler
     */
    public void changePicture(ExtendedTaskDelegate handler, String groupName, final FileInputStream pictureFile){
        new ExtendedTask<String, Void, UserGroupPicture>(handler){
            UserGroupPicture picture;
            @Override
            protected UserGroupPicture doInBackground(String... params){
                try{
                    picture = ServiceProvider.getService().groupEndpoint().changePicture(params[0]).execute();
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(picture.getPictureUrl());

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addPart("file", new InputStreamBody(pictureFile, "file"));
                    builder.addTextBody("id", picture.getId().toString());

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
                    picture.setPictureBlobKey(blobKey);
                }catch(IOException e){
                    e.printStackTrace();
                }
                return picture;
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
    public void getUserGroupPicture(ExtendedTaskDelegate handler, String groupName){
        new ExtendedTask<String, Void, UserGroupPicture>(handler){
            @Override
            protected UserGroupPicture doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().getUserGroupPicture(params[0]).execute();
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um die sichtbaren Mitglieder einer
     * Nutzergruppe zu erhalten.
     *
     * @param handler
     * @param groupName Der Name der Nutzergruppe
     */
    public void getUserGroupVisibleMembers(ExtendedTaskDelegate handler, String groupName){
        new ExtendedTask<String, Void, UserGroupVisibleMembers>(handler){
            @Override
            protected UserGroupVisibleMembers doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().getUserGroupVisibleMembers(params[0]).execute();
                }catch (IOException e){
                    e.printStackTrace();
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
    public void changeMyVisibility(ExtendedTaskDelegate handler, String groupName){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().changeMyVisibility(params[0]).execute();
                }catch (IOException e){
                    e.printStackTrace();
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
     * @param newPw Das neue Passwort der Nutzergruppe kann nicht null sein
     */
    public void changeUserGroupPassword(ExtendedTaskDelegate handler, String groupName, final String currentPw, final String newPw){
        new ExtendedTask<String, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().changeUserGroupPassword(params[0], currentPw, newPw).execute();
                }catch (IOException e){
                    e.printStackTrace();
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
     * @param privat Die Öffentlichkeit true = privat, false = offen
     */
    public void changeUserGroupPrivacy(ExtendedTaskDelegate handler, String groupName, final boolean privat){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().changeUserGroupPrivacy(params[0], privat).execute();
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }

            }
        }.execute(groupName);
    }
}
