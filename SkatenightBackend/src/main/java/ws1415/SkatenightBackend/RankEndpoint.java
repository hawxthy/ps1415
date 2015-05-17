package ws1415.SkatenightBackend;

import com.google.api.server.spi.config.Named;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;

import ws1415.SkatenightBackend.model.BooleanWrapper;
import ws1415.SkatenightBackend.model.Rank;
import ws1415.SkatenightBackend.model.Right;
import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.model.EndUser;

/**
 * Created by Bernd Eissing on 03.05.2015.
 */
public class RankEndpoint extends SkatenightServerEndpoint{

    /**
     * Diese Methode erstellt einen neuen Rank mit Rights für die übergebene UserGroup und fügt diesen
     * der Liste von Ranks in der UserGroup hinzu.
     *
     * @param name Der Names der neuen Ranks
     * @param rights Die Liste von Rechten(Right) die dieser Rank haben soll
     * @param groupName Der Name der UserGroup
     * @param describtion Die Beschreibung des Ranks
     * @return BooleanWrapper, eigene Klasse um boolean Werte zurück zu geben
     */
    public BooleanWrapper createRank( @Named("rankName") String name,@Named("rights") List<Right> rights, @Named("groupName") String groupName, @Named("describtion") String describtion){
//        //TODO noch zu implementieren.
//        if(rights == null){
//            throw new NullPointerException("no rights submitted");
//        }
//        if(name == null || name.isEmpty()){
//            throw new IllegalArgumentException("no name for the rank submitted");
//        }
//        if(groupName == null || name.isEmpty()){
//            throw new IllegalArgumentException("no group name submitted");
//        }
//        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
//        try{
//            UserGroup ug = new GroupEndpoint().getUserGroup(groupName);
//            if(ug == null){
//                throw new IllegalArgumentException("a group with the submitted group name does not exist");
//            }
//            Rank rank;
//            if(describtion == null){
//                rank = new Rank(name, "", rights);
//            }else{
//                rank = new Rank(name, describtion, rights);
//            }
//            // "touching" die rights damit sie richtig gespeichert werden
//            rank.getRights();
//            pm.makePersistent(rank);
//            ug.getRanking().add(rank);
//            pm.makePersistent(ug);
//            return new BooleanWrapper(true);
//        }finally{
//            pm.close();
//        }
        return new BooleanWrapper(true);
    }

    /**
     * Diese Methode löscht einen Rank in der übergebenen UserGroup.
     *
     * @param groupName Der Name der UserGroup
     * @param rank Der Rank der gelöscht werden soll
     * @return BooleanWrapper, eigene Klasse um boolean Werte zurück zu geben
     */
    public BooleanWrapper deleteRank(@Named("groupName") String groupName, Rank rank){
//        if(rank == null){
//            throw new NullPointerException("no rank submitted");
//        }
//        if(groupName == null ||groupName.isEmpty()){
//            throw new IllegalArgumentException("no group name submitted");
//        }
//
//        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
//        try{
//            UserGroup ug = new GroupEndpoint().getUserGroup(groupName);
//            if(ug == null){
//                throw new IllegalArgumentException("a group with the submitted group name does not exist");
//            }
//            if(ug.getRanking().remove(rank)){
//                pm.makePersistent(ug);
//            }else{
//                throw new IOException("could not delete the rank");
//            }
//            pm.makePersistent(rank);
//            pm.deletePersistent(rank);
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//        finally{
//            pm.close();
//        }
        return new BooleanWrapper(true);
    }

    /**
     * Diese Methode ändert einen Rank innerhalb der übergebenen UserGroup.
     *
     * @param rank Der Rank der geändert werden soll
     * @param groupName Die UserGroup, dessen Rank geändert werden soll
     * @return BooleanWrapper, eigene Klasse um boolean Werte zurück zu geben
     */
    public BooleanWrapper alterRank(Rank rank, @Named("groupName") String groupName){
//        if(rank == null){
//            throw new NullPointerException("no rank submitted");
//        }
//        if(groupName == null || groupName.isEmpty()){
//            throw new IllegalArgumentException("no group name submitted");
//        }
//        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
//        try{
//            UserGroup ug = new GroupEndpoint().getUserGroup(groupName);
//            if(ug == null){
//                throw new IllegalArgumentException("a group with the submitted group name does not exist");
//            }
//            int index = 0;
//            for(Rank r : ug.getRanking()){
//                if(r.getKey() == rank.getKey()){
//                    ug.getRanking().remove(index);
//                    ug.getRanking().add(index, rank);
//                    return  new BooleanWrapper(true);
//                }
//                index++;
//            }
//            pm.makePersistent(ug);
//        }finally {
//            pm.close();
//        }
        return new BooleanWrapper(false);
    }

    /**
     * Diese Methode ändert den Leader der übergebenen UserGroup und setzt gleichzitigt
     * den Rank des alten Leaders auf den alten Rank des neuen Leaders.
     *
     * @param rank  Der Rank der glaube ich nutzlos ist!
     * @param groupName Der Name der UserGroup, dessen Leader geändert werden soll
     * @param newLeader Die E-Mail des neuen Leaders
     * @param oldLeader Die E-Mail des alten Leaders
     * @return BooleanWrapper, eigene Klasse um bollean Werte zurück zu geben
     */
    public BooleanWrapper passLeader(Rank rank, @Named("groupName") String groupName, @Named("newLeader") String newLeader, @Named("oldLeader") String oldLeader){
//        // TODO rank ist glaube ich nutzlos
//        if(rank == null){
//            throw new NullPointerException("no rank submitted");
//        }
//        if(groupName == null ||groupName.isEmpty()){
//            throw new IllegalArgumentException("no group name submitted");
//        }
//        if(newLeader == null || newLeader.isEmpty()){
//            throw new IllegalArgumentException("no user to be the new Leader submitted");
//        }
//        if(oldLeader == null || oldLeader.isEmpty()){
//            throw new IllegalArgumentException("no user to be the old Leader submitted");
//        }
//        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
//        try{
//            UserGroup ug = new GroupEndpoint().getUserGroup(groupName);
//            if(ug == null){
//                throw new IllegalArgumentException("a group with the submitted group name does not exist");
//            }
//            EndUser newLeaderServer = new UserEndpoint().getFullUser(newLeader);
//            if(newLeaderServer == null){
//                throw new IllegalArgumentException("an enduser(newLeader) with the submitted e-mail does not exist");
//            }
//            EndUser oldLeaderServer = new UserEndpoint().getFullUser(oldLeader);
//            if(oldLeaderServer == null){
//                throw new IllegalArgumentException("an enduser(oldLeader) with the submitted e-mail does not exist");
//            }
//            if(!ug.getMemberRanks().containsKey(newLeaderServer.getEmail())){
//                throw new IllegalArgumentException("the enduser(newLeader) isn't part of the submitted user group");
//            }
//            if(!ug.getMemberRanks().containsKey(oldLeaderServer.getEmail())){
//                throw new IllegalArgumentException("the enduser(oldLeader) isn't part of the submitted user group");
//            }
//            ug.getMemberRanks().put(oldLeaderServer.getEmail(), ug.getMemberRanks().get(newLeaderServer));
//            ug.getMemberRanks().put(newLeaderServer.getEmail(), "Leader");
//            pm.makePersistent(ug);
//            return new BooleanWrapper(true);
//        }finally {
//            pm.close();
//        }
        return new BooleanWrapper(true);
    }

    /**
     * Diese Methode verteilt einen Rank innerhalb der übergebenen UserGroup an den
     * übergebenen EndUser.
     *
     * @param rank Der Rank der dem EndUser in dieser UserGroup gegeben werden soll
     * @param user Der EndUser, der einen neuen Rank erhalten soll
     * @param groupName Der Name der UserGroup, in der ein Rank verteilt werden soll
     * @return BooleanWrapper, eigene Klasse um boolean Werte zurück zu geben
     */
    public BooleanWrapper giveRank(Rank rank, @Named("userName") String user, @Named("groupName") String groupName){
//        if(rank == null){
//            throw new NullPointerException("no rank submitted");
//        }
//        if(groupName == null ||groupName.isEmpty()){
//            throw new IllegalArgumentException("no group name submitted");
//        }
//        if(user == null || user.isEmpty()){
//            throw new IllegalArgumentException("no user submitted");
//        }
//
//        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
//        try{
//            UserGroup ug = new GroupEndpoint().getUserGroup(groupName);
//            if(ug == null){
//                throw new IllegalArgumentException("a group with the submitted group name does not exist");
//            }
//            EndUser e = new UserEndpoint().getFullUser(user);
//            if(e == null){
//                throw new IllegalArgumentException("an enduser with the submitted mail doesn't exist");
//            }
//            ug.getMemberRanks().put(e.getEmail(), rank.getName());
//            pm.makePersistent(ug);
//        return new BooleanWrapper(true);
//    }finally {
//            pm.close();
//        }
        return new BooleanWrapper(true);
    }

    public BooleanWrapper takeRank(UserGroup u, @Named("userName") String user, @Named("rankName") String rank){
        //Eventuell nutzlos, da giveRank die selbe Funktion hat.
        return new BooleanWrapper(true);
    }
}
