package ws1415.ps1415.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.ps1415.Constants;
import ws1415.ps1415.R;
import ws1415.ps1415.task.GetEventTask;
import ws1415.ps1415.task.ToggleMemberEventAttendanceTask;

/**
 * Zeigt die Informationen des aktuellen Events an.
 */
public class ShowInformationActivity extends Activity implements ExtendedTaskDelegate<Void, Object> {
    public static final String EXTRA_KEY_ID = "show_information_extra_key_id";

    private static final String MEMBER_KEY_ID = "show_information_member_key_id";
    private static final String MEMBER_TITLE = "show_infomation_member_title";
    private static final String MEMBER_DATE = "show_infomation_member_date";
    private static final String MEMBER_LOCATION = "show_infomation_member_location";
    private static final String MEMBER_FEE = "show_infomation_member_fee";
    private static final String MEMBER_DESCRIPTION = "show_infomation_member_description";
    private static final String MEMBER_ROUTE = "show_infomation_member_route";
    private static final String MEMBER_ROUTE_BUTTON_TITLE = "show_information_member_route_button_title";
    private static final String MEMBER_ROUTE_FIELD_FIRST = "show_information_member_route_field_first";
    private static final String MEMBER_ROUTE_FIELD_LAST = "show_information_member_route_field_last";
    private static final String MEMBER_ATTENDING = "show_information_member_attending";

    static final int REQUEST_ACCOUNT_PICKER = 2;
    private GoogleAccountCredential credential;
    private SharedPreferences prefs;

    private long keyId;
    private String title;
    private String date;
    private String location;
    private String fee;
    private String description;
    private String route;
    private String routeButtonTitle;
    private int routeFieldFirst;
    private int routeFieldLast;
    private boolean attending;

    // Erstellen eines SimpleDateFormats, damit das Datum und die Uhrzeit richtig angezeigt werden
    private SimpleDateFormat dateFormat;

    /**
     *
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showinformation);

        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        // SharePreferences skatenight.app laden
        prefs = this.getSharedPreferences("skatenight.app", Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingAudience(this,"server:client_id:"+ Constants.WEB_CLIENT_ID);

        // accountName aus SharedPreferences laden
        if (prefs.contains("accountName")) {
            credential.setSelectedAccountName(prefs.getString("accountName", null));
        }

        // Kein accountName gesetzt, also AccountPicker aufrufen
        if (credential.getSelectedAccountName() == null) {
            startActivityForResult(credential.newChooseAccountIntent(),REQUEST_ACCOUNT_PICKER);
        }

        Intent intent;
        if (savedInstanceState != null) {
            keyId = savedInstanceState.getLong(MEMBER_KEY_ID);
            title = savedInstanceState.getString(MEMBER_TITLE);
            date = savedInstanceState.getString(MEMBER_DATE);
            location = savedInstanceState.getString(MEMBER_LOCATION);
            fee = savedInstanceState.getString(MEMBER_FEE);
            description = savedInstanceState.getString(MEMBER_DESCRIPTION);
            route = savedInstanceState.getString(MEMBER_ROUTE);
            routeButtonTitle = savedInstanceState.getString(MEMBER_ROUTE_BUTTON_TITLE);
            routeFieldFirst = savedInstanceState.getInt(MEMBER_ROUTE_FIELD_FIRST);
            routeFieldLast = savedInstanceState.getInt(MEMBER_ROUTE_FIELD_LAST);
            attending = savedInstanceState.getBoolean(MEMBER_ATTENDING);
            updateGUI();
        }
        else if ((intent = getIntent()) != null) {
            keyId = intent.getLongExtra(EXTRA_KEY_ID, 0);
            new GetEventTask(this).execute(keyId);
        }

        Button mapButton = (Button) findViewById(R.id.show_info_map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (route != null) {
                    // Erstellt den Dialog, ob die Position gespeichert werden soll und auf der Karte angezeigt wird
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowInformationActivity.this);
                    builder.setMessage("Deine Position wird gespeichert & auf der Karte angezeigt.");
                    builder.setPositiveButton(Html.fromHtml("<font color='#1FB1FF'>Ok</font>"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Leite wieter auf die Karte
                            Intent intent = new Intent(ShowInformationActivity.this, ShowRouteActivity.class);
                            intent.putExtra(ShowRouteActivity.EXTRA_ROUTE, route);
                            intent.putExtra(ShowRouteActivity.EXTRA_ROUTE_FIELD_FIRST, routeFieldFirst);
                            intent.putExtra(ShowRouteActivity.EXTRA_ROUTE_FIELD_LAST, routeFieldLast);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton(Html.fromHtml("<font color='#1FB1FF'>Abbrechen</font>"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Tue nichts
                        }
                    });

                    // Zeigt den Dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        Button attendButton = (Button) findViewById(R.id.show_info_attend_button);
        attendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ToggleMemberEventAttendanceTask(ShowInformationActivity.this, keyId, credential.getSelectedAccountName(), attending).execute();
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
                    }
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(MEMBER_KEY_ID, keyId);
        outState.putString(MEMBER_TITLE, title);
        outState.putString(MEMBER_DATE, date);
        outState.putString(MEMBER_LOCATION, location);
        outState.putString(MEMBER_FEE, fee);
        outState.putString(MEMBER_DESCRIPTION, description);
        outState.putString(MEMBER_ROUTE, route);
        outState.putString(MEMBER_ROUTE_BUTTON_TITLE, routeButtonTitle);
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Übernimmt die Informationen aus dem übergebenen Event-Objekt in die GUI-Elemente.
     * @param e Das neue Event-Objekt.
     */
    public void setEventInformation(Event e) {
        if (e != null) {
            title =  e.getTitle();
            if (e.getDate() != null) {
                date = dateFormat.format(new Date(e.getDate().getValue()));
            } else {
                date = "";
            }
            location = e.getLocation();
            if (e.getFee() != null) {
                fee = e.getFee().toString()+" €";
            } else {
                fee = "";
            }
            if (e.getDescription() != null) {
                description = e.getDescription().getValue();
            } else {
                description = "";
            }
            if (e.getRoute() != null && e.getRoute().getRouteData() != null) {
                route = e.getRoute().getRouteData().getValue();
                routeButtonTitle = e.getRoute().getName() + " (" + e.getRoute().getLength() + ")";
            }
            if (e.getRouteFieldFirst() != null) {
                routeFieldFirst = e.getRouteFieldFirst();
            }
            if (e.getRouteFieldLast() != null) {
                routeFieldLast = e.getRouteFieldLast();
            }

            String email = credential.getSelectedAccountName();
            if (e.getMemberList() != null) {
                attending = e.getMemberList().contains(email);
            }

            // TODO: Member nur hinzufügen wenn er auch wirklich teilnehmen möchte


            // EmailAdresse des aktuellen users dem Event hinzufügen
            //new AddMemberToEventTask(null, e, email).execute();
        } else {
            title = null;
            date = null;
            location = null;
            fee = null;
            description = null;
            route = null;
            routeButtonTitle = null;
            routeFieldFirst = 0;
            routeFieldLast = 0;
        }
        updateGUI();
    }

    /**
     * Überträgt die in den Variablen gespeicherten Informationen auf das GUI.
     */
    private void updateGUI() {
        TextView dateView = (TextView) findViewById(R.id.show_info_date_textview);
        TextView locationView = (TextView) findViewById(R.id.show_info_location_textview);
        TextView feeView = (TextView) findViewById(R.id.show_info_fee_textview);
        TextView descriptionView = (TextView) findViewById(R.id.show_info_description_textview);
        Button mapButton = (Button) findViewById(R.id.show_info_map_button);
        Button attendButton = (Button) findViewById(R.id.show_info_attend_button);

        if (title != null &&
                date != null &&
                location != null &&
                fee != null &&
                description != null &&
                routeButtonTitle != null) {
            setTitle(title);
            dateView.setText(date);
            locationView.setText(location);
            feeView.setText(fee);
            descriptionView.setText(description);
            mapButton.setText(routeButtonTitle);
            mapButton.setEnabled(true);
            attendButton.setEnabled(true);
            updateAttendButtonTitle();
        }
        else {
            setTitle("leer");
            dateView.setText("leer");
            locationView.setText("leer");
            feeView.setText("leer");
            descriptionView.setText("leer");
            mapButton.setEnabled(false);
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
