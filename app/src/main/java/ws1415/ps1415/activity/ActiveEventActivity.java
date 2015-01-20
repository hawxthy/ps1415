package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.skatenight.skatenightAPI.model.Event;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.util.LocationUtils;
import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.ps1415.task.QueryCurrentMemberEventTask;
import ws1415.ps1415.util.EventUtils;

public class ActiveEventActivity extends Activity implements ExtendedTaskDelegate<Void, Event> {
    private LocationReceiver receiver;

    private TextView timerTextView;
    private Date startDate;
    private Timer clockTimer;

    private long eventId;
    private String email;
    private String encodedWaypoints;
    private List<LatLng> waypoints;

    private TimerTask clockTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_active_event);
        setProgressBarIndeterminateVisibility(true);
        ((ScrollView) findViewById(R.id.active_event_scroll_view)).setVisibility(View.INVISIBLE);
        if (savedInstanceState != null) {
            startDate = new Date(savedInstanceState.getLong("date"));
        }
        else {
            startDate = new Date();
        }

        SharedPreferences prefs = this.getSharedPreferences("skatenight.app", Context.MODE_PRIVATE);
        String email = prefs.getString("accountName", null);
        if (email == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.active_event_no_email_message), Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            new QueryCurrentMemberEventTask(this, email).execute();
        }

        timerTextView = (TextView) findViewById(R.id.active_event_timer_textview);
        clockTimer = new Timer(true);

        Button speedProfileButton = (Button) findViewById(R.id.active_event_speed_profile_button);
        speedProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActiveEventActivity.this, ShowRouteActivity.class);
                intent.putExtra(ShowRouteActivity.EXTRA_ROUTE, encodedWaypoints);
                intent.putExtra(ShowRouteActivity.EXTRA_EVENT_ID, eventId);
                startActivity(intent);
            }
        });

        receiver = new LocationReceiver();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(getBaseContext(), LocationTransmitterService.class));
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("date", startDate.getTime());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (clockTimerTask == null) {
            clockTimerTask = new ClockTimerTask();
            clockTimer.scheduleAtFixedRate(clockTimerTask, 0, 1000);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(LocationTransmitterService.NOTIFICATION_LOCATION));
    }

    @Override
    protected void onPause() {
        if (clockTimerTask != null) {
            clockTimerTask.cancel();
            clockTimerTask = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_active_event, menu);
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

    private void updateTimerTextView() {
        long current = new Date().getTime()-startDate.getTime();
        current /= 1000;

        String sign = (current < 0) ? "-":"";

        long s = Math.abs(current%60);
        current /= 60;
        long m = Math.abs(current%60);
        current /= 60;
        long h = Math.abs(current);
        timerTextView.setText(getString(R.string.active_event_timer_format, sign, h, m, s));
    }

    private boolean isEventNull(Event e) {
        if (e == null) {
            return true;
        }
        else if (EventUtils.getInstance(this).getUniqueField(3, e) == null) {
            return true;
        }
        else if (e.getRoute().getRouteData() == null) {
            return true;
        }

        return false;
    }

    @Override
    public void taskDidFinish(ExtendedTask task, Event event) {
        setProgressBarIndeterminateVisibility(false);
        if (!isEventNull(event)) {
            eventId = event.getKey().getId();
            startDate = EventUtils.getInstance(this).getFusedDate(event);

            encodedWaypoints = event.getRoute().getRouteData().getValue();
            try {
                waypoints = LocationUtils.decodePolyline(event.getRoute().getRouteData().getValue());
            }
            catch (ParseException e) {
                Toast.makeText(getApplicationContext(), "Parse failed", Toast.LENGTH_SHORT).show();
            }

            Intent serviceIntent = new Intent(getBaseContext(), LocationTransmitterService.class);
            serviceIntent.putExtra(LocationTransmitterService.EXTRA_EVENT_ID, eventId);
            serviceIntent.putParcelableArrayListExtra(LocationTransmitterService.EXTRA_WAYPOINTS, new ArrayList(waypoints));
            serviceIntent.putExtra(LocationTransmitterService.EXTRA_START_DATE, startDate.getTime());
            startService(serviceIntent);
            updateTimerTextView();

            TextView distanceTextView = (TextView) findViewById(R.id.active_event_total_distance_textview);
            distanceTextView.setText(event.getRoute().getLength());

            ((ScrollView) findViewById(R.id.active_event_scroll_view)).setVisibility(View.VISIBLE);
        }
        else {
            Toast.makeText(getApplicationContext(), "Invalid event!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void taskDidProgress(ExtendedTask task, Void... progress) {

    }

    @Override
    public void taskFailed(ExtendedTask task, String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        setProgressBarIndeterminateVisibility(false);
    }

    private class ClockTimerTask extends TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTimerTextView();
                }
            });
        }
    }

    /**
     * Private Klasse zum empfangen von location updates vom LocationTransmitterTask
     * (benötigt weil BroadcastReceiver Klasse und nicht Interface).
     */
    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView altitudeTextView = (TextView) findViewById(R.id.active_event_current_altitude_textview);

            Location location = (Location) intent.getParcelableExtra(LocationTransmitterService.NOTIFICATION_EXTRA_LOCATION);
            if (location.hasAltitude()) {
                altitudeTextView.setText(getString(R.string.active_event_altitude_format_meters, location.getAltitude()));
            }
            int currentWaypoint = intent.getIntExtra(LocationTransmitterService.NOTIFICATION_EXTRA_CURRENT_WAYPOINT, 0);
            int waypointCount = intent.getIntExtra(LocationTransmitterService.NOTIFICATION_EXTRA_WAYPOINT_COUNT, 0);

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.active_event_progressbar);
            progressBar.setProgress(currentWaypoint);
            progressBar.setMax(waypointCount);

            //Toast.makeText(getApplicationContext(), currentWaypoint + "/" + waypointCount, Toast.LENGTH_SHORT).show();

            float currentDistance = intent.getFloatExtra(LocationTransmitterService.NOTIFICATION_EXTRA_CURRENT_DISTANCE, 0.0f);
            TextView currentDistanceTextView = (TextView) findViewById(R.id.active_event_current_distance_textview);
            currentDistanceTextView.setText(getString(R.string.active_event_distance_format_meters, currentDistance));

            float curSpeed = intent.getFloatExtra(LocationTransmitterService.NOTIFICATION_EXTRA_CUR_SPEED, 0.0f);
            TextView curSpeedTextView = (TextView) findViewById(R.id.active_event_cur_speed_textview);
            curSpeedTextView.setText(getString(R.string.active_event_speed_format, curSpeed));

            float maxSpeed = intent.getFloatExtra(LocationTransmitterService.NOTIFICATION_EXTRA_MAX_SPEED, 0.0f);
            TextView maxSpeedTextView = (TextView) findViewById(R.id.active_event_max_speed_textview);
            maxSpeedTextView.setText(getString(R.string.active_event_speed_format, maxSpeed));

            float avgSpeed = intent.getFloatExtra(LocationTransmitterService.NOTIFICATION_EXTRA_AVG_SPEED, 0.0f);
            TextView avgSpeedTextView = (TextView) findViewById(R.id.active_event_avg_speed_textview);
            avgSpeedTextView.setText(getString(R.string.active_event_speed_format, avgSpeed));

            float elevationGain = intent.getFloatExtra(LocationTransmitterService.NOTIFICATION_EXTRA_ELEVATION_GAIN, 0.0f);
            TextView elevationGainTextView = (TextView) findViewById(R.id.active_event_elevation_gain_textview);
            elevationGainTextView.setText(getString(R.string.active_event_altitude_format_meters, elevationGain));
        }
    }
}
