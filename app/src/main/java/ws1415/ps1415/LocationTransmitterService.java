package ws1415.ps1415;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import ws1415.ps1415.task.UpdateLocationTask;

/**
 * Created by Tristan Rust on 28.10.2014.
 *
 * Hintergrundservice der zur Ermittlung/Tracking der aktuellen Position dient und diese auf den
 * Server sendet. Falls der Nutzer noch nicht existiert wird er angelegt, ansonsten geupdatet.
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
        // Starten des GoogleApiClients
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
    }

    @Override
    public void onDestroy() {
        if (gac != null) gac.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Sendet die Location Requests sekündlich
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

        // Sendet die Nutzerdaten an den Server
        // TODO: Location sollte nur geschickt werden wenn das vom Nutzer gewünscht wird (dann email prompt anzeigen)
        if (email != null) {
            new UpdateLocationTask(email, location.getLatitude(), location.getLongitude()).execute();
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
