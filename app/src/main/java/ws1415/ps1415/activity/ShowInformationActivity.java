package ws1415.ps1415.activity;


import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.util.EventUtil;
import ws1415.common.util.LocationUtils;
import ws1415.ps1415.Constants;
import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.ShowCursorAdapter;
import ws1415.ps1415.task.GetEventTask;
import ws1415.ps1415.task.ToggleMemberEventAttendanceTask;
import ws1415.ps1415.util.EventUtils;
import ws1415.ps1415.util.FieldType;

/**
 * Activity zum Begutachten der Metainformationen der erstellten Veranstaltung.
 * <p/>
 * Created by Bernd Eissing, Marting Wrodarczyk on 21.10.2014.
 */
public class ShowInformationActivity extends Activity implements ExtendedTaskDelegate<Void, Object> {
    public static final int REQUEST_ACCOUNT_PICKER = 2;

    public static final String EXTRA_KEY_ID = "show_information_extra_key_id";

    private static final String MEMBER_KEY_ID = "show_information_member_key_id";
    private static final String MEMBER_ROUTE_FIELD_FIRST = "show_information_member_route_field_first";
    private static final String MEMBER_ROUTE_FIELD_LAST = "show_information_member_route_field_last";
    private static final String MEMBER_ATTENDING = "show_information_member_attending";

    // Adapter für die ListView von activity_show_information_list_view
    private ShowCursorAdapter listAdapter;

    // Die ListView von der xml datei activity_show_information
    private ListView listView;
    private long keyId;
    private int routeFieldFirst;
    private int routeFieldLast;
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
        if (savedInstanceState != null) {
            keyId = savedInstanceState.getLong(MEMBER_KEY_ID);
            routeFieldFirst = savedInstanceState.getInt(MEMBER_ROUTE_FIELD_FIRST);
            routeFieldLast = savedInstanceState.getInt(MEMBER_ROUTE_FIELD_LAST);
            attending = savedInstanceState.getBoolean(MEMBER_ATTENDING);
        }
        else if ((intent = getIntent()) != null) {
            keyId = intent.getLongExtra(EXTRA_KEY_ID, 0);
            new GetEventTask(this).execute(keyId);
        }

        Button attendButton = (Button) findViewById(R.id.show_info_attend_button);
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
                    new ToggleMemberEventAttendanceTask(ShowInformationActivity.this, keyId, credential.getSelectedAccountName(), attending).execute();

                    // LocaitonTransmitterService starten, wenn das Event noch aktiv ist
                    if (active) {
                        try {
                            List<LatLng> waypoints = LocationUtils.decodePolyline(event.getRoute().getRouteData().getValue());
                            Intent serviceIntent = new Intent(getBaseContext(), LocationTransmitterService.class);
                            serviceIntent.putExtra(LocationTransmitterService.EXTRA_EVENT_ID, keyId);
                            serviceIntent.putParcelableArrayListExtra(LocationTransmitterService.EXTRA_WAYPOINTS, new ArrayList(waypoints));
                            serviceIntent.putExtra(LocationTransmitterService.EXTRA_START_DATE, startDate.getTime());
                            startService(serviceIntent);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
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

                        new ToggleMemberEventAttendanceTask(this, keyId, credential.getSelectedAccountName(), attending).execute();
                    }
                }
                break;
        }
    }
    
    @Override
     protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(MEMBER_KEY_ID, keyId);
        outState.putInt(MEMBER_ROUTE_FIELD_FIRST, routeFieldFirst);
        outState.putInt(MEMBER_ROUTE_FIELD_LAST, routeFieldLast);
        outState.putBoolean(MEMBER_ATTENDING, attending);

        super.onSaveInstanceState(outState);
    }
    
    @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.example, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(ShowInformationActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            setTitle(EventUtils.getInstance(this).getUniqueField(FieldType.TITLE.getId(), e).getValue());
            listAdapter = new ShowCursorAdapter(this, e.getDynamicFields(), e);

            listView = (ListView) findViewById(R.id.activity_show_information_list_view);
            listView.setAdapter(listAdapter);

            // Prüft, ob das aktuelle Datum nach dem Start-Datum des Events liegt.
            // Also ob es bereits gestartet ist. Somit wird der Server gestartet.
            startDate = EventUtils.getInstance(this.getBaseContext()).getFusedDate(e);
            Date today = new Date();
            if (today.after(startDate)) {
                active = true;
            } else {
                active = false;
            }

            if (e.getRouteFieldFirst() != null) {
                routeFieldFirst = e.getRouteFieldFirst();
            }
            if (e.getRouteFieldLast() != null) {
                routeFieldLast = e.getRouteFieldLast();
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
            if (attending) Toast.makeText(getApplicationContext(), getString(R.string.show_info_toast_attending), Toast.LENGTH_SHORT).show();
            else Toast.makeText(getApplicationContext(), R.string.show_info_toast_leaving, Toast.LENGTH_SHORT).show();
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
