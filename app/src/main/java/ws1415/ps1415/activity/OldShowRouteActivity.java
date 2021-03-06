package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.skatenight.skatenightAPI.model.EventData;
import com.skatenight.skatenightAPI.model.UserLocationInfo;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.model.EventRole;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.task.QueryVisibleUsersTask;
import ws1415.ps1415.util.LocationUtils;


/**
 * Zeigt die Strecke des aktuellen Events auf einer Karte an.
 */
public class OldShowRouteActivity extends Activity {
    private static final String LOG_TAG = OldShowRouteActivity.class.getSimpleName();

    public static final String EXTRA_ROUTE = "show_route_extra_route";
    public static final String EXTRA_ROUTE_FIELD_FIRST = "show_route_extra_route_field_first";
    public static final String EXTRA_ROUTE_FIELD_LAST = "show_route_extra_route_field_last";
    public static final String EXTRA_WAYPOINTS = "show_route_extra_waypoints";
    public static final String EXTRA_EVENT_ID = "show_route_extra_event_id";
    public static final String EXTRA_SPEED_PROFILE = "show_route_extra_speed_profile";
    public static final String EXTRA_VISITED = "show_route_extra_visited";
    public static final String EXTRA_TIMESTAMPS = "show_route_extra_timestamps";
    public static final String EXTRA_USERGROUPS = "show_route_extra_show_groups";
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
    private boolean showSpeedProfile;

    private List<Marker> groupMarker = new ArrayList<Marker>();

    private Location location; // Enthält die aktuelle Position, die vom Server runtergeladen wurde

    // Handler für das regelmäßige Aktualisierungen
    private int mInterval = 30000;
    private Handler mHandler;

    private boolean updateUsergroups;
    private boolean updateField;

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
                eventId = savedInstanceState.getLong(MEMBER_EVENT_ID);

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
                if (intent.hasExtra(EXTRA_SPEED_PROFILE)) {
                    showSpeedProfile = true;
                }

                if (intent.hasExtra(EXTRA_VISITED) && intent.hasExtra(EXTRA_TIMESTAMPS)) {
                    int[] visited = intent.getIntArrayExtra(EXTRA_VISITED);
                    long[] timestamps = intent.getLongArrayExtra(EXTRA_TIMESTAMPS);

                    createRouteHighlight(visited, timestamps);
                }

                if (intent.hasExtra(EXTRA_WAYPOINTS)) {
                    ArrayList<HashMap> waypoints = (ArrayList<HashMap>) intent.getSerializableExtra(EXTRA_WAYPOINTS);
                    for (HashMap wp : waypoints) {
                        googleMap.addMarker(new MarkerOptions()
                                .title((String)wp.get("title"))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                .position(new LatLng((Double)wp.get("latitude"), (Double)wp.get("longitude")))
                                .draggable(true));
                    }
                }
                if (intent.hasExtra(EXTRA_USERGROUPS)){
                    if (mHandler == null) {
                        mHandler = new Handler();
                    }
                    updateUsergroups = true;
                    refreshVisibleMembers();
                }
            }
            catch (ParseException e) {
                Toast.makeText(getApplicationContext(), "Route parsing failed.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1);

            if (eventId > 0) {
                receiver = new LocationReceiver();

                // Falls ein Event übergeben wurde, dann regelmäßige Aktualisierung des Felds starten
                if (mHandler == null) {
                    mHandler = new Handler();
                }
                refreshField();
                updateField = true;
            }
        }

        highlightRoute(fieldFirst, fieldLast);
    }

    Runnable mStatusChecker = new Runnable(){
        @Override
        public void run(){
            Toast.makeText(OldShowRouteActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
            if (updateUsergroups) {
                refreshVisibleMembers();
            }
            if (updateField) {
                refreshField();
            }
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    void startRepeatingTask(){
        mStatusChecker.run();
    }

    void stopRepeatingTask(){
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(LocationTransmitterService.NOTIFICATION_LOCATION));
        }
        if (mHandler != null) {
            startRepeatingTask();
        }
    }

    @Override
    protected void onStop() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
        if (mHandler != null) {
            stopRepeatingTask();
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
            Intent intent = new Intent(OldShowRouteActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    /**
//     * Übernimmt die Informationen aus dem übergebenen Member-Objekt auf die Karte.
//     * @param m Das neue Event-Objekt.
//     */
//    public void drawMembers(Member m) {
//        if (m != null) {
//            // googleMap.clear();
//
//            LatLng pos = new LatLng(m.getLatitude(), m.getLongitude());
//
//            // Markerfarbe
//            float markerColor;
//            markerColor = BitmapDescriptorFactory.HUE_YELLOW;
//
//            // Fügt den aktuellen Membermarker auf die Karte ein
//            googleMap.addMarker(new MarkerOptions()
//                    .position(pos)
//                    .title("Skater " + m.getName())
//                    .snippet("" + m.getUpdatedAt())
//                    .icon(BitmapDescriptorFactory
//                            .defaultMarker(markerColor)));
//
//        } else {
//            location = null;
//        }
//    }

    private void createRouteHighlight(int[] visited, long[] timestamps) {
        routeTrack.clear();

        if (visited.length > 0 && visited.length == timestamps.length) {
            List<LatLng> waypoints = route.getPoints();

            PolylineOptions opt = new PolylineOptions();
            opt.add(waypoints.get(visited[0]));
            Float prevSpeed = null;

            for (int i = 1; i < visited.length; i++) {
                long elapsedMs = timestamps[i]-timestamps[i-1];

                LatLng curWaypoint = waypoints.get(visited[i]);
                LatLng prevWaypoint = waypoints.get(visited[i-1]);

                float[] output = new float[1];
                Location.distanceBetween(curWaypoint.latitude, curWaypoint.longitude, prevWaypoint.latitude, prevWaypoint.longitude, output);
                float distanceM = output[0];

                float elapsedH = elapsedMs/3.6e6f;
                float distanceKm = distanceM/1000.0f;

                float speed = distanceKm/elapsedH;
                    /*
                    if (i == visited.length-2) {
                        ((TextView) findViewById(R.id.textView)).setText("" + speed);
                        ((TextView) findViewById(R.id.textView2)).setText("" + distanceM);
                        ((TextView) findViewById(R.id.textView3)).setText("" + elapsedMs);
                    }
                    */

                int color = colorForSpeed(speed);
                if ((prevSpeed != null && color != colorForSpeed(prevSpeed)) || i == visited.length-1) {
                    opt.color(colorForSpeed((prevSpeed == null)?0.0f:prevSpeed));
                    opt.add(waypoints.get(visited[i]));
                    routeTrack.add(opt);

                    opt = new PolylineOptions();
                }

                opt.add(waypoints.get(visited[i]));

                prevSpeed = speed;
            }
        }
        drawTrack();
    }

    private void highlightRoute(int first, int last) {
        List<LatLng> highlight = route.getPoints().subList(first, last);
        if (routeHighlightLine != null) routeHighlightLine.remove();
        routeHighlight = new PolylineOptions()
                .addAll(highlight)
                .width(16.0f)
                .color(Color.GREEN);

        routeHighlightLine = googleMap.addPolyline(routeHighlight);
    }

    private void drawTrack() {
        if (routeLine != null) {
            routeLine.remove();
            routeLine = googleMap.addPolyline(route);
        }
        if (routeHighlightLine != null) {
            routeHighlightLine.remove();
            routeHighlightLine = googleMap.addPolyline(routeHighlight);
        }

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
        if (speed <= 5.0f) {
            return Color.RED;
        }
        else if (speed <= 15.0f) {
            return Color.GREEN;
        }
        else {
            return Color.CYAN;
        }
    }

    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(LocationTransmitterService.NOTIFICATION_EXTRA_EVENT_ID, 0);
            if (id > 0 && id == eventId && showSpeedProfile) {
                int[] visited = intent.getIntArrayExtra(LocationTransmitterService.NOTIFICATION_EXTRA_PASSED_WAYPOINTS);
                long[] timestamps = intent.getLongArrayExtra(LocationTransmitterService.NOTIFICATION_EXTRA_PASSED_WAYPOINT_TIME);

                createRouteHighlight(visited, timestamps);
            }
        }
    }

    private void refreshVisibleMembers(){
        new QueryVisibleUsersTask(new ExtendedTaskDelegate<Void, HashMap<UserLocationInfo, String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, HashMap<UserLocationInfo, String> userStringHashMap) {
                for(Marker m : groupMarker){
                    m.remove();
                }
                groupMarker.clear();

                ArrayList<Integer> farben = new ArrayList<Integer>();
                farben.add(R.drawable.small_marker_blue);
                farben.add(R.drawable.small_marker_green);
                farben.add(R.drawable.small_marker_red);
                farben.add(R.drawable.small_marker_yellow);
                farben.add(R.drawable.small_marker_pink);

                HashMap<String, Integer> gruppenFarben = new HashMap<>();

                for(UserLocationInfo user : userStringHashMap.keySet()){
                    if(!gruppenFarben.containsKey(userStringHashMap.get(user))){
                        gruppenFarben.put(userStringHashMap.get(user), farben.get(0));
                        farben.remove(0);
                    }
                    String firstName = (user.getFirstName() == null) ? "" : user.getFirstName();
                    String lastName = (user.getLastName() == null) ? "" : user.getLastName();
                    double latitude = (user.getLatitude() == null) ? 0L : user.getLatitude();
                    double longitude = (user.getLongitude() == null) ? 0L : user.getLongitude();
                    groupMarker.add(googleMap.addMarker(new MarkerOptions()
                            .title(firstName + " " + lastName)
                            .snippet(getString(R.string.group) + userStringHashMap.get(user))
                            .icon(BitmapDescriptorFactory.fromResource(gruppenFarben.get(userStringHashMap.get(user))))
                            .position(new LatLng(latitude, longitude))
                            .draggable(false)));
                }
            }

            @Override
            public void taskDidProgress(ExtendedTask task, Void... progress) {

            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {

            }
        }, this).execute();

        // Ordner und Sanitäter abrufen
        new AsyncTask<Void, Void, Void>() {
            EventRole[] roles = new EventRole[] {
                    EventRole.MARSHALL,
                    EventRole.MEDIC};
            BitmapDescriptor[] icons = new BitmapDescriptor[]{
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_security_black_24dp),
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_plus_circle_outline_black_24dp)};
            List<UserLocationInfo>[] locations = new List[roles.length];

            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 0; i < roles.length; i++) {
                    try {
                        locations[i] = ServiceProvider.getService().eventEndpoint().getEventRolePositions(eventId, roles[i].name()).execute().getItems();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                for (int i = 0; i < roles.length; i++) {
                    if (locations[i] != null) {
                        for (UserLocationInfo location : locations[i]) {
                            groupMarker.add(googleMap.addMarker(new MarkerOptions()
                                    .title(getResources().getStringArray(R.array.event_roles)[roles[i].ordinal()])
                                    .icon(icons[i])
                                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .draggable(false)));
                        }
                    }
                }
            }
        }.execute();
    }

    /**
    * Aktualisiert die Anzeige des aktuellen Felds.
    */
    private void refreshField() {
        EventController.getEvent(new ExtendedTaskDelegateAdapter<Void, EventData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EventData event) {
                fieldFirst = event.getRouteFieldFirst();
                fieldLast = event.getRouteFieldLast();
                highlightRoute(fieldFirst, fieldLast);
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Log.e(LOG_TAG, message);
            }
        }, eventId);
    }
}
