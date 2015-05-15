package ws1415.ps1415.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Event;

import java.util.Date;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.Constants;
import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.common.task.GetEventTask;
import ws1415.ps1415.task.ToggleMemberEventAttendanceTask;

/**
 * Activity zum Begutachten der Metainformationen der erstellten Veranstaltung.
 *
 * @author Bernd Eissing, Marting Wrodarczyk, Pascal Otto
 */
public class ShowInformationActivity extends Activity implements ExtendedTaskDelegate<Void, Object> {
    private static final String LOG_TAG = ShowInformationActivity.class.getSimpleName();
    
    public static final int REQUEST_ACCOUNT_PICKER = 2;

    public static final String EXTRA_KEY_ID = "show_information_extra_key_id";

    // Adapter für die ListView von activity_show_information_list_view
    // private ShowCursorAdapter listAdapter;

    // Die ListView von der xml datei activity_show_information
    private ListView listView;
    private long keyId;
    private boolean attending;

    // Das aktuelle Event
    private Event event;
    // Ob das Event gerade aktiv
    private boolean active;
    private Date startDate;

    private SharedPreferences prefs;
    private GoogleAccountCredential credential;

    /**
     * Erstellt die View und zeigt die Informationen in der ShowInformationActivity.
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_information);

        // SharePreferences skatenight.app laden
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Intent intent;

        if ((intent = getIntent()) != null) {
            keyId = intent.getLongExtra(EXTRA_KEY_ID, 0);
            new GetEventTask(new ExtendedTaskDelegateAdapter<Void, Event>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Event event) {
                    setEventInformation(event);
                }
            }).execute(keyId);
        }

        final Button attendButton = (Button) findViewById(R.id.show_info_attend_button);
        attendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SharePreferences skatenight.app laden
                credential = GoogleAccountCredential.usingAudience(ShowInformationActivity.this, "server:client_id:" + Constants.WEB_CLIENT_ID);

                // accountName aus SharedPreferences laden
                if (prefs.contains("accountName")) {
                    credential.setSelectedAccountName(prefs.getString("accountName", null));
                }

                // Kein accountName gesetzt, also AccountPicker aufrufen
                if (credential.getSelectedAccountName() == null) {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                }
                else {
                    new ToggleMemberEventAttendanceTask(ShowInformationActivity.this, keyId, credential.getSelectedAccountName(), attending, ShowInformationActivity.this).execute();
                }
            }
        });
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

                        new ToggleMemberEventAttendanceTask(this, keyId, credential.getSelectedAccountName(), attending, this).execute();
                    }
                }
                break;
        }
    }

    /**
     * Speichert die Usersettings in einem String.
     */
    private void displayUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String  settings = "";

        settings=settings+"Position Senden:"+ sharedPrefs.getBoolean("prefSendLocation", false);
    }

    @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_information, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(ShowInformationActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // public ShowCursorAdapter getShowCursorAdapter() {
    //     return listAdapter;
    // }

    public Event getEvent() {
        return event;
    }

    /**
     * Übernimmt die Informationen aus dem übergebenen Event-Objekt in die GUI-Elemente.
     *
     * @param e Das neue Event-Objekt.
     */
    public void setEventInformation(Event e) {
        Button attendButton = (Button) findViewById(R.id.show_info_attend_button);
        if (e != null) {
            event = e;
            setTitle(e.getTitle());
            // TODO Verwendung von Dynamic Fields entfernen
            // listAdapter = new ShowCursorAdapter(this, e.getDynamicFields(), e);

            listView = (ListView) findViewById(R.id.activity_show_information_list_view);
            // listView.setAdapter(listAdapter);

            // Prüft, ob das aktuelle Datum nach dem Start-Datum des Events liegt.
            // Also ob es bereits gestartet ist. Somit wird der Server gestartet.
            startDate = new Date(e.getDate().getValue());
            Date today = new Date();
            if (today.after(startDate)) {
                active = true;
            } else {
                active = false;
            }

            attendButton.setEnabled(true);

            if (prefs.contains("accountName") && e.getMemberList() != null) {
                attending = e.getMemberList().contains(prefs.getString("accountName", ""));
            }
            else {
                attending = false;
            }
            updateAttendButtonTitle();
        } else {
            active = false;
            startDate = null;
            attendButton.setEnabled(false);
            attendButton.setText(getString(R.string.show_info_button_attend));
        }
    }


    public void updateAttendButtonTitle() {
        Button attendButton = (Button) findViewById(R.id.show_info_attend_button);
        if (attending) {
            attendButton.setText(getString(R.string.show_info_button_leave));
        }
        else {
            attendButton.setText(getString(R.string.show_info_button_attend));
        }
    }

    @Override
    public void taskDidFinish(ExtendedTask task, Object result) {
        if (task instanceof GetEventTask) {
            setEventInformation((Event) result);
        }
        else if (task instanceof ToggleMemberEventAttendanceTask) {
            attending = (Boolean) result;
            if (attending) {
                Toast.makeText(getApplicationContext(), getString(R.string.show_info_toast_attending), Toast.LENGTH_SHORT).show();

                if (prefs.contains(Long.toString(keyId))) {
                    prefs.edit().remove(Long.toString(keyId))
                            .commit();
                }
                new GetEventTask(new ExtendedTaskDelegate<Void, Event>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Event e) {
                        Date startDate = new Date(e.getDate().getValue());
                        LocationTransmitterService.ScheduleService(ShowInformationActivity.this, keyId, startDate);
                    }

                    @Override
                    public void taskDidProgress(ExtendedTask task, Void[] progress) {

                    }

                    @Override
                    public void taskFailed(ExtendedTask task, String message) {
                        Log.e(LOG_TAG, "Unable to register alarm (" + keyId + "): " + message);
                    }
                }).execute(keyId);

            }
            else {
                Toast.makeText(getApplicationContext(), R.string.show_info_toast_leaving, Toast.LENGTH_SHORT).show();
                stopService(new Intent(getBaseContext(), LocationTransmitterService.class));
                prefs.edit().putBoolean(keyId + "-started", false).commit();
            }
            updateAttendButtonTitle();
        }
    }

    @Override
    public void taskDidProgress(ExtendedTask task, Void... progress) {

    }

    @Override
    public void taskFailed(ExtendedTask task, String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
