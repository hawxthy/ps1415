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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Event;

import java.util.Date;
import java.util.List;

import ws1415.common.gcm.GCMUtil;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.task.QueryEventsTask;
import ws1415.ps1415.Constants;
import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.ps1415.adapter.EventsCursorAdapter;
import ws1415.ps1415.util.LocalGCMUtil;
import ws1415.ps1415.util.PrefManager;

public class ShowEventsActivity extends BaseActivity implements ExtendedTaskDelegate<Void, List<Event>> {
    /**
     * Falls diese Activity einen Intent mit der REFRESH_EVENTS_ACTION erhält, wird die Liste der
     * Events aktualisiert.
     */
    public static final String REFRESH_EVENTS_ACTION = "REFRESH_EVENTS";
    private static final String TAG = "Skatenight";
    public static final int SETTINGS_RESULT = 1;
    public static final int REQUEST_ACCOUNT_PICKER = 2;

    private ListView eventListView;
    private List<Event> eventList;
    private EventsCursorAdapter mAdapter;

    // Komponenten und Variablen für GCM
    private String SENDER_ID = Constants.GCM_PROJECT_ID;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private GoogleCloudMessaging gcm;
    private String regid;
    private Context context;

    private SharedPreferences prefs;
    private GoogleAccountCredential credential;


    /**
     * Fragt alle Events vom Server ab und fügt diese in die Liste ein
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

        // SharePreferences skatenight.app laden
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        credential = GoogleAccountCredential.usingAudience(this, "server:client_id:" + Constants.WEB_CLIENT_ID);

        if(PrefManager.getSelectedUserMail(context).equals("")){
            context.startActivity(new Intent(this, RegisterActivity.class));
            finish();
        } else {
            credential.setSelectedAccountName(PrefManager.getSelectedUserMail(context));
            String s = PrefManager.getSelectedUserMail(context);
            ServiceProvider.login(credential);
            initGCM();
        }

        super.onCreate(savedInstanceState);

        // Einstellungen müssen als erstes beim App Start geladenw werden
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

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
                Event e = mAdapter.getItem(i);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Intent intent;

                Date startDate = new Date(e.getDate().getValue());
                if (new Date().after(startDate) &&
                        e.getMemberList() != null &&
                        e.getMemberList().containsKey(prefs.getString("accountName", null))) {

                    if (!prefs.getBoolean(e.getId()+"-started", false)) {
                        LocationTransmitterService.ScheduleService(ShowEventsActivity.this, e.getId(), startDate);
                    }

                    intent = new Intent(ShowEventsActivity.this, ActiveEventActivity.class);
                    intent.putExtra(ActiveEventActivity.EXTRA_KEY_ID, e.getId());
                }
                else {
                    intent = new Intent(ShowEventsActivity.this, ShowInformationActivity.class);
                    intent.putExtra(ShowInformationActivity.EXTRA_KEY_ID, e.getId());
                }
                startActivity(intent);
            }
        });

        // Listener für REFRESH_EVENTS_ACTION-Intents erstellen
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                new QueryEventsTask(ShowEventsActivity.this).execute();
            }
        }, new IntentFilter(REFRESH_EVENTS_ACTION));

        //new QueryEventsTask(this).execute();
    }

    private void refresh(){
        setProgressBarIndeterminateVisibility(true);
        new QueryEventsTask(ShowEventsActivity.this).execute();
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
            Intent intent = new Intent(ShowEventsActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_refresh_events) {
            refresh();
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
}
