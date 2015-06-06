package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.util.LocationUtils;

/**
 * Activity zum Anzeigen einer Route in einer Map.
 *
 * @author Pascal Otto
 */
public class ShowRouteActivity extends Activity {
    public static final String EXTRA_TITLE = "show_route_extra_title";
    public static final String EXTRA_ROUTE = "show_route_extra_route";
    public static final String EXTRA_WAYPOINTS = "show_route_extra_waypoints";

    private static final String MEMBER_ROUTE = "show_route_member_route";

    private GoogleMap googleMap;
    private PolylineOptions route;

    /**
     * Zeigt die Route, die über einen Intent verschickt wurde, an.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        setTitle(getIntent().getStringExtra("routeName"));

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.show_route_map_fragment)).getMap();
        googleMap.setMyLocationEnabled(true);

        Intent intent;
        if (savedInstanceState != null && savedInstanceState.containsKey(MEMBER_ROUTE)) {
            route = (PolylineOptions) savedInstanceState.getParcelable(MEMBER_ROUTE);
            googleMap.addPolyline(route);
        }
        else if ((intent = getIntent()) != null) {
            if (intent.hasExtra(EXTRA_TITLE)) {
                setTitle(intent.getStringExtra(EXTRA_TITLE));
            }
            if (intent.hasExtra(EXTRA_ROUTE)) {
                String encodedPath = intent.getStringExtra(EXTRA_ROUTE);
                try {
                    List<LatLng> line = LocationUtils.decodePolyline(encodedPath);

                    route = new PolylineOptions()
                            .addAll(line)
                            .color(Color.BLUE);

                    googleMap.clear();
                    googleMap.addPolyline(route);

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
                }
                catch (ParseException e) {
                    Toast.makeText(getApplicationContext(), "Route parsing failed.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                // Wegpunkte laden, falls vorhanden
                if (intent.hasExtra(EXTRA_WAYPOINTS)) {
                    List<HashMap> waypoints = (List<HashMap>) intent.getSerializableExtra(EXTRA_WAYPOINTS);
                    for (HashMap wp : waypoints) {
                        RouteEditorActivity.Waypoint tmp = RouteEditorActivity.Waypoint.create(
                                new LatLng((Double)wp.get("latitude"), (Double)wp.get("longitude")),
                                (String)wp.get("title")

                        );
                        googleMap.addMarker(tmp.getMarkerOptions());
                    }
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (route != null) {
            outState.putParcelable(MEMBER_ROUTE, route);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_route, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
