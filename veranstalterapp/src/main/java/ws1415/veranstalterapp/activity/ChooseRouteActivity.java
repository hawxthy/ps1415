package ws1415.veranstalterapp.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ws1415.veranstalterapp.adapter.MapsCursorAdapter;
import ws1415.veranstalterapp.fragment.AnnounceInformationFragment;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.ServiceProvider;

/**
 * Activity zum Auswählen einer Route für das zu erstellende Event.
 *
 * @author Bernd Eissing, Marting Wrodarczyk on 21.11.2014.
 */
public class ChooseRouteActivity extends Activity {
    private ListView lv;
    private List<Route> routeList;
    private MapsCursorAdapter mAdapter;
    private static EditEventActivity activity;

    /**
     * Initialisiert die ListView, setzt den OnClickListener und füllt die Liste in der View
     * mit den Routen vom Server
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_route);

        lv = (ListView) findViewById(R.id.choose_routes_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((AnnounceInformationFragment)HoldTabsActivity.getAdapter().getItem(1)).getAdapter().setRouteAndText(routeList.get(i));
                if(activity != null){
                    activity.getAdapter().setRouteAndText(routeList.get(i));
                }
                finish();
            }
        });
        new PrivateQueryRouteTask().execute(this);
    }

    /**
     * Schreibt die Routen aus der übergebenen Liste in die ListView der Activity
     *
     * @param results die Liste der Routen
     */
    public void setRoutesToListView(ArrayList<Route> results) {
        routeList = results;
        mAdapter = new MapsCursorAdapter(this, results);
        lv.setAdapter(mAdapter);
    }


    /**
     * Klasse, welche mit SkatenightBackend kommunizert um auf den Server zuzugreifen.
     *
     * Created by Bernd Eissing, Martin Wrodarczyk on 04.11.2014.
     */
    private class PrivateQueryRouteTask extends AsyncTask<ChooseRouteActivity, Void, List<Route>> {
        private ChooseRouteActivity view;

        /**
         * Ruft die Liste der Routen vom Server ab und gibt diese aus
         * @return die Liste der Routen vom Server
         */
        @Override
        protected List<Route> doInBackground(ChooseRouteActivity... params){
            view =params[0];
            try{
                return ServiceProvider.getService().skatenightServerEndpoint().getRoutes().execute().getItems();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Ruft setRoutesToListView in ChooseRouteActivity auf.
         *
         * @param results
         */
        @Override
        protected void onPostExecute(List<Route> results){
            view.setRoutesToListView((ArrayList<Route>) results);
        }
    }

    /**
     * Übergibt die Instanz der EditEventActivity der ChooseRouteActivity
     *
     * @param eea die Instanz der EditEventActivity
     */
    public static void giveEditEventActivity(EditEventActivity eea){
        activity = eea;
    }
}
