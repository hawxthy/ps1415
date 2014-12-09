package ws1415.ps1415.Activities;



import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.Event;

import ws1415.ps1415.adapter.ShowCursorAdapter;
import ws1415.ps1415.R;
import ws1415.ps1415.task.GetEventTask;

/**
 * Activity zum Begutachten der Metainformationen der erstellten Veranstaltung.
 *
 * Created by Bernd Eissing, Marting Wrodarczyk on 21.10.2014.
 */
public class ShowInformationActivity extends Activity {
    // Adapter für die ListView von activity_show_information_list_view
    private ShowCursorAdapter listAdapter;

    // Die ListView von der xml datei activity_show_information
    private ListView listView;
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

        long id = getIntent().getLongExtra("event", 0);
        new GetEventTask(this).execute(id);
    }

    /**
     * Übernimmt die Informationen aus dem übergebenen Event-Objekt in die GUI-Elemente.
     *
     * @param e Das neue Event-Objekt.
     */
    public void setEventInformation(Event e) {
        if (e != null) {
            listAdapter = new ShowCursorAdapter(this, e.getDynamicFields(), e);

            listView = (ListView) findViewById(R.id.activity_show_information_list_view);
            listView.setAdapter(listAdapter);
        }
    }
}
