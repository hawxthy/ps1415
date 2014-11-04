package ws1415.veranstalterapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

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
            for (int i = 0; i < activity.getWaypointArrayAdapter().getCount(); i++) {
                updateWaypoint(activity.getWaypointArrayAdapter().getItem(i));
            }
            updateRoute(activity.getRoute());
        }

        if (savedInstanceState == null) {
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
            for (int i = 0; i < activity.getWaypointArrayAdapter().getCount(); i++) {
                if ((wp = activity.getWaypointArrayAdapter().getItem(i)).getMarker().equals(marker)) {
                    wp.setMarkerOptions(wp.getMarkerOptions().position(marker.getPosition()));
                    activity.getWaypointArrayAdapter().notifyDataSetChanged();

                    break;
                }
            }
            activity.loadRoute();
        }
    }

    public GoogleMap getMap() {
        return googleMap;
    }

    public void updateRoute(RouteEditorActivity.Route route) {
        if (route == null) return;
        if (route.getPolyline() != null) {
            route.getPolyline().remove();
            route.setPolyline(null);
        }

        route.setPolyline(googleMap.addPolyline(route.getPolylineOptions()));
    }

    public void updateWaypoint(RouteEditorActivity.Waypoint waypoint) {
        if (waypoint.getMarker() != null) {
            waypoint.getMarker().remove();
            waypoint.setMarker(null);
        }
        waypoint.setMarker(googleMap.addMarker(waypoint.getMarkerOptions()));
    }
}
