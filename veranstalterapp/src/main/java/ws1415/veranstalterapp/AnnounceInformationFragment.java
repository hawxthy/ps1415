package ws1415.veranstalterapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
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
import com.appspot.skatenight_ms.skatenightAPI.model.Event;
import com.appspot.skatenight_ms.skatenightAPI.model.Text;

/**
 * Klasse zum veröffentlichen von neuen Veranstaltungen.
 *
 * Created by Bernd Eissing, Marting Wrodarczyk on 21.10.2014.
 */
public class AnnounceInformationFragment extends Fragment {
    // Die Viewelemente für das Event
    private EditText editTextTitle;
    private EditText editTextFee;
    private EditText editTextLocation;
    private EditText editTextDescription;

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

    static final int DATE_DIALOG_ID = 1;
    static final int TIME_DIALOG_ID = 2;

    // Attribute für die Zeit
    private int hour;
    private int minute;

    // das neu erstellte Event
    private Event event;

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

        // Setze die Listener für die Buttons
        setButtonListener();


        // Setze die aktuelle Zeit und das Datum
        setCurrentDateOnView();
        return view;
    }

    private void setButtonListener() {
        // Setze die Listener für Cancel und Apply Buttons
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelInfo();
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
    }

    /**
     * Erstellt je nach id ein DatePicker- oder TimePicker Dialog.
     * @param id DatePickerDialog falls id = 1, TimePickerDialog falls id = 2
     * @return Dialog Fenster
     */
    protected Dialog showDialog(int id){
        switch(id){
            case DATE_DIALOG_ID:
                // setze das Datum des Pickers als das angegebene Datum für das Event
                return new DatePickerDialog(getActivity(), datePickerListener,  year, month, day);
            case TIME_DIALOG_ID:
                // setze die Zeit des Picker als angegebene Zeit für das Event
                return new TimePickerDialog(getActivity(), timePickerListener, hour, minute, true);
        }
        return null;
    }

    /**
     * Setzt das angegebene Datum bei einer Änderung als Text auf den datePickerButton.
     */
    private OnDateSetListener datePickerListener = new OnDateSetListener(){

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay){
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            // set selected date into Button
            datePickerButton.setText(day +"."+ (month+1) +"."+year);
        }
    };

    /**
     * Setzt das aktuelle Datum als Text auf den datePickerButton.
     */
    public void setCurrentDateOnView(){
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // set selected date into Button
        datePickerButton.setText(day +"."+ (month+1) +"."+year);
    }

    /**
     * Setzt einen Listener, welcher die ausgewählte Uhrzeit bei einer Änderung setzt und auf
     * den timePickerButton setzt.
     */
    private OnTimeSetListener timePickerListener = new OnTimeSetListener(){

        @Override
        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
            hour = selectedHour;
            minute = selectedMinute;
            if(minute < 10){
                //minute = 0+ selectedMinute;
                timePickerButton.setText(hour +":0"+minute+" Uhr");
            }else{
                timePickerButton.setText(hour +":"+minute+" Uhr");
            }
        }
    };


    /**
     * Liest die eingegebenen Informationen aus, erstellt ause diesen ein Event und fügt dieses
     * Event dem Server hinzu. Momentan wir noch das alte Event überschrieben.
     */
    public void applyInfo(){

        // Zeige einen Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.areyousure);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Weise die Werte aus den Feldern Variablen zu, um damit dann das Event zu setzen.
                String title = editTextTitle.getText().toString();
                String fee = editTextFee.getText().toString();
                String date = day+ "." +month+ "." +year+ " " +hour+ ":" +minute+ " Uhr";
                String location =editTextLocation.getText().toString();
                Text description = new Text();
                description.setValue(editTextDescription.getText().toString());

                // Erstelle ein neue Event
                event = new Event();

                // Setze die Attribute vom Event
                event.setTitle(title);
                event.setFee(fee);
                event.setDate(date);
                event.setLocation(location);
                event.setDescription(description);
                new CreateEventTask().execute(event);
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.eventcreated), Toast.LENGTH_LONG).show();

                // Update die Informationen in ShowInformationFragment
                HoldTabsActivity.updateInformation();
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
     * und löscht alle Eingeben in den Text Feldern.
     */
    public void cancelInfo(){
        Toast.makeText(getActivity(), "Wurde noch nicht erstellt", Toast.LENGTH_LONG).show();
        editTextTitle.setText("");
        editTextFee.setText("");
        editTextLocation.setText("");
        editTextDescription.setText("");
        setCurrentDateOnView();
        timePickerButton.setText(getResources().getString(R.string.announce_info_set_time));
    }
}