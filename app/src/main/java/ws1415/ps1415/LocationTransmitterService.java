package ws1415.ps1415;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.appspot.skatenight_ms.skatenightAPI.model.Member;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ws1415.ps1415.util.LocationUtils;

/**
 * Created by Tristan Rust on 28.10.2014.
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
    public void onLocationChanged(Location location) {

        String locString = LocationUtils.encodeLocation(location);

        // Erstelle einen neuen Member
        Member bob = new Member();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        // Setze die Attribute vom Member
        bob.setName("Bob");
        bob.setUpdatedAt(date.toString());
        bob.setLocation(locString);
        bob.setEmail("bob@test.com");

        new CreateMemberTask().execute(bob);

        // Toast.makeText(getApplicationContext(), location.toString() + " - " + locString + " - " + locDecodec, Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), bob.getUpdatedAt(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
