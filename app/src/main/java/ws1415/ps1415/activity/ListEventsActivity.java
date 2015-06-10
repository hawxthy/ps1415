package ws1415.ps1415.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.EventFilter;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.EventAdapter;
import ws1415.ps1415.fragment.EventListFragment;
import ws1415.ps1415.gcm.GCMUtil;
import ws1415.ps1415.util.UniversalUtil;

public class ListEventsActivity extends BaseActivity implements EventListFragment.OnEventClickListener {
    /**
     * Falls diese Activity einen Intent mit der REFRESH_EVENTS_ACTION erhält, wird die Liste der
     * Events aktualisiert.
     */
    public static final String REFRESH_EVENTS_ACTION = "REFRESH_EVENTS";
    /**
     * Bestimmt die Anzahl Events, die pro Aufruf an den Server herunter geladen werden.
     */
    private static final int EVENTS_PER_REQUEST = 15;

    private EventListFragment eventFragment;
    private EventAdapter eventAdapter;


    /**
     * Zeigt aktuelle Events an.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }

        // Einstellungen müssen als erstes beim App Start geladenw werden
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_list_events);

        // EventFragment initialisieren
        eventFragment = (EventListFragment) getFragmentManager().findFragmentById(R.id.eventFragment);
        EventFilter filter = new EventFilter();
        filter.setLimit(EVENTS_PER_REQUEST);
        eventAdapter = new EventAdapter(this, filter);
        eventFragment.setListAdapter(eventAdapter);

        // Listener für REFRESH_EVENTS_ACTION-Intents erstellen
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {

                refresh();
            }
        }, new IntentFilter(REFRESH_EVENTS_ACTION));
    }

    private void refresh(){
        eventAdapter.refresh();
    }

    /**
     * Setzt die Events in die Liste
     */
    @Override
    public void onResume(){
        super.onResume();
        GCMUtil.checkPlayServices(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_refresh_events) {
            refresh();
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onEventClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, ShowEventActivity.class);
        intent.putExtra(ShowEventActivity.EXTRA_EVENT_ID, id);
        startActivity(intent);
    }

    @Override
    public boolean onEventLongClick(AdapterView<?> parent, View v, int position, long id) {
        return false;
    }
}
