package ws1415.veranstalterapp;
import android.os.AsyncTask;

import com.appspot.skatenight_ms.skatenightAPI.model.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import ws1415.veranstalterapp.Adapter.MapsCursorAdapter;
import ws1415.veranstalterapp.Fragments.AnnounceInformationFragment;


public class ChooseRouteActivity extends Activity {
    private ListView lv;
    private List<Route> routeList;
    private MapsCursorAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_route);

        lv = (ListView) findViewById(R.id.choose_routes_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((AnnounceInformationFragment)HoldTabsActivity.getAdapter().getItem(1)).setRoute(routeList.get(i));
                finish();
            }
        });
        new QueryRouteTask2().execute(this);
    }

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
    private class QueryRouteTask2 extends AsyncTask<ChooseRouteActivity, Void, List<Route>> {
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

        @Override
        protected void onPostExecute(List<Route> results){
            view.setRoutesToListView((ArrayList<Route>) results);
        }
    }
}
