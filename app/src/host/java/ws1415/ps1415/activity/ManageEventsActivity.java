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
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.EventFilter;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.EventAdapter;
import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.fragment.EventListFragment;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.UniversalUtil;

public class ManageEventsActivity extends BaseActivity implements EventListFragment.OnEventClickListener {
    private static final int CREATE_EVENT_REQUEST_CODE = 0;

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
        getMenuInflater().inflate(R.menu.menu_manage_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.action_settings) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_create_event) {
            intent = new Intent(this, EditEventActivity.class);
            startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
            return true;
        } else if (id == R.id.action_refresh_events) {
            refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CREATE_EVENT_REQUEST_CODE:
                refresh();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onEventClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, ShowEventActivity.class);
        intent.putExtra(ShowEventActivity.EXTRA_EVENT_ID, id);
        startActivity(intent);
    }

    @Override
    public boolean onEventLongClick(AdapterView<?> parent, View v, final int position, final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(eventAdapter.getItem(position).getTitle())
                .setItems(R.array.manage_events_event_actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        switch (which) {
                            case 0:
                                // Anzeigen
                                intent = new Intent(ManageEventsActivity.this, ShowEventActivity.class);
                                intent.putExtra(ShowEventActivity.EXTRA_EVENT_ID, id);
                                startActivity(intent);
                                break;
                            case 1:
                                // Bearbeiten
                                intent = new Intent(ManageEventsActivity.this, EditEventActivity.class);
                                intent.putExtra(EditEventActivity.EXTRA_EVENT_ID, id);
                                startActivity(intent);
                                break;
                            case 2:
                                // Teilnehmer bearbeiten
                                intent = new Intent(ManageEventsActivity.this, ManageEventParticipantsActivity.class);
                                intent.putExtra(ManageEventParticipantsActivity.EXTRA_EVENT_ID, id);
                                startActivity(intent);
                                break;
                            case 3:
                                // Löschen
                                AlertDialog.Builder builder = new AlertDialog.Builder(ManageEventsActivity.this);
                                builder.setTitle(R.string.delete_event)
                                        .setMessage(R.string.delete_event_message)
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                EventController.deleteEvent(new ExtendedTaskDelegateAdapter<Void, Void>() {
                                                    @Override
                                                    public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                                        onEventDeleted(position);
                                                    }

                                                    @Override
                                                    public void taskFailed(ExtendedTask task, String message) {
                                                        Toast.makeText(ManageEventsActivity.this, R.string.event_deletion_error, Toast.LENGTH_LONG).show();
                                                    }
                                                }, id);
                                            }
                                        })
                                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                builder.create().show();
                                break;
                        }
                    }
                });
        builder.create().show();
        return true;
    }

    /**
     * Wird vom EventController aufgerufen, sobald ein Event gelöscht wurde.
     * @param position    Die Position des Events, das gelöscht wurde.
     */
    private void onEventDeleted(int position) {
        eventAdapter.removeItem(position);
    }

}
