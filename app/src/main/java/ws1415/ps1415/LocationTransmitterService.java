package ws1415.ps1415;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.appspot.skatenight_ms.skatenightAPI.SkatenightAPI;
import com.appspot.skatenight_ms.skatenightAPI.model.Member;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import ws1415.ps1415.util.LocationUtils;

/**
 * Created by Tristan Rust on 28.10.2014.
 *
 * Hintergrundservice der zur Ermittlung/Tracking der aktuellen Position dient.
 *
 */
public class LocationTransmitterService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient gac;

    public LocationTransmitterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gac = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        gac.connect();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "onDestroy", Toast.LENGTH_LONG).show();
        if (gac != null) gac.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(1000);

        LocationServices.FusedLocationApi.requestLocationUpdates(gac, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    /**
     * Wird aufgerufen, wenn sich die Position ändert.
     * @param location Die aktuell ermittelt Position
     */
    public void onLocationChanged(Location location) {
        // Holt sich die Google Mail Adresse aus den SharedPreferences, die beim Einloggen angegeben werden mussten
        SharedPreferences prefs = this.getSharedPreferences("skatenight.app", Context.MODE_PRIVATE);
        String email = prefs.getString("accountName", null);

        // Kodiert die Locationdaten als String zur Speicherung auf dem Server
        String locString = LocationUtils.encodeLocation(location);

        // Sendet die Nutzerdaten an den Server
        new UpdateLocationTask().execute(email, locString);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
