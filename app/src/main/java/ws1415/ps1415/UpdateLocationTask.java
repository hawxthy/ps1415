package ws1415.ps1415;

import android.os.AsyncTask;

import com.appspot.skatenight_ms.skatenightAPI.model.Member;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;

/**
 * Created by Richard on 03.11.2014.
 * Erwartet die Mail-Adresse des Benutzers und seine Position und schickt diese Daten an den Server.
 */
public class UpdateLocationTask extends AsyncTask<String, Void, Void> {
    /**
     * Schickt eine Mail-Adresse und die dazu gehörige Position an den Server.
     * @param params Die Mail-Adresse als ersten und die Position als zweiten Parameter.
     * @return null
     */
    @Override
    protected Void doInBackground(String... params) {
        try {
            ServiceProvider.getService().skatenightServerEndpoint().updateMemberLocation(params[0], params[1]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
