package ws1415.veranstalterapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


public class EditorWaypointsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_editor_waypoints, container, false);

        if (getActivity() instanceof RouteEditorActivity) {
            final RouteEditorActivity activity = (RouteEditorActivity) getActivity();
            ListView listView = (ListView) view.findViewById(R.id.route_editor_waypoints_listview);
            listView.setAdapter(activity.getWaypointArrayAdapter());
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ((RouteEditorActivity) getActivity()).showWaypoint((RouteEditorActivity.Waypoint) parent.getAdapter().getItem(position));
                }
            });
        }

        return view;
    }
}
