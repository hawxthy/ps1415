package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.Rank;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.io.IOException;
import java.util.List;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.util.Right;

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

    public boolean createRank(ExtendedTaskDelegate handler, String groupName, String describtion, String name, List<String> rights){
        final String groupNameFinal = groupName;
        final String describtionFinal = describtion;
        new ExtendedTask<List<String>, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(List<String>... params){
                try{
                    return ServiceProvider.getService().rankEndpoint().createRank("rankName", params[0], groupNameFinal, describtionFinal).execute();
                }catch(IOException e){
                    publishError("Der Rank konnte nicht erstellt werden");
                    return null;
                }
            }
        }.execute(rights);
        return true;
    }

    public boolean deleteRank(ExtendedTaskDelegate handler, Rank rank, String groupName){
        final String groupNameFinal = groupName;
        new ExtendedTask<Rank, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(Rank... params){
                try{
                    return ServiceProvider.getService().rankEndpoint().deleteRank(groupNameFinal, params[0]).execute();
                }catch(IOException e){
                    publishError("Der Rank konnte nicht gelöscht werden");
                    return null;
                }
            }
        }.execute(rank);
        return true;
    }

    public boolean alterRank(ExtendedTaskDelegate handler, Rank rank, String groupName){
        final String groupNameFinal = groupName;
        new ExtendedTask<Rank, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(Rank... params){
                try{
                    return ServiceProvider.getService().rankEndpoint().alterRank(groupNameFinal, params[0]).execute();
                }catch(IOException e){
                    publishError("Der Rank konnte nicht verändert werden");
                    return null;
                }
            }
        }.execute(rank);
        return true;
    }

    public boolean passLeader(ExtendedTaskDelegate handler, Rank rank, String groupName, final String newLeader, final String oldLeader){
        final String groupNameFinal = groupName;
        final String newLeaderFinal = newLeader;
        final String oldLeaderFinal = oldLeader;
        new ExtendedTask<Rank, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(Rank... params){
                try{
                    return ServiceProvider.getService().rankEndpoint().passLeader(groupNameFinal, newLeader, oldLeader, params[0]).execute();
                }catch(IOException e){
                    publishError("Der Leader konnte nicht vergeben werden");
                    return null;
                }
            }
        }.execute(rank);
        return true;
    }

    public boolean giveRank(ExtendedTaskDelegate handler, Rank rank, String groupName, String user){
        final String groupNameFinal = groupName;
        final String userfinal = user;
        new ExtendedTask<Rank, Void, BooleanWrapper>(handler){
            @Override
            protected BooleanWrapper doInBackground(Rank... params){
                try{
                    return ServiceProvider.getService().rankEndpoint().giveRank(userfinal, groupNameFinal, params[0]).execute();
                }catch(IOException e){
                    publishError("Der Rank konnte nicht vergeben werden");
                    return null;
                }
            }
        }.execute(rank);
        return true;
    }

    public boolean takeRank(ExtendedTaskDelegate handler, UserGroup u, String user, Rank rank){
        //Eventuell nutzlos, da giveRank die selbe Funktion hat.
        return true;
    }
}
