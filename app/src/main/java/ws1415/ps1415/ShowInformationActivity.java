package ws1415.ps1415;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.appspot.skatenight_ms.skatenightAPI.model.Event;


public class ShowInformationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showinformation);

        new QueryEventTask().execute(this);
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
        TextView dateView = (TextView) findViewById(R.id.show_info_date_textview);
        TextView locationView = (TextView) findViewById(R.id.show_info_location_textview);
        TextView feeView = (TextView) findViewById(R.id.show_info_fee_textview);
        TextView descriptionView = (TextView) findViewById(R.id.show_info_description_textview);
        if (e != null) {
            setTitle(e.getTitle());
            dateView.setText(e.getDate());
            locationView.setText(e.getLocation());
            feeView.setText(e.getFee());
            descriptionView.setText(e.getDescription().getValue());
        } else {
            setTitle("leer");
            dateView.setText("leer");
            locationView.setText("leer");
            feeView.setText("leer");
            descriptionView.setText("leer");
        }
    }
}
