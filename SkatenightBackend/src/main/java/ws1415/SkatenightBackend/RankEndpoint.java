package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;

import java.util.List;

import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.Rank;
import ws1415.SkatenightBackend.model.UserGroup;

/**
 * Created by Bernd Eissing on 03.05.2015.
 */
public class RankEndpoint extends SkatenightServerEndpoint{

    public BooleanWrapper createRank( @Named("rankName") String name,@Named("rights") List<Integer> rights){
        //TODO noch zu implementieren.
        return new BooleanWrapper(true);
    }

    public BooleanWrapper deleteRank(UserGroup u, @Named("rankName") String name){
        //TODO noch zu implementieren.
        return new BooleanWrapper(true);
    }

    public BooleanWrapper alterRank(UserGroup u, @Named("rankName") String name){
        //TODO noch zu implementieren.
        return new BooleanWrapper(true);
    }

    public BooleanWrapper passLeader(UserGroup u, @Named("name") String user){
        //TODO noch zu implementieren.
        return new BooleanWrapper(true);
    }

    public BooleanWrapper giveRank(UserGroup u, @Named("name") String user, @Named("rankName") String rank){
        //TODO noch zu implementieren.
        return new BooleanWrapper(true);
    }

    public BooleanWrapper takeRank(UserGroup u, @Named("userName") String user, @Named("rankName") String rank){
        //Eventuell nutzlos, da giveRank die selbe Funktion hat.
        return new BooleanWrapper(true);
    }
}
