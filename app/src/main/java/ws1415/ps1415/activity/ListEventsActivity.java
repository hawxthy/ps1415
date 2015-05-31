package ws1415.ps1415.activity;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.EventFilter;

import ws1415.common.gcm.GCMUtil;
import ws1415.common.net.ServiceProvider;
import ws1415.ps1415.Constants;
import ws1415.ps1415.R;
import ws1415.ps1415.adapter.EventAdapter;
import ws1415.ps1415.fragment.EventListFragment;
import ws1415.ps1415.util.LocalGCMUtil;
import ws1415.ps1415.util.PrefManager;

public class ListEventsActivity extends BaseActivity implements EventListFragment.OnEventClickListener {
    /**
     * Falls diese Activity einen Intent mit der REFRESH_EVENTS_ACTION erhält, wird die Liste der
     * Events aktualisiert.
     */
    public static final String REFRESH_EVENTS_ACTION = "REFRESH_EVENTS";
    private static final String TAG = "Skatenight";
    public static final int REQUEST_ACCOUNT_PICKER = 2;
    /**
     * Bestimmt die Anzahl Events, die pro Aufruf an den Server herunter geladen werden.
     */
    private static final int EVENTS_PER_REQUEST = 15;

    private GoogleCloudMessaging gcm;
    private String regid;
    private Context context;

    private SharedPreferences prefs;
    private GoogleAccountCredential credential;

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

        // Einstellungen müssen als erstes beim App Start geladenw werden
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_show_events);
        context = this;

        // SharedPreferences skatenight.app laden
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        credential = GoogleAccountCredential.usingAudience(this, "server:client_id:" + Constants.WEB_CLIENT_ID);

        if(PrefManager.getSelectedUserMail(context).equals("")){
            context.startActivity(new Intent(this, RegisterActivity.class));
            finish();
        } else {
            credential.setSelectedAccountName(PrefManager.getSelectedUserMail(context));
            ServiceProvider.login(credential);
            initGCM();
        }

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

    private void initGCM() {
        // GCM initialisieren
        context = this;
        if (GCMUtil.checkPlayServices(this)) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = LocalGCMUtil.getRegistrationId(context);

            if (regid.isEmpty()) {
                LocalGCMUtil.registerInBackground(context, gcm);
            } else {
                LocalGCMUtil.sendRegistrationIdToBackend(regid);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
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
            Intent intent = new Intent(ListEventsActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_refresh_events) {
            refresh();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback-Methode für den Account-Picker.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);

                        // accountName in den SharedPreferences speichern
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("accountName", accountName);
                        editor.commit();

                        ServiceProvider.login(credential);
                        initGCM();

                        Log.i(TAG, "User: " + ServiceProvider.getEmail() + " created.");
                        PrefManager.setSelectedUserMail(context, ServiceProvider.getEmail());
                    }
                }
                break;
        }
    }

    @Override
    public void onEventClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, ShowEventActivity.class);
        intent.putExtra(ShowEventActivity.EXTRA_EVENT_ID, id);
        startActivity(intent);
    }
}
