package ws1415.veranstalterapp.activity;

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
import java.util.List;

import ws1415.veranstalterapp.R;
import ws1415.common.util.LocationUtils;

/**
 * Activity zum Anzeigen einer Route in einer Map.
 *
 * Edited by Bernd Eissing, Marting Wrodarczyk on 21.10.2014.
 */
public class ShowRouteActivity extends Activity {
    public static final String EXTRA_TITLE = "show_route_extra_title";
    public static final String EXTRA_ROUTE = "show_route_extra_route";
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
