package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.UserListData;

import java.io.IOException;
import java.util.List;

import ws1415.common.model.GlobalRole;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;

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
    public static void assignGlobalRole(ExtendedTaskDelegate<Void, Void> handler, final String userMail,
                                        final GlobalRole role) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().roleEndpoint().assignGlobalRole(role.getId(), userMail).execute();
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
    public static void listGlobalAdmins(ExtendedTaskDelegate<Void, List<UserListData>> handler){
        new ExtendedTask<Void, Void, List<UserListData>>(handler) {
            @Override
            protected List<UserListData> doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().roleEndpoint().listGlobalAdmins().execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Administratoren konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }


}
