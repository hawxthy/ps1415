package ws1415.common.controller;

import java.io.IOException;
import java.util.List;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;

/**
 * Created by Bernd Eissing on 02.05.2015.
 */
public class RightController {
    private RightController instance;

    private RightController(){}

    public RightController getInstance(){
        if(instance == null){
            instance = new RightController();
        }
        return instance;
    }

    /**
     * Methode, welche mit dem GroupEndoint kommunizert um einem Mitglied einer
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
     * Methode, welche mit dem GroupEndoint kommunizert um einem Mitglied einer
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
                    return ServiceProvider.getService().rightEndpoint().distributeRightsToUser(params[0], rightNames, userName).execute();
                }catch(IOException e){
                    publishError("Die Rechte konnten nicht verteilt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndoint kommunizert Mitgliedern einer
     * Nutzergruppe ein neues Recht zu geben.
     *
     * @param handler Der Task, der mit dem Server kommuniziert.
     * @param groupName Der Name der Nutzergruppe
     * @param userNames Die EndUser
     * @param rightName Das Recht
     */
    public void giveRightToUsers(ExtendedTaskDelegate handler, String groupName, final List<String> userNames, final String rightName){
        new ExtendedTask<String, Void, Void>(handler){
            @Override
            protected Void doInBackground(String... params){
                try{
                    return ServiceProvider.getService().rightEndpoint().distributeRightToUsers(params[0], rightName, userNames).execute();
                }catch(IOException e){
                    publishError("Das Recht konnt nicht verteilt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndoint kommunizert Mitgliedern einer
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
                    return ServiceProvider.getService().rightEndpoint().distributeRightsToUsers(params[0], rightNames, userNames).execute();
                }catch(IOException e){
                    publishError("Das Recht konnt nicht verteilt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndoint kommunizert um einem Mitglied einer
     * Nutzergruppe ein neues Recht zu entziehen.
     *
     * @param handler Der Task, der mit dem Server kommuniziert.
     * @param groupName Der Name der Nutzergruppe
     * @param userName Der EndUser
     * @param rightName Das Recht
     */
    public void takeRankFromUser(ExtendedTaskDelegate handler, String groupName, final String userName, final String rightName){
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
     * Methode, welche mit dem GroupEndoint kommunizert um einem Mitglied einer
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
                    return ServiceProvider.getService().rightEndpoint().takeRightsFromUser(params[0], rightNames, userName).execute();
                }catch(IOException e){
                    publishError("Das Recht konnt nicht verteilt werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndoint kommunizertum Mitgliedern einer
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
                    return ServiceProvider.getService().rightEndpoint().takeRightFromUsers(params[0], rightName, userNames).execute();
                }catch(IOException e){
                    publishError("Das Recht konnt nicht entzogen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }

    /**
     * Methode, welche mit dem GroupEndoint kommunizert um Mitgliedern einer
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
                    return ServiceProvider.getService().rightEndpoint().takeRightsFromUsers(params[0], rightNames, userNames).execute();
                }catch(IOException e){
                    publishError("Die Rechte konnten nicht entzogen werden");
                    return null;
                }
            }
        }.execute(groupName);
    }
}
