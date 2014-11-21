package ws1415.ps1415.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.Event;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.EventsCursorAdapter;
import ws1415.ps1415.task.QueryEventTask;

public class ShowEventsActivity extends Activity {
    private ListView eventListView;
    private List<Event> eventList;
    private EventsCursorAdapter mAdapter;

    /**
     * Fragt alle Events vom Server ab und fügt diese in die Liste ein
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_events);

        // ListView initialisieren
        eventListView = (ListView) findViewById(R.id.activity_show_events_list_view);

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Ruft die showRouteActivity auf, die die ausgewählte Route anzeigt.
             *
             * @param adapterView
             * @param view
             * @param i Index der ausgewählten Route in der ListView
             * @param l
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ShowEventsActivity.this, ShowInformationActivity.class);
                intent.putExtra("event", eventList.get(i).getKey().getId());
                startActivity(intent);
            }
        });

        new QueryEventTask().execute(this);
    }

    /**
     * Setzt die Events in die Liste
     */
    @Override
    public void onResume(){
        super.onResume();

        eventListView.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_events, menu);
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
     * Füllt die ListView mit den Events vom Server.
     *
     * @param results ArrayList von Events
     */
    public void setEventsToListView(List<Event> results) {
        eventList = results;
        mAdapter = new EventsCursorAdapter(this, results);
        eventListView.setAdapter(mAdapter);
    }
}
