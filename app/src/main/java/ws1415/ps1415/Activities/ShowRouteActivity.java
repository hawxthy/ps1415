package ws1415.ps1415.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.appspot.skatenight_ms.skatenightAPI.model.Member;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.ParseException;
import java.util.List;

import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.ps1415.task.QueryMemberTask;
import ws1415.ps1415.util.LocationUtils;

/**
 * Zeigt die Strecke des aktuellen Events auf einer Karte an.
 */
public class ShowRouteActivity extends Activity {
    public static final String EXTRA_ROUTE = "show_route_extra_route";
    private static final String MEMBER_ROUTE = "show_route_member_route";
    private static final String MEMBER_ROUTE_HIGHLIGHT = "show_route_member_route_highlight";

    private GoogleMap googleMap;
    private PolylineOptions route;
    private PolylineOptions routeHighlight;
    private Polyline routeHighlightLine;
    private Intent service;

    private Location location; // Enthält die aktuelle Position, die vom Server runtergeladen wurde

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.show_route_map_fragment)).getMap();
        googleMap.setMyLocationEnabled(true);

        Intent intent;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MEMBER_ROUTE)) {
                route = (PolylineOptions) savedInstanceState.getParcelable(MEMBER_ROUTE);
                googleMap.addPolyline(route);
            }
            if (savedInstanceState.containsKey(MEMBER_ROUTE_HIGHLIGHT)) {
                routeHighlight = (PolylineOptions) savedInstanceState.getParcelable(MEMBER_ROUTE_HIGHLIGHT);
                googleMap.addPolyline(routeHighlight);
            }
        }
        else if ((intent = getIntent()) != null && intent.hasExtra(EXTRA_ROUTE)) {
            String encodedPath = intent.getStringExtra(EXTRA_ROUTE);
            try {
                List<LatLng> line = LocationUtils.decodePolyline(encodedPath);

                route = new PolylineOptions()
                        .addAll(line)
                        .width(10.0f)
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

                highlightRoute(5, 10);
            }
            catch (ParseException e) {
                Toast.makeText(getApplicationContext(), "Route parsing failed.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        // Ruft die aktuellen Memberinformationen ab
        new QueryMemberTask().execute((ShowRouteActivity) this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Starten des Hintergrundservices zur Standortermittlung
        service = new Intent(getBaseContext(), LocationTransmitterService.class);
        service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(service);

    }

    @Override
    protected void onStop() {
        super.onStop();

        stopService(service);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (route != null) {
            outState.putParcelable(MEMBER_ROUTE, route);
        }
        if (routeHighlight != null) {
            outState.putParcelable(MEMBER_ROUTE_HIGHLIGHT, routeHighlight);
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
        } else {
            location = null;
        }
    }

    private void highlightRoute(int first, int last) {
        List<LatLng> highlight = route.getPoints().subList(first, last);
        if (routeHighlightLine != null) routeHighlightLine.remove();
        routeHighlight = new PolylineOptions()
                .addAll(highlight)
                .width(7.5f)
                .color(Color.RED);

        routeHighlightLine = googleMap.addPolyline(routeHighlight);
    }
}
