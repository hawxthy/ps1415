package ws1415;

import android.test.AndroidTestCase;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import ws1415.common.net.ServiceProvider;
import ws1415.ps1415.Constants;

/**
 * Erweitert den normalen AndroidTestCase um Authenifizierung. Es wird dabei automatisch der erste
 * Benutzer, der auf dem angeschlossenen Handy gefunden wird, eingeloggt, falls noch kein anderer
 * Benutzer eingeloggt ist.
 * @author Richard Schulze
 */
public class AuthenticatedAndroidTestCase extends AndroidTestCase {
    private int selectedAccount = -1;

    /**
     * Loggt beim Initialisieren des Test automatisch den ersten Benutzer ein, der auf dem Gerät eingerichtet ist.
     * @throws Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        changeAccount(0);
    }

    /**
     * Wechselt auf den Account des Testgeräts mit dem angegebenen Index.
     * @param index    Der Index des Accounts, der ausgewählt wird.
     */
    public void changeAccount(int index) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getContext(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        if (credential.getSelectedAccountName() == null || selectedAccount != index) {
            credential.setSelectedAccountName(credential.getAllAccounts()[index].name);
            selectedAccount = index;
        }
        ServiceProvider.login(credential);
    }

    /**
     * Gibt den Index des zurzeit ausgewählten Accounts zurück.
     * @return Der Index des zurzeit ausgewählten Accounts.
     */
    public int getSelectedAccount() {
        return selectedAccount;
    }

    /**
     * Gibt die Mail-Adresse des zurzeit ausgewählten Accounts zurück.
     * @return Die Mail-Adresse des zurzeit ausgewählten Accounts.
     */
    public String getSelectedAccountMail() {
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getContext(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        return credential.getSelectedAccountName();
    }

    /**
     * Gibt die Mail-Adresse des Accounts mit dem übergebenen Index zurück.
     * @param index    Der Index des Accounts, dessen Mail-Adresse abgerufen wird.
     * @return Die Mail-Adresse des Accounts mit dem angegebenen Index.
     */
    public String getAccountMail(int index) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getContext(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        return credential.getAllAccounts()[index].name;
    }

    /**
     * Gibt die Anzahl der registrierten Accounts des Geräts zurück.
     * @return Die Anzahl der Accounts auf dem Testgerät.
     */
    public int getAccountCount() {
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getContext(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        return credential.getAllAccounts().length;
    }
}
