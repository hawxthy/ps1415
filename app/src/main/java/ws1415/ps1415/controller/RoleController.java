package ws1415.ps1415.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.model.GlobalRole;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Der RoleController steuert die Verwaltung der Rollen der Benutzer auf dem Server.
 *
 * @author Martin Wrodarczyk
 */
public abstract class RoleController {
    /**
     * Weist einem Benutzer eine globale Rolle zu.
     *
     * @param handler Auszuführender Task
     * @param userMail E-Mail Adresse des Benutzers
     * @param role Neue Rolle
     */
    public static void assignGlobalRole(ExtendedTaskDelegate<Void, Boolean> handler, final String userMail,
                                        final GlobalRole role) {
        new ExtendedTask<Void, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().roleEndpoint().assignGlobalRole(role.getId(), userMail).execute().getValue();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Rolle des Benutzers konnte nicht gesetzt werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Gibt eine Liste von globalen Administratoren aus.
     *
     * @param handler Auszuführender Task
     */
    public static void listGlobalAdmins(ExtendedTaskDelegate<Void, List<String>> handler){
        new ExtendedTask<Void, Void, List<String>>(handler) {
            @Override
            protected List<String> doInBackground(Void... params) {
                try {
                    List<String> result = ServiceProvider.getService().roleEndpoint().listGlobalAdmins().execute().getStringList();
                    if(result == null) return new ArrayList<>();
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Administratoren konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }



    /**
     * Prüft, ob der Benutzer mit der angegebenen E-Mail ein globaler Administrator ist.
     * @param handler    Der Handler, dem der Boolean-Wert übergeben wird.
     * @param email      Die zu prüfende E-Mail.
     */
    public static void isAdmin(ExtendedTaskDelegateAdapter<Void, Boolean> handler, final String email) {
        new ExtendedTask<Void, Void, Boolean>(handler) {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().roleEndpoint().isAdmin(email).execute().getValue();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Administrator-Status konnte nicht geprüft werden");
                }
                return false;
            }
        }.execute();
    }
}
