package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.Event;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.EventsCursorAdapter;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.ps1415.task.QueryEventsTask;

public class ShowEventsActivity extends Activity implements ExtendedTaskDelegate<Void, List<Event>> {
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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_show_events);
        setProgressBarIndeterminateVisibility(true);

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
                intent.putExtra(ShowInformationActivity.EXTRA_KEY_ID, eventList.get(i).getKey().getId());
                startActivity(intent);
            }
        });

        new QueryEventsTask(this).execute();
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

    @Override
    public void taskDidFinish(ExtendedTask task, List<Event> events) {
        eventList = events;
        mAdapter = new EventsCursorAdapter(this, events);
        eventListView.setAdapter(mAdapter);
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void taskDidProgress(ExtendedTask task, Void... progress) {

    }

    @Override
    public void taskFailed(ExtendedTask task, String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        setProgressBarIndeterminateVisibility(false);
    }
}
