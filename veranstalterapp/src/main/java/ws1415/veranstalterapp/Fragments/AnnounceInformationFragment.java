package ws1415.veranstalterapp.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import com.appspot.skatenight_ms.skatenightAPI.model.Event;
import com.appspot.skatenight_ms.skatenightAPI.model.Route;
import com.appspot.skatenight_ms.skatenightAPI.model.Text;
import com.google.api.client.util.DateTime;

import ws1415.veranstalterapp.Activities.ChooseRouteActivity;
import ws1415.veranstalterapp.Activities.HoldTabsActivity;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.task.CreateEventTask;

/**
 * Fragment zum veröffentlichen von neuen Veranstaltungen.
 * <p/>
 * Created by Bernd Eissing, Marting Wrodarczyk on 21.10.2014.
 */
public class AnnounceInformationFragment extends Fragment {
    // Die Viewelemente für das Event
    private EditText editTextTitle;
    private EditText editTextFee;
    private EditText editTextLocation;
    private EditText editTextDescription;

    // Der Button für die Route
    private Button routePickerButton;

    // Die Buttons für Zeit und Datum
    private Button datePickerButton;
    private Button timePickerButton;

    // Die Buttons für Cancel und Apply
    private Button applyButton;
    private Button cancelButton;

    // Attribute für das Datum
    private int year;
    private int month;
    private int day;

    // Attribute für die Eingabe felder
    private String title;
    private String fee;
    private String location;
    private Text description;

    private Calendar cal;

    static final int DATE_DIALOG_ID = 1;
    static final int TIME_DIALOG_ID = 2;

    // Attribute für die Zeit
    private int hour;
    private int minute;

    // Das Attribut Route
    private Route route;

    // das neu erstellte Event
    private Event event;

    /**
     * Erstellt die View, initialisiert die Attribute, setzt die Listener für die Buttons.
     *
     * @param inflater           Übersetzt die xml Datei in View Elemente
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announce_information, container, false);

        // Initialisiere die View Elemente
        editTextTitle = (EditText) view.findViewById(R.id.announce_info_title_edittext);
        editTextFee = (EditText) view.findViewById(R.id.announce_info_fee_edittext);
        editTextLocation = (EditText) view.findViewById(R.id.announce_info_location_edittext);
        editTextDescription = (EditText) view.findViewById(R.id.announce_info_description_edittext);

        // Initialisiere die Buttons
        timePickerButton = (Button) view.findViewById(R.id.announce_info_time_button);
        datePickerButton = (Button) view.findViewById(R.id.announce_info_date_button);
        applyButton = (Button) view.findViewById(R.id.announce_info_apply_button);
        cancelButton = (Button) view.findViewById(R.id.announce_info_cancel_button);
        routePickerButton = (Button) view.findViewById(R.id.announce_info_choose_route);

        // Setze die Listener für die Buttons
        setButtonListener();


        // Setze die aktuelle Zeit und das Datum
        setCurrentDateOnView();
        return view;
    }

    /**
     * Methode zum setzen der Listener für die Buttons "timePickerButton", "datePickerButton",
     * "applyButton", "cancelButton", "routePickerButton".
     */
    private void setButtonListener() {
        // Setze die Listener für Cancel und Apply Buttons
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelInfo(true);
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyInfo();
            }
        });

        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * Die onClick Methode welche aufgerufen wird, wenn der datePickerButton gedrückt wird.
                 */
                showDialog(TIME_DIALOG_ID).show();
            }
        });

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * Die onClick Methode welche aufgerufen wird, wenn der datePickerButton gedrückt wird.
                 */
                showDialog(DATE_DIALOG_ID).show();
            }
        });

        routePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChooseRouteActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Erstellt je nach id ein DatePicker- oder TimePicker Dialog.
     *
     * @param id DatePickerDialog falls id = 1, TimePickerDialog falls id = 2
     * @return Dialog Fenster
     */
    protected Dialog showDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                // setze das Datum des Pickers als das angegebene Datum für das Event
                return new DatePickerDialog(getActivity(), datePickerListener, year, month, day);
            case TIME_DIALOG_ID:
                // setze die Zeit des Picker als angegebene Zeit für das Event
                return new TimePickerDialog(getActivity(), timePickerListener, hour, minute, true);
        }
        return null;
    }

    /**
     * Setzt das angegebene Datum bei einer Änderung als Text auf den datePickerButton.
     */
    private OnDateSetListener datePickerListener = new OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            // set selected date into Button
            datePickerButton.setText(day + "." + (month + 1) + "." + year);
        }
    };

    /**
     * Setzt das aktuelle Datum als Text auf den datePickerButton.
     */
    public void setCurrentDateOnView() {
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // set selected date into Button
        datePickerButton.setText(day + "." + (month + 1) + "." + year);
    }

    /**
     * Setzt einen Listener, welcher die ausgewählte Uhrzeit bei einer Änderung setzt und auf
     * den timePickerButton setzt.
     */
    private OnTimeSetListener timePickerListener = new OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
            hour = selectedHour;
            minute = selectedMinute;
            if (minute < 10) {
                //minute = 0+ selectedMinute;
                timePickerButton.setText(hour + ":0" + minute + " Uhr");
            } else {
                timePickerButton.setText(hour + ":" + minute + " Uhr");
            }
        }
    };


    /**
     * Liest die eingegebenen Informationen aus, erstellt ause diesen ein Event und fügt dieses
     * Event dem Server hinzu. Momentan wir noch das alte Event überschrieben.
     */
    public void applyInfo() {

        // Zeige einen Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.areyousure);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // Weise die Werte aus den Feldern Variablen zu, um damit dann das Event zu setzen.
                title = editTextTitle.getText().toString();
                fee = editTextFee.getText().toString();
                // Erstelle einen Calendar zum Speichern des Datums und der Uhrzeit
                cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                cal.set(Calendar.HOUR, hour);
                cal.set(Calendar.MINUTE, minute);

                // Weise die Daten aus Calendar dem Datentyp Date zu
                Date date = cal.getTime();
                // Mache aus dem Date ein DateTime, da dies von ServerBackend benötigt wird.
                DateTime dTime = new DateTime(date);


                location = editTextLocation.getText().toString();
                description = new Text();
                description.setValue(editTextDescription.getText().toString());
                // Überprüfen ob wirklich alle daten des Events gesetzt sind
                if (!title.isEmpty() && !fee.isEmpty() && dTime != null && !location.isEmpty() && !description.isEmpty() && route != null) {
                    // Erstelle ein neue Event
                    event = new Event();

                    // Setze die Attribute vom Event
                    event.setTitle(title);
                    event.setFee(fee);
                    event.setDate(dTime);
                    event.setLocation(location);
                    event.setRoute(route);
                    event.setDescription(description);
                    new CreateEventTask().execute(event);
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.eventcreated), Toast.LENGTH_LONG).show();
                    editTextTitle.setText("");
                    editTextFee.setText("");
                    timePickerButton.setText(getString(R.string.announce_info_set_time));
                    editTextLocation.setText("");
                    routePickerButton.setText(getString(R.string.announce_info_choose_map));
                    editTextDescription.setText("");
                    setCurrentDateOnView();

                    // Update die Informationen in ShowInformationFragment
                    HoldTabsActivity.updateInformation();
                } else {
                    cancelInfo(false);
                }

            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Gibt momentan einen Toast aus, wenn der Benutzer auf den Cancel Button drückt
     * (löscht alle Eingaben) oder wenn der Benutze ein unvollständiges Event hinzufügen will
     */
    public void cancelInfo(boolean allSet) {
        if (allSet) {
            Toast.makeText(getActivity(), "Wurde noch nicht erstellt", Toast.LENGTH_LONG).show();
            editTextTitle.setText("");
            editTextFee.setText("");
            editTextLocation.setText("");
            routePickerButton.setText(getString(R.string.announce_info_choose_map));
            editTextDescription.setText("");
            setCurrentDateOnView();
        } else {
            Toast.makeText(getActivity(), "Nicht alle Felder ausgefüllt", Toast.LENGTH_LONG).show();
        }

        timePickerButton.setText(getResources().getString(R.string.announce_info_set_time));
    }

    /**
     * Dies Methode setzt die Route für das Event
     *
     * @param selectedRoute Die Route für das Event
     */
    public void setRoute(Route selectedRoute) {
        route = selectedRoute;
        routePickerButton.setText(selectedRoute.getName());
    }
}
