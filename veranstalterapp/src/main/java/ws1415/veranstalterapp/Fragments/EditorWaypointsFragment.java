package ws1415.veranstalterapp.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.RouteEditorActivity;


public class EditorWaypointsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_editor_waypoints, container, false);

        if (getActivity() instanceof RouteEditorActivity) {
            final RouteEditorActivity activity = (RouteEditorActivity) getActivity();
            ListView listView = (ListView) view.findViewById(R.id.route_editor_waypoints_listview);
            listView.setAdapter(activity.getArrayAdapter());
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ((RouteEditorActivity) getActivity()).showWaypoint((RouteEditorActivity.Waypoint) parent.getAdapter().getItem(position));
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    RouteEditorActivity activity = (RouteEditorActivity) getActivity();
                    activity.removeWaypoint(position);
                    return true;
                }
            });
        }

        return view;
    }
}
