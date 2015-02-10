package ws1415.veranstalterapp.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.skatenight.skatenightAPI.model.RoutePoint;
import com.skatenight.skatenightAPI.model.ServerWaypoint;
import com.skatenight.skatenightAPI.model.Text;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import ws1415.veranstalterapp.fragment.EditorMapFragment;
import ws1415.veranstalterapp.fragment.EditorWaypointsFragment;
import ws1415.veranstalterapp.R;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.util.LocationUtils;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.fragment.EditorMapFragment;
import ws1415.veranstalterapp.fragment.EditorWaypointsFragment;
import ws1415.veranstalterapp.task.AddRouteTask;
import ws1415.veranstalterapp.task.RouteLoaderTask;


public class RouteEditorActivity extends Activity implements ActionBar.TabListener, ExtendedTaskDelegate<Void, RouteEditorActivity.Route> {
    private static final String LOG_TAG = RouteEditorActivity.class.getSimpleName();

    public static final String EXTRA_NAME = "route_editor_activity_extra_name";
    public static final String EXTRA_WAYPOINTS = "route_editor_activity_extra_waypoints";
    private static final String MEMBER_NAME = "route_editor_activity_member_name";
    private static final String MEMBER_WAYPOINTS = "route_editor_activity_member_waypoints";
    private static final String MEMBER_ROUTE = "route_editor_activity_member_route";

    private static final int MAX_WAYPOINTS = 10;

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;

    private List<HashMap> waypoints;
    private String name;
    private ArrayAdapter<Waypoint> waypointArrayAdapter;
    private Route route;
    private RouteLoaderTask currentTask;

    private static RouteEditorActivity routeEditorActivity;

    public RouteEditorActivity(){
        routeEditorActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_route_editor);
        setProgressBarIndeterminateVisibility(false);

        name = null;

        waypointArrayAdapter = new WaypointAdapter(
                this,
                R.layout.list_view_item_waypoint,
                R.id.list_view_item_waypoint_name_textview);

        route = null;

        if(getIntent().hasExtra(EXTRA_WAYPOINTS)) {
            EditorMapFragment mapFragment = (EditorMapFragment) getFragmentByPosition(0);
            waypoints = (ArrayList<HashMap>) getIntent().getSerializableExtra(EXTRA_WAYPOINTS);
            for(int i=0; i<waypoints.size(); i++){
                Waypoint tmp = Waypoint.create(new LatLng((Double) waypoints.get(i).get("latitude"),
                                (Double) waypoints.get(i).get("longitude")),
                        (String) waypoints.get(i).get("title"));
                waypointArrayAdapter.add(tmp);
                mapFragment.updateWaypoint(tmp);
            }
        }

        Intent intent;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MEMBER_NAME)) {
                name = savedInstanceState.getString(MEMBER_NAME);
            }
            if (savedInstanceState.containsKey(MEMBER_WAYPOINTS)) {
                waypointArrayAdapter.addAll((Waypoint[]) savedInstanceState.getParcelableArray(MEMBER_WAYPOINTS));
            }
            if (savedInstanceState.containsKey(MEMBER_ROUTE)) {
                route = (Route) savedInstanceState.getParcelable(MEMBER_ROUTE);
            }
        }
        else if ((intent = getIntent()) != null) {
            if (intent.hasExtra(EXTRA_NAME)) {
                name = intent.getStringExtra(EXTRA_NAME);
            }
        }

        if (name == null || name.length() <= 0) {
            Log.e(LOG_TAG, "Name should not be empty.");
            finish();
        }

        sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);

        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(sectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        }

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getActionBar().setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        if (route == null && waypointArrayAdapter.getCount() > 1) {
            // Wir sollten eigentlich eine Route haben...
            loadRoute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(MEMBER_NAME, name);

        Waypoint[] temp = new Waypoint[waypointArrayAdapter.getCount()];
        for (int i = 0; i < waypointArrayAdapter.getCount(); i++) {
            temp[i] = waypointArrayAdapter.getItem(i);
        }
        outState.putParcelableArray(MEMBER_WAYPOINTS, temp);

        if (route != null) {
            outState.putParcelable(MEMBER_ROUTE, route);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.route_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setEnabled(waypointArrayAdapter.getCount() < MAX_WAYPOINTS);
        Drawable d = menu.getItem(0).getIcon().mutate();
        d.setAlpha(menu.getItem(0).isEnabled() ? 255 : 64);
        menu.getItem(0).setIcon(d);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            showSaveDialog(false);
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_add_waypoint) {
            addWaypoint();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showSaveDialog(true);
    }

    /**
     * Zeigt einen "Möchten Sie speichern?" Dialog an und speichert ggf.
     *
     * @param backButtonPressed <code>true</code> falls die Methode von der <code>onBackPressed</code>
     *                          Methode aufgerufen wurde, <code>false</code> sonst.
     */
    private void showSaveDialog(final boolean backButtonPressed) {
        if (route != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.route_editor_save_dialog);

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    List<LatLng> src = route.getPolylineOptions().getPoints();

                    String encoded = LocationUtils.encodePolyline(src);

                    List<RoutePoint> routePoints = new ArrayList<RoutePoint>(src.size());
                    for (LatLng p: src) {
                        RoutePoint routePoint = new RoutePoint();
                        routePoint.setLatitude(p.latitude);
                        routePoint.setLongitude(p.longitude);
                        routePoints.add(routePoint);
                    }


                    Toast.makeText(getApplicationContext(), "size: " + routePoints.size(), Toast.LENGTH_LONG).show();


                    com.skatenight.skatenightAPI.model.Route rt = new com.skatenight.skatenightAPI.model.Route();
                    rt.setName(name);
                    rt.setRouteData(new Text().setValue(encoded));
                    rt.setRoutePoints(routePoints);
                    // Wegpunkte im Route-Objekt speichern
                    rt.setWaypoints(new ArrayList<ServerWaypoint>());
                    for (int i = 0; i < getArrayAdapter().getCount(); i++) {
                        MarkerOptions mo = getArrayAdapter().getItem(i).getMarkerOptions();
                        ServerWaypoint swp = new ServerWaypoint();
                        swp.setTitle(mo.getTitle());
                        swp.setLongitude(mo.getPosition().longitude);
                        swp.setLatitude(mo.getPosition().latitude);
                        rt.getWaypoints().add(swp);
                    }
                    Toast.makeText(getApplicationContext(), "size: " + rt.getRoutePoints().size(), Toast.LENGTH_LONG).show();

                    String length;
                    if (route.getDistance() < 1000) {
                        length = route.getDistance() + " m";
                        //length = String.format("%i m", route.getDistance()); // Crash?!
                    }
                    else {
                        length = String.format("%.1f km", route.getDistance()/1000.0f);
                    }

                    rt.setLength(length);

                    new AddRouteTask().execute(rt);

                    if (backButtonPressed) {
                        RouteEditorActivity.super.onBackPressed();
                    }
                    else {
                        RouteEditorActivity.this.finish();
                    }
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (backButtonPressed) {
                        RouteEditorActivity.super.onBackPressed();
                    }
                    else {
                        RouteEditorActivity.this.finish();
                    }
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.route_editor_leave_dialog);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    RouteEditorActivity.super.onBackPressed();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    public ArrayAdapter<Waypoint> getArrayAdapter() {
        return waypointArrayAdapter;
    }

    /**
     * Fügt einen neuen Waypoint am Mittelpunkt der Karte hinzu und aktualisiert die Strecke.
     */
    public void addWaypoint() {
        EditorMapFragment mapFragment = (EditorMapFragment) getFragmentByPosition(0);
        waypointArrayAdapter.add(mapFragment.addWaypoint(waypointArrayAdapter.getCount() + 1));
        invalidateOptionsMenu();
        loadRoute();
    }

    /**
     * Entfernt den Waypoint mit dem Index position und aktualisiert die Route.
     * @param position Index des zu löschenden Waypoint.
     */
    public void removeWaypoint(int position) {
        if (position < 0 || position >= waypointArrayAdapter.getCount()) {
            throw new IndexOutOfBoundsException("Index " + position + " is out of bounds.");
        }
        EditorMapFragment mapFragment = (EditorMapFragment) getFragmentByPosition(0);
        Waypoint waypoint = waypointArrayAdapter.getItem(position);
        waypointArrayAdapter.remove(waypoint);
        mapFragment.removeWaypoint(waypoint);

        Toast.makeText(getApplicationContext(),
                getString(R.string.route_editor_waypoint_deleted, waypoint.getMarkerOptions().getTitle()),
                Toast.LENGTH_SHORT).show();

        // Titel der Marker der nachfolgenden Waypoints aktualisieren.
        for (int i = position; i < waypointArrayAdapter.getCount(); i++) {
            waypoint = waypointArrayAdapter.getItem(i);
            waypoint.getMarkerOptions().title(getString(R.string.route_editor_waypoint_name_format, i+1));
            mapFragment.updateWaypoint(waypoint);
        }

        invalidateOptionsMenu();
        loadRoute();
    }

    /**
     * Entfernt die aktuelle Route und läd eine neue im Hintergrund.
     */
    public void loadRoute() {
        setRoute(null);
        if (waypointArrayAdapter.getCount() > 1) {
            currentTask = new RouteLoaderTask(this);
            currentTask.execute(waypointArrayAdapter);
            setProgressBarIndeterminateVisibility(true);
        }
    }

    /**
     * Zeigt die neue Route auf der Karte an. Sollte nur vom RouteLoaderTask aufgerufen werden.
     *
     * @param route Neue Route.
     */
    private void setRoute(Route route) {
        if (this.route != null && this.route.getPolyline() != null) {
            this.route.getPolyline().remove();
        }
        this.route = route;
        EditorMapFragment mapFragment = (EditorMapFragment) getFragmentByPosition(0);
        if (mapFragment == null) {
            return;
        }
        mapFragment.updateRoute(route);
    }

    /**
     * Liefert die aktuelle Route. Falls keine Route vorhanden ist, wird null zurückgegeben.
     *
     * @return Aktuelle Route.
     */
    public Route getRoute() {
        return route;
    }

    /**
     * Zentriert die Karte beim übergebenen Waypoint und öffnet das Info-Fenster.
     *
     * @param waypoint Zu zeigender Waypoint.
     */
    public void showWaypoint(Waypoint waypoint) {
        viewPager.setCurrentItem(0);
        EditorMapFragment mapFragment = (EditorMapFragment) getFragmentByPosition(0);
        mapFragment.showWaypoint(waypoint);
    }

    /**
     * Liefert das Fragment am Index pos.
     *
     * @param pos Index des Fragment.
     * @return Fragment mit Index pos.
     */
    private Fragment getFragmentByPosition(int pos) {
        String tag = "android:switcher:" + viewPager.getId() + ":" + pos;
        return getFragmentManager().findFragmentByTag(tag);
    }

    @Override
    public void taskDidFinish(ExtendedTask task, Route route) {
        if (task == currentTask) {
            setRoute(route);
            setProgressBarIndeterminateVisibility(false);
            currentTask = null;
        }
    }

    @Override
    public void taskDidProgress(ExtendedTask task, Void... progress) {

    }

    @Override
    public void taskFailed(ExtendedTask task, String message) {
        if (task == currentTask) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            setProgressBarIndeterminateVisibility(false);
            currentTask = null;
        }

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return Fragment.instantiate(RouteEditorActivity.this, EditorMapFragment.class.getName());
                case 1:
                    return Fragment.instantiate(RouteEditorActivity.this, EditorWaypointsFragment.class.getName());
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_fragment_route_editor_map);
                case 1:
                    return getString(R.string.title_fragment_route_editor_waypoints);
            }
            return null;
        }
    }

    public class WaypointAdapter extends ArrayAdapter<Waypoint> {

        public WaypointAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(R.id.list_view_item_waypoint_name_textview);
            textView.setText(getItem(position).getMarkerOptions().getTitle());
            //textView.setText(getString(R.string.route_editor_waypoint_name_format, (position + 1)));
            return view;
        }
    }

    /**
     * Klasse für eine auf der Karte anzuzeigende Route. Sie beinhaltet die PolylineOptions zum
     * hinzufügen der Route zur Karte, die Polyline Referenz zur gezeichneten Route auf der Karte
     * und die Länge der Route in Metern (als integer Wert). Sie implementiert das Parcelable
     * Interface, damit die Route zwischengespeichert werden kann. Es werden jedoch lediglich das
     * PolylineOptions Objekt und die Routenlänge gespeichert. Die Polyline wird auf null gesetzt,
     * kann aber mithilfe der PolylineOptions neu gezeichnet werden.
     */
    public static class Route implements Parcelable {
        private PolylineOptions polylineOptions;
        private Polyline polyline;

        private int distance; // Distance in m

        public Route(List<LatLng> line, int distance) throws ParseException {
            this.polylineOptions = new PolylineOptions()
                    .color(Color.BLUE);
            this.polylineOptions.addAll(line);

            this.polyline = null;

            this.distance = distance;
        }

        private Route(Parcel in) {
            this.polylineOptions = in.readParcelable(MarkerOptions.class.getClassLoader());
            this.polyline = null;

            this.distance = in.readInt();
        }

        public PolylineOptions getPolylineOptions() {
            return polylineOptions;
        }

        public void setPolyline(Polyline polyline) {
            this.polyline = polyline;
        }

        public Polyline getPolyline() {
            return polyline;
        }

        public String getEncoded() {
            return LocationUtils.encodePolyline(getPolylineOptions().getPoints());
        }

        public int getDistance() {
            return distance;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(polylineOptions, flags);
            out.writeInt(distance);
        }

        public static final Parcelable.Creator<Route> CREATOR
                = new Parcelable.Creator<Route>() {
            public Route createFromParcel(Parcel in) {
                return new Route(in);
            }

            public Route[] newArray(int size) {
                return new Route[size];
            }
        };
    }

    /**
     * Klasse für einen auf der Karte anzuzeigenden Wegpunkt. Sie beinhaltet die MarkerOptions zum
     * hinzufügen des Markers auf der Karte und die Marker Referenz zum dargestellten Marker auf
     * der Karte. Falls der Waypoint derzeit nicht auf der Karte sichtbar ist, ist dieser null.
     * Die Klasse implementiert das Parcelable Interface, damit die Wegpunkte zwischengespeichert
     * werden können. Es wird jedoch lediglich das MarkerOptions Objekt gespeichert. Der Marker
     * wird auf null gesetzt, kann aber mithilfe der MarkerOptions neu erzeugt werden.
     */
    public static class Waypoint implements Parcelable {
        private MarkerOptions markerOptions;
        private Marker marker;

        public static Waypoint create(LatLng position, String title) {
            return new Waypoint(
                    new MarkerOptions()
                            .title(title)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .position(position)
                            .draggable(true)

            );
        }

        private Waypoint(MarkerOptions options) {
            markerOptions = options;
            marker = null;
        }

        private Waypoint(Parcel in) {
            markerOptions = in.readParcelable(MarkerOptions.class.getClassLoader());
            marker = null;
        }

        public void setMarkerOptions(MarkerOptions markerOptions) {
            this.markerOptions = markerOptions;
        }

        public MarkerOptions getMarkerOptions() {
            return markerOptions;
        }

        public void setMarker(Marker marker) {
            this.marker = marker;
        }

        public Marker getMarker() {
            return marker;
        }

        public String toString() {
            return markerOptions.getPosition().toString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(markerOptions, flags);
        }

        public static final Parcelable.Creator<Waypoint> CREATOR
                = new Parcelable.Creator<Waypoint>() {
            public Waypoint createFromParcel(Parcel in) {
                return new Waypoint(in);
            }

            public Waypoint[] newArray(int size) {
                return new Waypoint[size];
            }
        };
    }

    public SectionsPagerAdapter getSectionsPagerAdapter() {
        return sectionsPagerAdapter;
    }

    public static RouteEditorActivity getRouteEditorActivity() {
        return routeEditorActivity;
    }
}
