package ws1415.veranstalterapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import com.google.appengine.api.datastore.Text;
import java.util.Calendar;


public class AnnounceInformationActivity extends Activity{
    // Die Viewelemente für das Event
    private EditText editTextTitle;
    private EditText editTextFee;
    private EditText editTextLocation;
    private EditText editTextDescription;

    // Die Buttons für Zeit und Datum
    private Button datePickerButton;
    private Button timePickerButton;

    // Attribute für das Datum
    private int year;
    private int month;
    private int day;

    static final int DATE_DIALOG_ID = 1;
    static final int TIME_DIALOG_ID = 2;

    // Attribute für die Zeit
    private int hour;
    private int minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announce_information);

        // Initialisiere die View Elemente


        // Initialisiere die Buttons
        timePickerButton = (Button) findViewById(R.id.announce_info_time_button);
        datePickerButton = (Button) findViewById(R.id.announce_info_date_button);

        // Setze die aktuelle Zeit und das Datum
        setCurrentDateOnView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.announce_information, menu);
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

    public void showDatePickerDialog(View view){
        showDialog(DATE_DIALOG_ID);
    }
    public void showTimePickerDialog(View view){
        showDialog(TIME_DIALOG_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id){
        switch(id){
            case DATE_DIALOG_ID:
                // setze das Datum des Pickers als das angegebene Datum für das Event
                return new DatePickerDialog(this, datePickerListener,  year, month, day);
            case TIME_DIALOG_ID:
                // setze die Zeit des Picker als angegebene Zeit für das Event
                return new TimePickerDialog(this, timePickerListener, hour, minute, true);
        }
        return null;
    }

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

    // disaplay current date
    public void setCurrentDateOnView(){
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // set selected date into Button
        datePickerButton.setText(day +"."+ (month+1) +"."+year);
    }

    private OnTimeSetListener timePickerListener = new OnTimeSetListener(){

        @Override
        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
            hour = selectedHour;
            minute = selectedMinute;
            if(minute < 10){
                timePickerButton.setText(hour +":"+0+minute+" Uhr");
            }else{
                timePickerButton.setText(hour +":"+minute+" Uhr");
            }
        }
    };

    /**
     * Hier soll was passieren wenn der Versanstalter den Prozess abbricht!
     */
    public void cancelInfo(){
       // TODO Muss noch implementiert werden
    }

    public void applyInfo(){

    }

}
