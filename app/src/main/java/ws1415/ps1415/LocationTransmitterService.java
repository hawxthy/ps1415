package ws1415.ps1415;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.util.List;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.util.LocationUtils;
import ws1415.ps1415.task.RouteLoaderTask;
import ws1415.ps1415.task.UpdateLocationTask;

/**
 * Created by Tristan Rust on 28.10.2014.
 *
 * Hintergrundservice der zur Ermittlung/Tracking der aktuellen Position dient und diese auf den
 * Server sendet. Falls der Nutzer noch nicht existiert wird er angelegt, ansonsten geupdatet.
 *
 */
public class LocationTransmitterService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ExtendedTaskDelegate<Void, String> {
    private static final String LOG_TAG = LocationTransmitterService.class.getSimpleName();

    public static final float MAX_NEXT_WAYPOINT_DISTANCE = 10.0f;
    public static final float MAX_ANY_WAYPOINT_DISTANCE = 60.0f;
    public static final int MIN_WAYPOINT_MEMBER_COUNT = 1;

    public static final String NOTIFICATION_LOCATION = "location_transmitter_service_notification_location";
    public static final String NOTIFICATION_EXTRA_LOCATION = "location_transmitter_service_notification_location";
    public static final String NOTIFICATION_EXTRA_CURRENT_WAYPOINT = "location_transmitter_service_notification_current_waypoint";
    public static final String NOTIFICATION_EXTRA_WAYPOINT_COUNT = "location_transmitter_service_notification_waypoint_count";
    private LocalBroadcastManager broadcastManager;

    private GoogleApiClient gac;
    private List<LatLng> waypoints;
    private int currentWaypoint; // TODO: currentWaypoint speichern!

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

        new RouteLoaderTask(this, 5759409141579776L).execute();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        broadcastManager = LocalBroadcastManager.getInstance(this);
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

        if (waypoints != null && waypoints.size() > 0) {
            calculateCurrentWaypoint(new LatLng(location.getLatitude(), location.getLongitude()));
            Log.d(LOG_TAG, "current: " + currentWaypoint);
        }

        sendLocationUpdate(location);
    }

    private void sendLocationUpdate(Location location) {
        if (waypoints != null) {
            Intent intent = new Intent(NOTIFICATION_LOCATION);
            if(location != null)
                intent.putExtra(NOTIFICATION_EXTRA_LOCATION, location);
            intent.putExtra(NOTIFICATION_EXTRA_CURRENT_WAYPOINT, currentWaypoint);
            intent.putExtra(NOTIFICATION_EXTRA_WAYPOINT_COUNT, waypoints.size());
            broadcastManager.sendBroadcast(intent);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void calculateCurrentWaypoint(LatLng location) {
        //Integer currentWaypoint = member.getCurrentWaypoint();
        /*
        if (currentWaypoint == null) {
            member.setCurrentWaypoint(0);
        }
        */
        if (currentWaypoint < waypoints.size()-1) {
            LatLng current = waypoints.get(currentWaypoint);
            LatLng next = waypoints.get(currentWaypoint+1);
            float distanceCurrent = distance(current.latitude, current.longitude, location.latitude, location.longitude);
            float distanceNext = distance(next.latitude, next.longitude, location.latitude, location.longitude);
            Log.d(LOG_TAG, "dcurrent: " + distanceCurrent);
            Log.d(LOG_TAG, "dnext: " + distanceNext);
            boolean findNextWaypoint = false;
            if (distanceCurrent < MAX_NEXT_WAYPOINT_DISTANCE) {
                findNextWaypoint = true;
            }
            if (distanceNext < distanceCurrent) {
                if (distanceNext < MAX_NEXT_WAYPOINT_DISTANCE) {
                    //member.setCurrentWaypoint(currentWaypoint+1);
                    currentWaypoint++;
                    findNextWaypoint = false;
                }
                else {
                    findNextWaypoint = true;
                }
            }
            if (findNextWaypoint) {
                Log.d(LOG_TAG, "findNextWaypoint");
                // Den nächsten Wegpunkt finden:
                float minDistance = Float.POSITIVE_INFINITY;
                for (int i = currentWaypoint; i < waypoints.size(); i++) {
                    float distance = distance(
                            location.latitude, location.longitude,
                            waypoints.get(i).latitude, waypoints.get(i).longitude);
                    Log.d(LOG_TAG, i + ": " + distance);
                    if (distance < MAX_ANY_WAYPOINT_DISTANCE && distance < minDistance) {
                        //member.setCurrentWaypoint(i);
                        currentWaypoint = i;
                        minDistance = distance;
                    }
                }
            }
        }
    }

    private float distance(double lat1, double lon1, double lat2, double lon2) {
        float[] distance = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, distance);
        return distance[0];
    }

    @Override
    public void taskDidFinish(ExtendedTask task, String s) {
        Log.d(LOG_TAG, s);
        try {
            waypoints = LocationUtils.decodePolyline(s);
        }
        catch (ParseException e) {
            Log.e(LOG_TAG, "Unable to parse waypoints.", e);
        }
        onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(gac));
    }

    @Override
    public void taskDidProgress(ExtendedTask task, Void... progress) {

    }

    @Override
    public void taskFailed(ExtendedTask task, String message) {
        Log.e(LOG_TAG, message);
    }
}
