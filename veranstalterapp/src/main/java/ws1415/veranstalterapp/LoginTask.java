package ws1415.veranstalterapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.appspot.skatenight_ms.skatenightAPI.model.BooleanWrapper;

import java.io.IOException;

/**
 * Created by Richard on 28.10.2014.
 */
public class LoginTask extends AsyncTask<LoginActivity, Void, Boolean> {
    private LoginActivity activity;

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
