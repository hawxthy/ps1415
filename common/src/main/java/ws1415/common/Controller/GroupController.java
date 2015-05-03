package ws1415.common.Controller;

import android.graphics.Picture;

import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.UserGroup;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.task.QueryHostsTask;

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

    public void setVisibility(UserGroup u, boolean visibility){
        //TODO Lösung für den PrefManager finden.
    }

    public void sendInvitation(ExtendedTaskDelegate handler, UserGroup u, String user, String message){
        //TODO Implementieren.
    }

    public boolean removeMember(ExtendedTaskDelegate handler, UserGroup u, String user){
        //TODO Implementieren.
        return true;
    }

    public void sendGlobalMessage(ExtendedTaskDelegate handler, UserGroup u, String message){
        //TODO Implementieren.
    }

    public boolean sendMessage(ExtendedTaskDelegate handler, UserGroup u, String message, String user){
        //TODO Implementieren.
        return true;
    }

    public boolean postBlackBoard(ExtendedTaskDelegate handler, UserGroup u, String Message){
        //TODO Implementieren.
        return true;
    }

    public boolean deleteBoardMessage(ExtendedTaskDelegate handler, UserGroup u, BoardEntry be){
        //TODO Implementieren.
        return true;
    }

    public boolean changePicture(ExtendedTaskDelegate handler, UserGroup u, Picture p){
        //TODO Implementieren.
        return true;
    }
}
