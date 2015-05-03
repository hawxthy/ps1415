package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.Rank;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.List;

import ws1415.common.task.ExtendedTaskDelegate;

/**
 * Created by Bernd Eissing on 02.05.2015.
 */
public class RankController {
    private RankController instance;

    private RankController(){}

    public RankController getInstance(){
        if(instance == null){
            instance = new RankController();
        }
        return instance;
    }

    public boolean createRank(ExtendedTaskDelegate handler, UserGroup u, String name, List<Integer> rights){
        //TODO noch zu implementieren.
        return true;
    }

    public boolean deleteRank(ExtendedTaskDelegate handler, UserGroup u, String name){
        //TODO noch zu implementieren.
        return true;
    }

    public boolean alterRank(ExtendedTaskDelegate handler, UserGroup u, String name){
        //TODO noch zu implementieren.
        return true;
    }

    public boolean passLeader(ExtendedTaskDelegate handler, UserGroup u, String user){
        //TODO noch zu implementieren.
        return true;
    }

    public boolean giveRank(ExtendedTaskDelegate handler, UserGroup u, String user, Rank rank){
        //TODO noch zu implementieren.
        return true;
    }

    public boolean takeRank(ExtendedTaskDelegate handler, UserGroup u, String user, Rank rank){
        //Eventuell nutzlos, da giveRank die selbe Funktion hat.
        return true;
    }
}
