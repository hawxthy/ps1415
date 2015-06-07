package ws1415.ps1415.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.RouteEditorActivity;

public class EditorMapFragment extends Fragment implements GoogleMap.OnMarkerDragListener {
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_route_editor_map, container, false);

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.route_editor_map_fragment)).getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerDragListener(this);

        if (getActivity() instanceof RouteEditorActivity) {
            RouteEditorActivity activity = (RouteEditorActivity) getActivity();
            for (int i = 0; i < activity.getArrayAdapter().getCount(); i++) {
                updateWaypoint(activity.getArrayAdapter().getItem(i));
            }
            updateRoute(activity.getRoute());
        }

        if (savedInstanceState == null) {
            // Falls die Karte nicht wiederhergestellt wird, wird die Karte auf Münster zentriert.
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.960633, 7.626133), 12.0f));
        }

        return view;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        if (getActivity() instanceof RouteEditorActivity) {
            RouteEditorActivity activity = (RouteEditorActivity) getActivity();
            RouteEditorActivity.Waypoint wp = null;

            // Sucht den Waypoint des aktuellen Markers und aktualisiert dessen Position.
            for (int i = 0; i < activity.getArrayAdapter().getCount(); i++) {
                if ((wp = activity.getArrayAdapter().getItem(i)).getMarker().equals(marker)) {
                    wp.setMarkerOptions(wp.getMarkerOptions().position(marker.getPosition()));
                    activity.getArrayAdapter().notifyDataSetChanged();
                    break;
                }
            }
            activity.loadRoute();
        }
    }

    /**
     * Aktualisiert die übergebene Route. Falls die Route noch nicht auf der Karte angezeigt wird,
     * wird sie hinzugefügt, andernfalls wird sie zunächst gelöscht und anschließend wieder
     * hinzugefügt. Falls null übergeben wird, wird nichts gemacht.
     *
     * @param route Zu aktualisierende Route.
     */
    public void updateRoute(RouteEditorActivity.Route route) {
        if (route == null) return;
        if (route.getPolyline() != null) {
            // Vorherige Polyline entfernen
            route.getPolyline().remove();
            route.setPolyline(null);
        }

        // Neue Polyline hinzufügen
        route.setPolyline(googleMap.addPolyline(route.getPolylineOptions()));
    }

    /**
     * Erzeugt einen neuen Waypoint und fügt diesen zur Karte hinzu.
     * Der Index beeinflusst lediglich den Namen.
     *
     * @param index Nummer des Waypoint z.B. 2 für "Wegpunkt 2".
     * @return Erstellter Waypoint.
     */
    public RouteEditorActivity.Waypoint addWaypoint(int index) {
        RouteEditorActivity.Waypoint waypoint = RouteEditorActivity.Waypoint.create(
                googleMap.getCameraPosition().target,
                getString(R.string.route_editor_waypoint_name_format, index));

        waypoint.setMarker(googleMap.addMarker(waypoint.getMarkerOptions()));
        return waypoint;
    }

    /**
     * Entfernt den Waypoint von der Karte und fügt ihn anschließend wieder hinzu.
     *
     * @param waypoint Zu aktualisierender Waypoint.
     */
    public void updateWaypoint(RouteEditorActivity.Waypoint waypoint) {
        if (waypoint.getMarker() != null) {
            waypoint.getMarker().remove();
            waypoint.setMarker(null);
        }

        waypoint.setMarker(googleMap.addMarker(waypoint.getMarkerOptions()));
    }

    /**
     * Entfernt den Waypoint von der Karte.
     *
     * @param waypoint
     */
    public void removeWaypoint(RouteEditorActivity.Waypoint waypoint) {
        if (waypoint.getMarker() != null) {
            waypoint.getMarker().remove();
            waypoint.setMarker(null);
        }
    }

    /**
     * Zentriert die Karte auf dem übergebenen Waypoint und öffnet dessen Info-Fenster.
     *
     * @param waypoint Zu zeigender Waypoint.
     */
    public void showWaypoint(RouteEditorActivity.Waypoint waypoint) {
        if (waypoint.getMarker() != null) {
            // Kamera zentrieren
            googleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(waypoint.getMarkerOptions().getPosition(), 15.0f)
                    )
            );

            waypoint.getMarker().showInfoWindow();
        }
    }
}
