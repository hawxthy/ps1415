package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.skatenight.skatenightAPI.model.Member;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ws1415.common.util.LocationUtils;
import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.ps1415.task.QueryMemberTask;

/**
 * Zeigt die Strecke des aktuellen Events auf einer Karte an.
 */
public class ShowRouteActivity extends Activity {
    public static final String EXTRA_ROUTE = "show_route_extra_route";
    public static final String EXTRA_ROUTE_FIELD_FIRST = "show_route_extra_route_field_first";
    public static final String EXTRA_ROUTE_FIELD_LAST = "show_route_extra_route_field_last";
    public static final String EXTRA_EVENT_ID = "show_route_extra_event_id";
    private static final String MEMBER_ROUTE = "show_route_member_route";
    private static final String MEMBER_ROUTE_HIGHLIGHT = "show_route_member_route_highlight";
    private static final String MEMBER_ROUTE_TRACK = "show_route_member_route_track";
    private static final String MEMBER_ROUTE_FIELD_FIRST = "show_route_member_route_field_first";
    private static final String MEMBER_ROUTE_FIELD_LAST = "show_route_member_route_field_last";
    private static final String MEMBER_EVENT_ID = "show_route_member_event_id";

    private GoogleMap googleMap;
    private PolylineOptions route;
    private Polyline routeLine;
    private PolylineOptions routeHighlight;
    private Polyline routeHighlightLine;
    private List<PolylineOptions> routeTrack;
    private List<Polyline> routeTrackLines;
    private int fieldFirst;
    private int fieldLast;
    private long eventId;
    private LocationReceiver receiver;

    private Location location; // Enthält die aktuelle Position, die vom Server runtergeladen wurde

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.show_route_map_fragment)).getMap();
        googleMap.setMyLocationEnabled(true);

        routeTrack = new ArrayList<PolylineOptions>();
        routeTrackLines = new ArrayList<Polyline>();

        Intent intent;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MEMBER_ROUTE)) {
                route = (PolylineOptions) savedInstanceState.getParcelable(MEMBER_ROUTE);
                routeLine = googleMap.addPolyline(route);
            }
            if (savedInstanceState.containsKey(MEMBER_ROUTE_HIGHLIGHT)) {
                routeHighlight = (PolylineOptions) savedInstanceState.getParcelable(MEMBER_ROUTE_HIGHLIGHT);
                routeHighlightLine = googleMap.addPolyline(routeHighlight);
            }
            if (savedInstanceState.containsKey(MEMBER_ROUTE_FIELD_FIRST)) {
                fieldFirst = savedInstanceState.getInt(MEMBER_ROUTE_FIELD_FIRST);
            }
            if (savedInstanceState.containsKey(MEMBER_ROUTE_FIELD_LAST)) {
                fieldLast = savedInstanceState.getInt(MEMBER_ROUTE_FIELD_LAST);
            }
            if (savedInstanceState.containsKey(MEMBER_EVENT_ID)) {
                eventId = savedInstanceState.getInt(MEMBER_EVENT_ID);

                if (eventId > 0) {
                    receiver = new LocationReceiver();
                }
            }
            if (savedInstanceState.containsKey(MEMBER_ROUTE_TRACK)) {
                routeTrack = savedInstanceState.getParcelableArrayList(MEMBER_ROUTE_TRACK);
                drawTrack();
            }
        }
        else if ((intent = getIntent()) != null && intent.hasExtra(EXTRA_ROUTE)) {
            String encodedPath = intent.getStringExtra(EXTRA_ROUTE);
            try {
                List<LatLng> line = LocationUtils.decodePolyline(encodedPath);

                route = new PolylineOptions()
                        .addAll(line)
                        .width(12.0f)
                        .color(Color.BLUE);

                googleMap.clear();
                routeLine = googleMap.addPolyline(route);

                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        if (route != null) {
                            // Grenzwerte der Strecke berechnen und Karte zentrieren
                            LatLngBounds.Builder builder = LatLngBounds.builder();
                            for (LatLng point : route.getPoints()) {
                                builder.include(point);
                            }
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                        }
                    }
                });

                if (intent.hasExtra(EXTRA_ROUTE_FIELD_FIRST)) {
                    fieldFirst = intent.getIntExtra(EXTRA_ROUTE_FIELD_FIRST, 0);
                }
                if (intent.hasExtra(EXTRA_ROUTE_FIELD_LAST)) {
                    fieldLast = intent.getIntExtra(EXTRA_ROUTE_FIELD_LAST, 0);
                }

                Toast.makeText(getApplicationContext(), fieldFirst + " " + fieldLast, Toast.LENGTH_LONG).show();
            }
            catch (ParseException e) {
                Toast.makeText(getApplicationContext(), "Route parsing failed.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1);

            if (eventId > 0) {
                receiver = new LocationReceiver();
            }
        }

        highlightRoute(fieldFirst, fieldLast);

        // Ruft die aktuellen Memberinformationen ab
        new QueryMemberTask().execute((ShowRouteActivity) this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(LocationTransmitterService.NOTIFICATION_LOCATION));
        }
    }

    @Override
    protected void onStop() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (route != null) {
            outState.putParcelable(MEMBER_ROUTE, route);
        }
        if (routeHighlight != null) {
            outState.putParcelable(MEMBER_ROUTE_HIGHLIGHT, routeHighlight);
        }
        outState.putInt(MEMBER_ROUTE_FIELD_FIRST, fieldFirst);
        outState.putInt(MEMBER_ROUTE_FIELD_LAST, fieldLast);
        if (eventId > 0) {
            outState.putLong(MEMBER_EVENT_ID, eventId);
            outState.putParcelableArrayList(MEMBER_ROUTE_TRACK, new ArrayList<PolylineOptions>(routeTrack));
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_route, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Übernimmt die Informationen aus dem übergebenen Member-Objekt auf die Karte.
     * @param m Das neue Event-Objekt.
     */
    public void drawMembers(Member m) {
        if (m != null) {
            // googleMap.clear();

            LatLng pos = new LatLng(m.getLatitude(), m.getLongitude());

            // Markerfarbe
            float markerColor;
            markerColor = BitmapDescriptorFactory.HUE_YELLOW;

            // Fügt den aktuellen Membermarker auf die Karte ein
            googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title("Skater " + m.getName())
                    .snippet("" + m.getUpdatedAt())
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(markerColor)));

            Toast.makeText(getApplicationContext(), "" + m.getCurrentWaypoint(), Toast.LENGTH_SHORT).show();
        } else {
            location = null;
        }
    }

    private void highlightRoute(int first, int last) {
        List<LatLng> highlight = route.getPoints().subList(first, last);
        if (routeHighlightLine != null) routeHighlightLine.remove();
        routeHighlight = new PolylineOptions()
                .addAll(highlight)
                .width(8.0f)
                .color(Color.GREEN);

        routeHighlightLine = googleMap.addPolyline(routeHighlight);
    }

    private void drawTrack() {
        // Linien entfernen
        for (Polyline subline : routeTrackLines) {
            subline.remove();
        }
        routeTrackLines.clear();

        for (PolylineOptions opt : routeTrack) {
            routeTrackLines.add(googleMap.addPolyline(opt));
        }
    }

    private int colorForSpeed(float speed) {
        if (speed <= 0.0f) {
            return Color.DKGRAY;
        }
        else if (speed <= 5.0f) {
            return Color.RED;
        }
        else if (speed <= 10.0f) {
            return Color.GREEN;
        }
        else {
            return Color.CYAN;
        }
    }

    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            routeTrack.clear();
            int[] visited = intent.getIntArrayExtra(LocationTransmitterService.NOTIFICATION_EXTRA_PASSED_WAYPOINTS);
            long[] timestamps = intent.getLongArrayExtra(LocationTransmitterService.NOTIFICATION_EXTRA_PASSED_WAYPOINT_TIME);
            Toast.makeText(getApplicationContext(), visited.length + " - " + timestamps.length, Toast.LENGTH_SHORT).show();

            if (visited.length > 0 && visited.length == timestamps.length) {
                List<LatLng> waypoints = route.getPoints();

                PolylineOptions opt = new PolylineOptions();
                opt.add(waypoints.get(visited[0]));
                Float prevSpeed = null;

                for (int i = 1; i < visited.length; i++) {
                    long elapsedMs = timestamps[i]-timestamps[i-1];

                    LatLng curWaypoint = waypoints.get(i);
                    LatLng prevWaypoint = waypoints.get(i-1);

                    float[] output = new float[1];
                    Location.distanceBetween(curWaypoint.latitude, curWaypoint.longitude, prevWaypoint.latitude, prevWaypoint.longitude, output);
                    float distanceM = output[0];

                    float elapsedH = elapsedMs*3e6f;
                    float distanceKm = distanceM/1000.0f;

                    float speed = distanceKm/elapsedH;

                    opt.add(waypoints.get(visited[i]));

                    int color = colorForSpeed(speed);
                    if ((prevSpeed != null && color != colorForSpeed(prevSpeed)) || i == visited.length-1) {
                        opt.color(color);
                        routeTrack.add(opt);

                        opt = new PolylineOptions();
                        opt.add(waypoints.get(visited[i]));
                    }
                    prevSpeed = speed;
                }
            }
            drawTrack();
        }
    }
}
