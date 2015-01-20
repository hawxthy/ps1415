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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Tristan Rust on 28.10.2014.
 *
 * Hintergrundservice der zur Ermittlung/Tracking der aktuellen Position dient und diese auf den
 * Server sendet. Falls der Nutzer noch nicht existiert wird er angelegt, ansonsten geupdatet.
 *
 */
public class LocationTransmitterService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String LOG_TAG = LocationTransmitterService.class.getSimpleName();

    public static final float MAX_NEXT_WAYPOINT_DISTANCE = 10.0f;
    public static final float MAX_ANY_WAYPOINT_DISTANCE = 60.0f;

    public static final String EXTRA_EVENT_ID = "location_transmitter_service_extra_event_id";
    public static final String EXTRA_WAYPOINTS = "location_transmitter_service_extra_waypoints";
    public static final String EXTRA_START_DATE = "location_transmitter_service_extra_start_date";

    public static final String NOTIFICATION_LOCATION = "location_transmitter_service_notification_location";
    public static final String NOTIFICATION_EXTRA_LOCATION = "location_transmitter_service_notification_location";
    public static final String NOTIFICATION_EXTRA_EVENT_ID = "location_transmitter_service_notification_event_id";
    public static final String NOTIFICATION_EXTRA_CURRENT_WAYPOINT = "location_transmitter_service_notification_current_waypoint";
    public static final String NOTIFICATION_EXTRA_WAYPOINT_COUNT = "location_transmitter_service_notification_waypoint_count";
    public static final String NOTIFICATION_EXTRA_CURRENT_DISTANCE = "location_transmitter_service_notification_current_distance";
    public static final String NOTIFICATION_EXTRA_CUR_SPEED = "location_transmitter_service_notification_cur_speed";
    public static final String NOTIFICATION_EXTRA_MAX_SPEED = "location_transmitter_service_notification_max_speed";
    public static final String NOTIFICATION_EXTRA_AVG_SPEED = "location_transmitter_service_notification_avg_speed";
    public static final String NOTIFICATION_EXTRA_ELEVATION_GAIN = "location_transmitter_service_notification_elevation_gain";
    public static final String NOTIFICATION_EXTRA_PASSED_WAYPOINTS = "location_transmitter_service_notification_passed_waypoints";
    public static final String NOTIFICATION_EXTRA_PASSED_WAYPOINT_TIME = "location_transmitter_service_notification_passed_waypoint_time";
    private LocalBroadcastManager broadcastManager;

    private GoogleApiClient gac;
    private long eventId;
    private List<LatLng> waypoints;
    private List<Integer> passedWaypoints;
    private List<Long> passedWaypointTimes;
    private Date startDate;
    private int currentWaypoint; // TODO: currentWaypoint speichern!
    private float curSpeed;
    private float maxSpeed;
    private float avgSpeed;
    private float currentDistance;
    private boolean foundFirstWaypoint;
    private float previousAlt;
    private float elevationGain;

    public LocationTransmitterService() {
        foundFirstWaypoint = false;
        currentDistance = 0.0f;
        curSpeed = 0.0f;
        maxSpeed = 0.0f;
        avgSpeed = 0.0f;
        previousAlt = Float.NaN;
        elevationGain = 0.0f;
        passedWaypoints = new ArrayList<>();
        passedWaypointTimes = new ArrayList<>();
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

        eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1);
        waypoints = intent.getParcelableArrayListExtra(EXTRA_WAYPOINTS);
        startDate = new Date(intent.getLongExtra(EXTRA_START_DATE, 0));
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
        locationRequest.setInterval(1000);

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
            //new UpdateLocationTask(email, location.getLatitude(), location.getLongitude()).execute();
        }

        if (waypoints != null && waypoints.size() > 0) {
            // Nur aktuelle Distanz berechnen wenn, bereits ein current Waypoint existiert
            int oldWaypoint = -1;
            if (foundFirstWaypoint) {
                oldWaypoint = currentWaypoint;
            }
            calculateCurrentWaypoint(new LatLng(location.getLatitude(), location.getLongitude()));
            foundFirstWaypoint = true;

            // CurrentWaypoint in die passedWaypoint Liste eintragen
            if (passedWaypoints.isEmpty() || (passedWaypoints.get(passedWaypoints.size()-1) != currentWaypoint)) {
                passedWaypoints.add(currentWaypoint);

                // vergangene Zeit in der passedWaypointTime liste speichern
                passedWaypointTimes.add(new Date().getTime());
            }


            Log.d(LOG_TAG, "current: " + currentWaypoint);

            // Aktuelle Distance aktualisieren
            if (oldWaypoint != -1) {
                currentDistance += distance(oldWaypoint, currentWaypoint);
            }
        }
        if (location.hasSpeed()) {
            curSpeed = mpsTokph(location.getSpeed());
            maxSpeed = Math.max(curSpeed, maxSpeed);
        }

        if (location.hasAltitude()) {
            if (!Float.isNaN(previousAlt)) {
                if (location.getAltitude() > previousAlt) {
                    elevationGain += location.getAltitude()-previousAlt;
                }
            }

            previousAlt = (float)location.getAltitude();
        }

        float elapsedTimeH = (new Date().getTime()-startDate.getTime())/3.6e6f;
        // currentDistance/1000.0f -> km
        // elapsedTimeH -> verstrichene Zeit in h
        avgSpeed = (currentDistance/1000.0f)/elapsedTimeH;

        sendLocationUpdate(location);
    }

    private void sendLocationUpdate(Location location) {
        if (waypoints != null) {
            Intent intent = new Intent(NOTIFICATION_LOCATION);
            if(location != null)
                intent.putExtra(NOTIFICATION_EXTRA_LOCATION, location);
            intent.putExtra(NOTIFICATION_EXTRA_EVENT_ID, eventId);
            intent.putExtra(NOTIFICATION_EXTRA_CURRENT_WAYPOINT, currentWaypoint);
            intent.putExtra(NOTIFICATION_EXTRA_WAYPOINT_COUNT, waypoints.size());
            intent.putExtra(NOTIFICATION_EXTRA_CURRENT_DISTANCE,currentDistance);
            intent.putExtra(NOTIFICATION_EXTRA_CUR_SPEED, curSpeed);
            intent.putExtra(NOTIFICATION_EXTRA_MAX_SPEED, maxSpeed);
            intent.putExtra(NOTIFICATION_EXTRA_AVG_SPEED, avgSpeed);
            intent.putExtra(NOTIFICATION_EXTRA_ELEVATION_GAIN, elevationGain);
            intent.putExtra(NOTIFICATION_EXTRA_PASSED_WAYPOINTS, toPrimitiveInt(passedWaypoints));
            intent.putExtra(NOTIFICATION_EXTRA_PASSED_WAYPOINT_TIME, toPrimitiveLong(passedWaypointTimes));
            broadcastManager.sendBroadcast(intent);
        }
    }

    private int[] toPrimitiveInt(List<Integer> list) {
        int[] out = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            out[i] = list.get(i);
        }
        return out;
    }

    private long[] toPrimitiveLong(List<Long> list) {
        long[] out = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            out[i] = list.get(i);
        }
        return out;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void calculateCurrentWaypoint(LatLng location) {
        float minDistance = Float.POSITIVE_INFINITY;
        for (int i = currentWaypoint; i < waypoints.size(); i++) {
            float distance = distance(
                    location.latitude, location.longitude,
                    waypoints.get(i).latitude, waypoints.get(i).longitude);
            //Log.d(LOG_TAG, i + ": " + distance);
            if (distance < MAX_ANY_WAYPOINT_DISTANCE && distance < minDistance) {
                //member.setCurrentWaypoint(i);
                currentWaypoint = i;
                minDistance = distance;
            }
        }
        /*
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
        */
    }

    private float distance(double lat1, double lon1, double lat2, double lon2) {
        float[] distance = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, distance);
        return distance[0];
    }

    private float mpsTokph(float val) {
        return val*3.6f;
    }

    /**
     * Berechnet die Distanz zwischen 2 Wegpunkten
     * @param waypoint1 index des ersten Wegpunktes
     * @param waypoint2 index des zweiten Wegpunktes
     * @return die Distanz
     */
    private float distance(int waypoint1, int waypoint2) {
        LatLng w1 = waypoints.get(waypoint1);
        LatLng w2 = waypoints.get(waypoint2);
        float distanceW1W2 = distance(w1.latitude, w1.longitude, w2.latitude, w2.longitude);

        return distanceW1W2;
    }
}
