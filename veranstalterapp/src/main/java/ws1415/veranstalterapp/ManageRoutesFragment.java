package ws1415.veranstalterapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;

import com.appspot.skatenight_ms.skatenightAPI.model.Route;

import java.util.ArrayList;
import java.util.List;

import ws1415.veranstalterapp.Adaper.MapsCursorAdapter;

/**
 *
 */
public class ManageRoutesFragment extends Fragment {
    private ListView routeList;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_manage_routes, container, false);

        routeList = (ListView) view.findViewById(R.id.manage_routes_route_list);
        new QueryRouteTask().execute(this);
        routeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            }
        });

        routeList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                createSelectionsMenu(i);
                return true;
            }
        });
        return view;
    }

    public void setRoutesToListView(ArrayList<Route> results) {
        routeList.setAdapter(new MapsCursorAdapter(getActivity(), results));
    }

    private void createSelectionsMenu(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getRoutes().get(position).getName())
                .setItems(R.array.selections_menu, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int index) {
                        if(index == 0){
                            // TODO deleteRoute(id) erstellen und hier aufrufen
                            Log.d("Test", "deleteRoute");
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    private ArrayList<Route> getRoutes(){
        ArrayList<Route> tmpList = new ArrayList<Route>();
        // Erstelle test Routes
        Route route1 = new Route();
        Route route2 = new Route();
        Route route3 = new Route();
        route1.setName("Route 1");
        route2.setName("Route 2");
        route3.setName("Route 3");
        route1.setLength("10");
        route2.setLength("11");
        route3.setLength("12");

        tmpList.add(route1);
        tmpList.add(route2);
        tmpList.add(route3);

        return tmpList;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.manage_routes, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }
}
