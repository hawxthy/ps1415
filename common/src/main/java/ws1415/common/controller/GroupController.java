package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.EndUser;
import com.skatenight.skatenightAPI.model.Picture;
import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupBlackBoard;
import com.skatenight.skatenightAPI.model.UserGroupBlackBoardTransport;
import com.skatenight.skatenightAPI.model.UserGroupPicture;

import java.io.IOException;
import java.security.acl.Group;
import java.util.List;

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
    public void createUserGroup(ExtendedTaskDelegate handler, String groupName, final boolean isOpen){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().createUserGroup(params[0], isOpen).execute();
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
     */
    public void joinUserGroup(ExtendedTaskDelegate handler, String groupName){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().joinUserGroup(params[0]).execute();
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
    public void deleteBoardMessage(ExtendedTaskDelegate handler, final String group, final long boardEntryId){
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
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     * @param group Die UserGroup dessen Bild geändert werden soll
     * @param p Das neue Bild
     * @return Nachricht über das Ergebnis des änderns
     *          true = Das Bild wurde erfolgreich gelöscht
     *          false = Das Bild wurde nicht gelöscht, Fehler
     */
    public boolean changePicture(ExtendedTaskDelegate handler, String group, UserGroupPicture p){
        final String groupFinal = group;
        new ExtendedTask<UserGroupPicture, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(UserGroupPicture... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().changePicture(groupFinal, params[0]).execute();
                }catch (IOException e){
                    publishError("Member konnte nicht entfernt werden");
                    return null;
                }
            }
        }.execute(p);
        return true;
    }
}
