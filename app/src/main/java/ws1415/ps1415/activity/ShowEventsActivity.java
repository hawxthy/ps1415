package ws1415.ps1415.activity;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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

import java.io.IOException;
import java.util.Date;
import java.util.List;

import ws1415.common.gcm.GCMUtil;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.ps1415.Constants;
import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.common.net.ServiceProvider;
import ws1415.ps1415.adapter.EventsCursorAdapter;
import ws1415.common.task.QueryEventsTask;

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
                        e.getMemberList().contains(prefs.getString("accountName", null))) {

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

        // SharePreferences skatenight.app laden
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        credential = GoogleAccountCredential.usingAudience(this, "server:client_id:" + Constants.WEB_CLIENT_ID);

        // accountName aus SharedPreferences laden
        if (prefs.contains("accountName")) {
            credential.setSelectedAccountName(prefs.getString("accountName", null));
        }

        // Kein accountName gesetzt, also AccountPicker aufrufen
        if (credential.getSelectedAccountName() == null) {
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        } else {
            ServiceProvider.login(credential);
            initGCM();
        }

        // Listener für REFRESH_EVENTS_ACTION-Intents erstellen
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                new QueryEventsTask(ShowEventsActivity.this).execute();
            }
        }, new IntentFilter(REFRESH_EVENTS_ACTION));

        new QueryEventsTask(this).execute();
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
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            } else {
                sendRegistrationIdToBackend();
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
                    }
                }
                break;
        }
    }

    // ---------- Methoden für GCM ----------
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(ShowEventsActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.e(TAG, msg);
            }
        }.execute(null, null, null);
    }

    /**
     * Sendet die Registration-ID für GCM an das Backend.
     */
    private void sendRegistrationIdToBackend() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().userEndpoint().registerForGCM(regid).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(null, null, null);
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

}
