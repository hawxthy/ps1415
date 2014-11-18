package ws1415.veranstalterapp.Fragments;



import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.appspot.skatenight_ms.skatenightAPI.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;

import ws1415.veranstalterapp.Activities.HoldTabsActivity;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.task.GetEventTask;

/**
 * Activity zum Begutachten der Metainformationen der erstellten Veranstaltung.
 *
 * Created by Bernd Eissing, Marting Wrodarczyk on 21.10.2014.
 */
public class ShowInformationActivity extends Activity {
    private ActionBar actionBar;
    private SimpleDateFormat dateFormat;

    /**
     * Erstellt die View und zeigt die Informationen in der ShowInformationActivity.
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_information);
        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        actionBar = this.getActionBar();
        long id = getIntent().getLongExtra("event", 0);
        new GetEventTask(this).execute(id);
    }

    /**
     * Übernimmt die Informationen aus dem übergebenen Event-Objekt in die GUI-Elemente.
     *
     * @param e Das neue Event-Objekt.
     */
    public void setEventInformation(Event e) {
        TextView dateView = (TextView) findViewById(R.id.show_info_date_textview);
        TextView locationView = (TextView) findViewById(R.id.show_info_location_textview);
        TextView routeView = (TextView) findViewById(R.id.show_info_route_textview);
        TextView feeView = (TextView) findViewById(R.id.show_info_fee_textview);
        TextView descriptionView = (TextView) findViewById(R.id.show_info_description_textview);
        if (e != null) {
            this.setTitle(e.getTitle());
            dateView.setText(dateFormat.format(new Date(e.getDate().getValue())));
            locationView.setText(e.getLocation());
            routeView.setText(e.getRoute().getName() + " (" + e.getRoute().getLength() + ")");
            feeView.setText(e.getFee()+" €");
            if (e.getDescription() != null) {
                descriptionView.setText(e.getDescription().getValue());
            }
        } else {
            dateView.setText(getString(R.string.show_info_empty_text));
            locationView.setText(getString(R.string.show_info_empty_text));
            routeView.setText(getString(R.string.show_info_empty_text));
            feeView.setText(getString(R.string.show_info_empty_text));
            descriptionView.setText(getString(R.string.show_info_empty_text));
        }
    }


}
