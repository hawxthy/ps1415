package ws1415.veranstalterapp.task;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.appspot.skatenight_ms.skatenightAPI.model.BooleanWrapper;

import java.io.IOException;

import ws1415.veranstalterapp.Activities.HoldTabsActivity;
import ws1415.veranstalterapp.Activities.LoginActivity;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.ServiceProvider;

/**
 * Baut eine Verbindung zum Server mit dem in den GoogleAccountCredentials gewählten Google-Account
 * auf und prüft, ob der Benutzer ein authorisierter Veranstalter ist.
 * Falls der Veranstalter authorisiert ist, wird die HoldTabsActivity gestartet. Andernfalls wird
 * eine Fehlermeldung in Form eines Toasts ausgegeben.
 *
 * Created by Richard, Daniel on 28.10.2014.
 */
public class LoginTask extends AsyncTask<LoginActivity, Void, Boolean> {
    private LoginActivity activity;

    /**
     * Stellt die Verbindung zum Server her und prüft, ob der Benutzer ein Veranstalter ist.
     * @param params Die LoginActivity, die die Credentials zur Verfügung stellt.
     * @return true, wenn der Benutzer ein Veranstalter ist, sonst false.
     */
    @Override
    protected Boolean doInBackground(LoginActivity... params) {
        activity = params[0];
        ServiceProvider.login(activity.getCredential());
        try {
            BooleanWrapper isHost = ServiceProvider.getService().skatenightServerEndpoint().isHost(
                            activity.getCredential().getSelectedAccountName()).execute();
            return isHost.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Startet die HoldTabsActivity, wenn der Benutzer ein Veranstalter ist. Andernfalls wird ein
     * Toast angezeigt, der den Benutzer über die fehlende Berechtigung informiert.
     * @param result true, wenn der Benutzer ein Veranstalter ist, sonst false.
     */
    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            // Activity starten
            Intent i = new Intent(activity.getApplicationContext(),
                    HoldTabsActivity.class);
            activity.startActivity(i);
        } else {
            // Fehler anzeigen
            Toast.makeText(activity.getApplicationContext(),
                    R.string.not_authorized,
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
