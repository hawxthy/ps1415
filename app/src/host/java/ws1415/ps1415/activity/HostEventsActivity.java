package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.EventFilter;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.EventAdapter;
import ws1415.ps1415.fragment.EventListFragment;
import ws1415.ps1415.util.UniversalUtil;

public class HostEventsActivity extends BaseActivity implements EventListFragment.OnEventClickListener {
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
        setContentView(R.layout.activity_host_events);

        // EventFragment initialisieren
        eventFragment = (EventListFragment) getFragmentManager().findFragmentById(R.id.eventFragment);
        EventFilter filter = new EventFilter();
        filter.setLimit(EVENTS_PER_REQUEST);
        eventAdapter = new EventAdapter(this, filter);
        eventFragment.setListAdapter(eventAdapter);
    }

    private void refresh(){
        eventAdapter.refresh();
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
    public boolean onEventLongClick(AdapterView<?> parent, View v, int position, final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(eventAdapter.getEvent(position).getTitle())
                .setItems(R.array.host_events_event_actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Anzeigen
                                Intent intent = new Intent(HostEventsActivity.this, ShowEventActivity.class);
                                intent.putExtra(ShowEventActivity.EXTRA_EVENT_ID, id);
                                startActivity(intent);
                                break;
                            case 1:
                                // Bearbeiten
                                // TODO
                                break;
                            case 2:
                                // Löschen
                                // TODO
                                break;
                        }
                    }
                });
        builder.create().show();
        return true;
    }
}
