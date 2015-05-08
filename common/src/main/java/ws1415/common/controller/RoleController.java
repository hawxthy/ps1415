package ws1415.common.controller;

import com.skatenight.skatenightAPI.model.Domain;
import com.skatenight.skatenightAPI.model.JsonMap;
import com.skatenight.skatenightAPI.model.RoleWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ws1415.common.model.Role;
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
     * Gibt die Rolle eines Benutzers vom Server zurück.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     * @param domain   Umgebung
     */
    public static void getUserRole(ExtendedTaskDelegate handler, final String userMail,
                                   final Domain domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain nicht gültig");
        }
        new ExtendedTask<Void, Void, Role>(handler) {
            @Override
            protected Role doInBackground(Void... params) {
                try {
                    RoleWrapper roleWrapper = ServiceProvider.getService().roleEndpoint().getUserRole(userMail).execute();
                    return Role.getValue(roleWrapper.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Rolle für den Benutzer konnte nicht gesetzt werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Setzt die Rolle für einen Benutzer, falls eine Rolle für die Umgebung vorhanden ist, so wird
     * diese überschrieben.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     * @param domain   Umgebung
     * @param role     Rolle des Benutzers
     */
    public static void assignUserRole(ExtendedTaskDelegate handler, final String userMail,
                                      final Domain domain, final Role role) {
        if (domain == null || domain.getKey() == null) {
            throw new IllegalArgumentException("Domain nicht gültig");
        }
        new ExtendedTask<Void, Void, Domain>(handler) {
            @Override
            protected Domain doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().roleEndpoint().assignUserRole(userMail, role.getId(), domain).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Rolle für den Benutzer konnte nicht gesetzt werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Löscht die Rolle des Benutzers.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     * @param domain   Umgebung
     */
    public static void deleteUserRole(ExtendedTaskDelegate handler, final String userMail,
                                      final Domain domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain nicht gültig");
        }
        new ExtendedTask<Void, Void, Domain>(handler) {
            @Override
            protected Domain doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().roleEndpoint().deleteUserRole(userMail, domain).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Rolle für den Benutzer konnte nicht gesetzt werden");
                    return null;
                }
            }
        }.execute();
    }


    /**
     * Gibt die globale Rolle eines Benutzers aus.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void getGlobalRole(ExtendedTaskDelegate handler, final String userMail) {
        new ExtendedTask<Void, Void, Role>(handler) {
            @Override
            protected Role doInBackground(Void... params) {
                try {
                    Integer roleId = ServiceProvider.getService().roleEndpoint().getGlobalRole(userMail).execute().getValue();
                    return Role.getValue(roleId);
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError("Globale Rolle des Benutzers konnte nicht abgerufen werden");
                    return null;
                }
            }
        }.execute();
    }

    /**
     * Weist einem Benutzer eine globale Rolle zu.
     *
     * @param handler
     * @param userMail E-Mail Adresse des Benutzers
     * @param role Neue Rolle
     */
    public static void assignGlobalRole(ExtendedTaskDelegate handler, final String userMail, final Role role) {
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
     * Gibt die Rolle für den Benutzer für das lokale Objekt der Umgebung zurück.
     * TODO: Testen
     *
     * @param domain Umgebung
     * @param userMail E-Mail Adresse des Benutzers
     */
    public static void getUserRole(Domain domain, String userMail) throws JSONException {
        JsonMap userRoles = domain.getUserRoles();
        JSONObject userRolesObject = new JSONObject(userRoles);
        try {
            userRolesObject.getInt(userMail);
        } catch (JSONException e) {
            throw new JSONException("Rolle vom Benutzer konnte nicht abgerufen werden: " + userMail);
        }
    }

}
