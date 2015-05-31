package ws1415.ps1415.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.EventData;

import java.util.Date;

import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;
import ws1415.ps1415.R;
import ws1415.ps1415.adapter.DynamicFieldsAdapter;
import ws1415.ps1415.util.DiskCacheImageLoader;

public class ShowEventActivity extends Activity implements ExtendedTaskDelegate<Void,EventData> {
    public static final String EXTRA_EVENT_ID = ShowEventActivity.class.getName() + ".EventId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);

        Long eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
        EventController.getEvent(this, eventId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // -------------------- Callback-Methoden für das Abrufen eines Events --------------------
    @Override
    public void taskDidFinish(ExtendedTask task, EventData eventData) {
        setTitle(eventData.getTitle());

        ImageView headerImage = (ImageView) findViewById(R.id.headerImage);
        DiskCacheImageLoader.getInstance().loadImage(headerImage, eventData.getHeaderImage());
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(eventData.getTitle());
        TextView date = (TextView) findViewById(R.id.date);
        Date tmpDate = new Date(eventData.getDate().getValue());
        date.setText(DateFormat.getMediumDateFormat(this).format(tmpDate) + " " + DateFormat.getTimeFormat(this).format(tmpDate));
        TextView description = (TextView) findViewById(R.id.description);
        description.setText(eventData.getDescription());
        ListView dynamicFields = (ListView) findViewById(R.id.dynamicFields);
        dynamicFields.setAdapter(new DynamicFieldsAdapter(eventData.getDynamicFields()));
        HorizontalScrollView images = (HorizontalScrollView) findViewById(R.id.images);

        findViewById(R.id.eventLoading).setVisibility(View.GONE);
    }

    @Override
    public void taskDidProgress(ExtendedTask task, Void... progress) { }

    @Override
    public void taskFailed(ExtendedTask task, String message) {
        Toast.makeText(this, R.string.event_loading_error, Toast.LENGTH_LONG).show();
        findViewById(R.id.eventLoading).setVisibility(View.GONE);
    }
    // -------------------- Callback-Methoden für das Abrufen eines Events --------------------
}
