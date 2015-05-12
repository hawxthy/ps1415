package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.EndUser;

import java.io.IOException;
import java.util.List;

import ws1415.common.model.GlobalRole;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;

/**
 * Der RoleController steuert die Verwaltung der Rollen der Benutzer zwischen dem Server und den
 * View-Komponenten in der App.
 *
 * @author Martin Wrodarczyk
 */
public class RoleController {
    /**
     * Keine Instanziierung ermöglichen.
     */
    private RoleController() {
    }

    /**
     * Weist einem Benutzer eine globale Rolle zu.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     * @param role Neue Rolle
     */
    public static void assignGlobalRole(ExtendedTaskDelegate handler, final String userMail, final GlobalRole role) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().roleEndpoint().assignGlobalRole(userMail, role.getId()).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Globale Rolle des Benutzers konnte nicht gesetzt werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Gibt eine Liste von globalen Administratoren aus.
     *
     * @param handler
     */
    public static void listGlobalAdmins(ExtendedTaskDelegate handler){
        new ExtendedTask<Void, Void, List<EndUser>>(handler) {
            @Override
            protected List<EndUser> doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().roleEndpoint().listGlobalAdmins().execute().getItems();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Liste von Administratoren konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }


}
