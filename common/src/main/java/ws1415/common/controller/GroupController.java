package ws1415.common.controller;

import com.skatenight.skatenightAPI.SkatenightAPI;
import com.skatenight.skatenightAPI.model.Picture;
import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.AddUserGroupTask;
import ws1415.common.task.DeleteUserGroupTask;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.task.JoinUserGroupTask;
import ws1415.common.task.LeaveUserGroupTask;
import ws1415.common.task.QueryMyUserGroupsTask;
import ws1415.common.task.QueryUserGroupsTask;

/**
 * Created by Bernd Eissing on 02.05.2015.
 */
public class GroupController {
    private GroupController instance;

    private GroupController(){}

    public GroupController getInstance(){
        if(instance == null){
            instance = new GroupController();
        }
        return instance;
    }

    /**
     * Methode, welche mit dem GroupEndpoint via den QueryUserGroupsTask
     * kommuniziert um alle UserGroups zu laden
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void getAllUserGroups(ExtendedTaskDelegate handler){
        new QueryUserGroupsTask(handler).execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint via den QueryMyUserGroupsTask
     * kommuniziert um alle UserGroups zum angemeldeten EndUser zu laden
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void getMyUserGroups(ExtendedTaskDelegate handler){
        new QueryMyUserGroupsTask(handler).execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint via den QueryMyUserGroupsTask
     * kommuniziert um eine neue UserGroup zu erstellen
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void createUserGroup(ExtendedTaskDelegate handler){
        new ExtendedTask<EndUser>()
        try{
            ServiceProvider.getService().groupEndpoint().createUserGroup(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Methode, welche mit dem GroupEndpoint via den DeleteUserGroupTask
     * kommuniziert um eine UserGroup zu löschen
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void deleteUserGroup(ExtendedTaskDelegate handler){
        new DeleteUserGroupTask(handler).execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint via den JoinUserGroupTask
     * kommuniziert um einen EndUser einer UserGroup zuzuordnen
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void joinUserGroup(ExtendedTaskDelegate handler){
        new JoinUserGroupTask(handler).execute();
    }

    /**
     * Methode, welche mit dem GroupEndpoint via den LeaveUserGroupTask
     * kommuniziert um einen EndUser aus einer UserGroup zu entfernen
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     */
    public void leaveUserGroup(ExtendedTaskDelegate handler){
        new LeaveUserGroupTask(handler).execute();
    }

    public void setVisibility(UserGroup u, boolean visibility){
        //TODO Lösung für den PrefManager finden.
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziert um Einladungen in Form einer Notification
     * an den übergebenen EndUser zu schicken.
     *
     * @param handler Der Task, der den Server anspricht
     * @param group die UserGroup zu der die Einladung verschickt wird
     * @param user Der EndUser, der die Nachricht erhalten soll
     * @param message Eine Mitteilung die der Nachricht mitgegeben wird
     */
    public void sendInvitation(ExtendedTaskDelegate handler, UserGroup group, String user, String message){
        final String userFinal = user;
        final String messageFinal = message;
        new ExtendedTask<UserGroup, Void, Void>(handler){
            @Override
            protected Void doInBackground(UserGroup... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().sendInvitation(userFinal, messageFinal, params[0]).execute();
                }catch(IOException e){
                    publishError("Die Einladung konnte nicht versendet werden");
                    return null;
                }
            }
        }.execute(group);
    }

    /**
     * Methode, welche mit dem GroupEnpoint kommuniziert um
     * den übergenen EndUser aus der übergebenen UseGroup löscht.
     *
     * @param handler Task, der den Server anspricht
     * @param group Die UserGroup dessen EndUser gelöscht werden soll
     * @param user Die E-Mail des EndUsers, der gelöscht werden soll
     * @return Meldung über Erfolg des Löschens
     *          true = Löschen war erfolgreich
     *          false = Löschen war nicht erfolgreich
     */
    public boolean removeMember(ExtendedTaskDelegate handler, UserGroup group, String user){
        final String userFinal = user;
        new ExtendedTask<UserGroup, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(UserGroup... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().removeMember(userFinal, params[0]).execute();
                }catch (IOException e){
                    publishError("Member konnte nicht entfernt werden");
                    return null;
                }
            }
        }.execute(group);
        return true;
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
     * Methode, welche mit dem GroupEndoint kommuniziert um eine Nachricht an das
     * BlackBoard der übergebenen UserGroup zu schicken.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     * @param group Die UserGroup, dessen BlackBoard eine neue Nachricht erhalten soll
     * @param message Die Nachricht
     * @return Meldung über Erfolg der Postens
     *          true = Die Nachricht wurde erfolgreich gepostet
     *          false = Die Nachricht wurde nicht gepostet, Fehler
     */
    public boolean postBlackBoard(ExtendedTaskDelegate handler, UserGroup group, String message, String writer){
        //TODO muss noch um Bilder erweitert werden
        final String messageFinal = message;
        final String writerFinal = writer;
        new ExtendedTask<UserGroup, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(UserGroup... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().postBlackBoard(messageFinal, writerFinal, params[0]).execute();
                }catch (IOException e){
                    publishError("Member konnte nicht entfernt werden");
                    return null;
                }
            }
        }.execute(group);
        return true;
    }

    /**
     * Methode, welche mit dem GroupEndpoint kommuniziet um eine Nachricht vom BlackBoard
     * der übergebenen UserGroup zu löschen.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     * @param group Die UserGroup, deren BlackBoard Nachricht gelöscht werden soll
     * @param be Der BoardEntry der gelöscht werden soll
     * @return Nachricht über das Ergebnis des Löschens
     *          true = die Nachricht wurde erfolgreich gelöscht.
     *          false = die Nachricht wurde nicht gelöscht, Fehler
     */
    public boolean deleteBoardMessage(ExtendedTaskDelegate handler, String group, BoardEntry be){
        final String groupFinal = group;
        new ExtendedTask<BoardEntry, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(BoardEntry... params){
                try{
                    return ServiceProvider.getService().groupEndpoint().deleteBoardMessage(groupFinal, params[0]).execute();
                }catch (IOException e){
                    publishError("Member konnte nicht entfernt werden");
                    return null;
                }
            }
        }.execute(be);
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
    public boolean changePicture(ExtendedTaskDelegate handler, String group, Picture p){
        final String groupFinal = group;
        new ExtendedTask<Picture, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(Picture... params){
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
