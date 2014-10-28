package ws1415.ps1415;

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

import ws1415.ps1415.util.LocationUtils;


public class ShowRouteActivity extends Activity {
    public static final String EXTRA_PATH = "show_route_extra_path";
    private static final String MEMBER_PATH = "show_route_member_path";

    GoogleMap googleMap;
    PolylineOptions path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.show_route_map_fragment)).getMap();
        googleMap.setMyLocationEnabled(true);

        Intent intent;
        if (savedInstanceState != null && savedInstanceState.containsKey(MEMBER_PATH)) {

            path = (PolylineOptions) savedInstanceState.getParcelable(MEMBER_PATH);
            googleMap.addPolyline(path);
        }
        else if ((intent = getIntent()) != null && intent.hasExtra(EXTRA_PATH)) {
            String encodedPath = intent.getStringExtra(EXTRA_PATH);
            try {
                List<LatLng> line = LocationUtils.decodePolyline(encodedPath);

                path = new PolylineOptions()
                        .addAll(line)
                        .color(Color.BLUE);

                googleMap.clear();
                googleMap.addPolyline(path);

                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        if (path != null) {
                            // Grenzwerte der Strecke berechnen und Karte zentrieren
                            LatLngBounds.Builder builder = LatLngBounds.builder();
                            for (LatLng point : path.getPoints()) {
                                builder.include(point);
                            }
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20));
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (path != null) {
            outState.putParcelable(MEMBER_PATH, path);
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
