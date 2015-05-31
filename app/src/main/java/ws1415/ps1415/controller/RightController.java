package ws1415.ps1415.controller;

import com.skatenight.skatenightAPI.model.ListWrapper;

import java.io.IOException;
import java.util.List;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;

/**
 * Created by Bernd Eissing on 02.05.2015.
 */
public class RightController {
    private static RightController instance;

    private RightController(){}

    public static RightController getInstance(){
        if(instance == null){
            instance = new RightController();
        }
        return instance;
    }

    /**
     * Methode, welche mit dem RightEndpoint kommunizert um einem Mitglied einer
     * Nutzergruppe ein neues Recht zu geben.
     *
     * @param handler Der Task, der mit dem Server kommuniziert.
     * @param groupName Der Name der Nutzergruppe
     * @param userName Der EndUser der ein neues Recht erhalten soll
     * @param rightName Der Names des neuen Rechtes
     */
    public void giveRightToUser(ExtendedTaskDelegate handler, String groupName, final String userName, final String rightName){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().rightEndpoint().distributeRightToUser(params[0], rightName, userName).execute();
                }catch(IOException e){
                    publishError("Das Recht konnt nicht verteilt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem RightEndpoint kommunizert um einem Mitglied einer
     * Nutzergruppe neue Rechte zu geben.
     *
     * @param handler Der Task, der mit dem Server kommuniziert.
     * @param groupName Der Name der Nutzergruppe
     * @param userName Der EndUser
     * @param rightNames Die Rechte
     */
    public void giveRightsToUser(ExtendedTaskDelegate handler, String groupName, final String userName, final List<String> rightNames){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().rightEndpoint().distributeRightsToUser(params[0], userName, new ListWrapper().setStringList(rightNames)).execute();
                }catch(IOException e){
                    publishError("Die Rechte konnten nicht verteilt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem RightEndpoint kommunizert Mitgliedern einer
     * Nutzergruppe ein neues Recht zu geben.
     *
     * @param handler Der Task, der mit dem Server kommuniziert.
     * @param groupName Der Name der Nutzergruppe
     * @param userNames Die EndUser
     * @param rightName Das Recht
     */
    public void giveRightToUsers(ExtendedTaskDelegate handler, final String groupName, List<String> userNames, final String rightName){
        new ExtendedTask<List<String>, Void, Void>(handler){
            @Override
            protected Void doInBackground(List<String>... params){
                try{
                    return ServiceProvider.getService().rightEndpoint().distributeRightToUsers(groupName, rightName, new ListWrapper().setStringList(params[0])).execute();
                }catch(IOException e){
                    publishError("Das Recht konnt nicht verteilt werden");
                    return null;
                }
            }
        }.execute(userNames);
    }

    /**
     * Methode, welche mit dem RightEndpoint kommunizert Mitgliedern einer
     * Nutzergruppe neue Rechte zu geben.
     *
     * @param handler Der Task, der mit dem Server kommuniziert.
     * @param groupName Der Name der Nutzergruppe
     * @param userNames Die EndUser
     * @param rightNames Die Rechte
     */
    public void giveRightsToUsers(ExtendedTaskDelegate handler, String groupName, final List<String> userNames, final List<String> rightNames){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().rightEndpoint().distributeRightsToUsers(params[0], userNames, new ListWrapper().setStringList(rightNames)).execute();
                }catch(IOException e){
                    publishError("Das Recht konnte nicht verteilt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem RightEndpoint kommunizert um einem Mitglied einer
     * Nutzergruppe ein neues Recht zu entziehen.
     *
     * @param handler Der Task, der mit dem Server kommuniziert.
     * @param groupName Der Name der Nutzergruppe
     * @param userName Der EndUser
     * @param rightName Das Recht
     */
    public void takeRightFromUser(ExtendedTaskDelegate handler, String groupName, final String userName, final String rightName){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().rightEndpoint().takeRightFromUser(params[0], rightName, userName).execute();
                }catch(IOException e){
                    publishError("Das Recht konnt nicht verteilt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem RightEndpoint kommunizert um einem Mitglied einer
     * Nutzergruppe Rechte zu entziehen.
     *
     * @param handler Der Task, der mit dem Server kommuniziert.
     * @param groupName Der Name der Nutzergruppe
     * @param userName Der EndUser
     * @param rightNames Die Rechte
     */
    public void takeRightsFromUser(ExtendedTaskDelegate handler, String groupName, final String userName, final List<String> rightNames){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().rightEndpoint().takeRightsFromUser(params[0], userName, new ListWrapper().setStringList(rightNames)).execute();
                }catch(IOException e){
                    publishError("Das Recht konnt nicht verteilt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem RightEndpoint kommunizertum Mitgliedern einer
     * Nutzergruppe ein Recht zu entziehen.
     *
     * @param handler Der Task, der mit dem Server kommuniziert.
     * @param groupName Der Name der Nutzergruppe
     * @param userNames Die EndUser
     * @param rightName Das Recht
     */
    public void takeRightFromUsers(ExtendedTaskDelegate handler, String groupName, final List<String> userNames, final String rightName){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().rightEndpoint().takeRightFromUsers(params[0], rightName, new ListWrapper().setStringList(userNames)).execute();
                }catch(IOException e){
                    publishError("Das Recht konnt nicht entzogen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem RightEndpoint kommunizert um Mitgliedern einer
     * Nutzergruppe Rechte zu entziehen.
     *
     * @param handler Der Task, der mit dem Server kommuniziert.
     * @param groupName Der Name der Nutzergruppe
     * @param userNames Die EndUser
     * @param rightNames Die Rechte
     */
    public void takeRightsFromUsers(ExtendedTaskDelegate handler, String groupName, final List<String> userNames, final List<String> rightNames){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().rightEndpoint().takeRightsFromUsers(params[0], userNames, new ListWrapper().setStringList(rightNames)).execute();
                }catch(IOException e){
                    publishError("Die Rechte konnten nicht entzogen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem RightEndpoint kommunizert um den Leader einer
     * Nutzergruppe zu ändern.
     *
     * @param handler Der Task, der mit dem Server kommuniziert
     * @param groupName Der Name der Nutzergruppe
     * @param newLeader Der neue Leader
     */
    public void changeLeader(ExtendedTaskDelegate handler, String groupName, final String newLeader){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected  Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().rightEndpoint().changeLeader(params[0], newLeader).execute();
                } catch(IOException e){
                    publishError("Der Leader konnte nicht geändert werden");
                    return null;
                }
            }
        }.execute(groupName);
    }
}
