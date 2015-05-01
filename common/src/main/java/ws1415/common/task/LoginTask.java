package ws1415.common.task;

import android.content.Intent;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.BooleanWrapper;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Baut eine Verbindung zum Server mit dem in den GoogleAccountCredentials gewählten Google-Account
 * auf und prüft, ob der Benutzer ein authorisierter Veranstalter ist.
 * Falls der Veranstalter authorisiert ist, wird die HoldTabsActivity gestartet. Andernfalls wird
 * eine Fehlermeldung in Form eines Toasts ausgegeben.
 *
 * Created by Richard, Daniel on 28.10.2014.
 */
public class LoginTask extends ExtendedTask<GoogleAccountCredential, Void, Boolean> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public LoginTask(ExtendedTaskDelegate<Void, Boolean> delegate) {
        super(delegate);
    }

    /**
     * Stellt die Verbindung zum Server her und prüft, ob der Benutzer ein Veranstalter ist.
     * @param params Die LoginActivity, die die Credentials zur Verfügung stellt.
     * @return true, wenn der Benutzer ein Veranstalter ist, sonst false.
     */
    @Override
    protected Boolean doInBackground(GoogleAccountCredential... params) {
        ServiceProvider.login(params[0]);
        try {
            BooleanWrapper isHost = ServiceProvider.getService().hostEndpoint().isHost(
                            params[0].getSelectedAccountName()).execute();
            return isHost.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
