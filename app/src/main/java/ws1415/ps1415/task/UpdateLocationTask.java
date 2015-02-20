package ws1415.ps1415.task;

import android.os.AsyncTask;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;

/**
 * Erwartet die Mail-Adresse des Benutzers und seine Position und schickt diese Daten an den Server.
 *
 * @author Richard Schulze
 */
public class UpdateLocationTask extends AsyncTask<Void, Void, Void> {
    private String email;
    private double latitude;
    private double longitude;

    public UpdateLocationTask(String email, double latitude, double longitude) {
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Schickt eine Mail-Adresse und die dazu gehörige Position an den Server.
     *
     * @param params
     * @return null
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            //Log.e("Err", "Err start");
            ServiceProvider.getService().skatenightServerEndpoint().updateMemberLocation(email, latitude, longitude).execute();
        } catch (IOException e) {
            //Log.e("Err", "Err error");
            e.printStackTrace();
        }
        return null;
    }
}
