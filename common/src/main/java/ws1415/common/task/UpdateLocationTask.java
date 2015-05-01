package ws1415.common.task;

import android.os.AsyncTask;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Erwartet die Mail-Adresse des Benutzers und seine Position und schickt diese Daten an den Server.
 *
 * @author Richard Schulze
 */
public class UpdateLocationTask extends ExtendedTask<Void, Void, Void> {
    private String email;
    private double latitude;
    private double longitude;
    private long currentEventId;

    public UpdateLocationTask(ExtendedTaskDelegate<Void, Void> delegate, String email, double latitude, double longitude, long currentEventId) {
        super(delegate);

        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentEventId = currentEventId;
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
            ServiceProvider.getService().routeEndpoint().updateMemberLocation(email, latitude, longitude, currentEventId).execute();
        } catch (IOException e) {
            //Log.e("Err", "Err error");
            e.printStackTrace();
        }
        return null;
    }
}
