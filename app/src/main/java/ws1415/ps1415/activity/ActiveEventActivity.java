package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.EventData;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.common.task.GetEventTask;
import ws1415.ps1415.util.LocalAnalysisData;
import ws1415.ps1415.util.LocalStorageUtil;

public class ActiveEventActivity extends Activity implements ExtendedTaskDelegate<Void, EventData> {
    public static final String LOG_TAG = ActiveEventActivity.class.getSimpleName();
    public static final String EXTRA_KEY_ID = "active_event_extra_key_id";

    private static final String MEMBER_KEY_ID = "active_event_member_key_id";
    private static final String MEMBER_START_DATE = "active_event_member_start_date";
    private static final String MEMBER_ENCODED_WAYPOINTS = "active_event_member_encoded_waypoints";

    private LocationReceiver receiver;

    private TextView timerTextView;
    private Date startDate;
    private Timer clockTimer;

    private long eventId;
    private boolean saved;

    private String encodedWaypoints;

    private TimerTask clockTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_active_event);
        setProgressBarIndeterminateVisibility(true);
        ((ScrollView) findViewById(R.id.active_event_scroll_view)).setVisibility(View.INVISIBLE);
        if (savedInstanceState != null) {
            startDate = new Date(savedInstanceState.getLong(MEMBER_START_DATE));
            eventId = savedInstanceState.getLong(MEMBER_KEY_ID);
            encodedWaypoints = savedInstanceState.getString(MEMBER_ENCODED_WAYPOINTS);
        }
        else {
            startDate = new Date();

            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(EXTRA_KEY_ID)) {
                eventId = intent.getLongExtra(EXTRA_KEY_ID, 0L);
            }
            else {
                Log.e(LOG_TAG, "EventId is required.");
                finish();
            }

            encodedWaypoints = "";
        }

        timerTextView = (TextView) findViewById(R.id.active_event_timer_textview);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.contains(String.valueOf(eventId))) {
            saved = true;

            restoreLocalData(String.valueOf(eventId));

            Button endParticipationButton = (Button) findViewById(R.id.active_event_cancel_button);
            ((ViewGroup) endParticipationButton.getParent()).removeView(endParticipationButton);
        }
        else {
            saved = false;

            clockTimer = new Timer(true);

            new GetEventTask(this).execute(eventId);

            Button endParticipationButton = (Button) findViewById(R.id.active_event_cancel_button);
            endParticipationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopService(new Intent(getBaseContext(), LocationTransmitterService.class));
                    stopClockTimer();

                    saved = true;

                    Button button = (Button) v;
                    button.setEnabled(false);
                }
            });
        }

        Button speedProfileButton = (Button) findViewById(R.id.active_event_speed_profile_button);
        speedProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActiveEventActivity.this, ShowRouteActivity.class);
                intent.putExtra(ShowRouteActivity.EXTRA_ROUTE, encodedWaypoints);
                if (saved) {
                    LocalStorageUtil storeLocalData = new LocalStorageUtil(getApplicationContext());
                    LocalAnalysisData localData = storeLocalData.getData(String.valueOf(eventId));
                    intent.putExtra(ShowRouteActivity.EXTRA_VISITED, localData.getVisited());
                    intent.putExtra(ShowRouteActivity.EXTRA_TIMESTAMPS, localData.getTimestamps());
                }
                else {
                    intent.putExtra(ShowRouteActivity.EXTRA_EVENT_ID, eventId);
                    intent.putExtra(ShowRouteActivity.EXTRA_SPEED_PROFILE, true);
                }

                startActivity(intent);
            }
        });

        Button mapButton = (Button) findViewById(R.id.active_event_map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActiveEventActivity.this, ShowRouteActivity.class);
                intent.putExtra(ShowRouteActivity.EXTRA_USERGROUPS, true);
                intent.putExtra(ShowRouteActivity.EXTRA_ROUTE, encodedWaypoints);
                intent.putExtra(ShowRouteActivity.EXTRA_EVENT_ID, eventId);
                startActivity(intent);
            }
        });

        receiver = new LocationReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(MEMBER_START_DATE, startDate.getTime());
        outState.putLong(MEMBER_KEY_ID, eventId);
        outState.putString(MEMBER_ENCODED_WAYPOINTS, encodedWaypoints);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!saved) {
            startClockTimer();
        }
    }

    @Override
    protected void onPause() {
        if (!saved) {
            stopClockTimer();
        }
        super.onPause();
    }

    private void startClockTimer() {
        if (clockTimerTask == null) {
            clockTimerTask = new ClockTimerTask();
            clockTimer.scheduleAtFixedRate(clockTimerTask, 0, 1000);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(LocationTransmitterService.NOTIFICATION_LOCATION));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(LocationTransmitterService.NOTIFICATION_CANCEL));
    }

    private void stopClockTimer() {
        if (clockTimerTask != null) {
            clockTimerTask.cancel();
            clockTimerTask = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
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
            Intent intent = new Intent(ActiveEventActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTimerTextView(Date endDate) {
        long current = endDate.getTime()-startDate.getTime();
        current /= 1000;

        String sign = (current < 0) ? "-":"";

        long s = Math.abs(current%60);
        current /= 60;
        long m = Math.abs(current%60);
        current /= 60;
        long h = Math.abs(current);
        timerTextView.setText(getString(R.string.active_event_timer_format, sign, h, m, s));
    }

    private boolean isEventNull(EventData e) {
        if (e == null) {
            return true;
        }
        else if (e.getDate() == null) {
            return true;
        }
        else if (e.getRoute().getRouteData() == null) {
            return true;
        }

        return false;
    }

    @Override
    public void taskDidFinish(ExtendedTask task, EventData event) {
        setProgressBarIndeterminateVisibility(false);
        if (!isEventNull(event)) {
            startDate = new Date(event.getDate().getValue());

            encodedWaypoints = event.getRoute().getRouteData().getValue();

            updateTimerTextView(new Date());

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
                    updateTimerTextView(new Date());
                }
            });
        }
    }

    private void restoreLocalData (String id) {
        LocalAnalysisData localData = new LocalStorageUtil(getApplicationContext()).getData(id);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.active_event_progressbar);
        progressBar.setProgress((int)(localData.getProgress()*100.0f));
        progressBar.setMax(100);

        float currentDistance = localData.getCurrentDistance();
        TextView currentDistanceTextView = (TextView) findViewById(R.id.active_event_current_distance_textview);
        currentDistanceTextView.setText(getString(R.string.active_event_distance_format_meters, currentDistance));

        TextView distanceTextView = (TextView) findViewById(R.id.active_event_total_distance_textview);
        distanceTextView.setText(localData.getDistance());

        float curSpeed = 0.0f;
        TextView curSpeedTextView = (TextView) findViewById(R.id.active_event_cur_speed_textview);
        curSpeedTextView.setText(getString(R.string.active_event_speed_format, curSpeed));

        float maxSpeed = localData.getMaxSpeed();
        TextView maxSpeedTextView = (TextView) findViewById(R.id.active_event_max_speed_textview);
        maxSpeedTextView.setText(getString(R.string.active_event_speed_format, maxSpeed));

        float avgSpeed = localData.getAvgSpeed();
        TextView avgSpeedTextView = (TextView) findViewById(R.id.active_event_avg_speed_textview);
        avgSpeedTextView.setText(getString(R.string.active_event_speed_format, avgSpeed));

        float elevationGain = localData.getElevationGain();
        TextView elevationGainTextView = (TextView) findViewById(R.id.active_event_elevation_gain_textview);
        elevationGainTextView.setText(getString(R.string.active_event_altitude_format_meters, elevationGain));

        startDate = localData.getStartDate();
        updateTimerTextView(localData.getEndDate());

        encodedWaypoints = localData.getWaypoints();

        setProgressBarIndeterminateVisibility(false);
        ((ScrollView) findViewById(R.id.active_event_scroll_view)).setVisibility(View.VISIBLE);

    }

    /**
     * Private Klasse zum empfangen von location updates vom LocationTransmitterTask
     * (benötigt weil BroadcastReceiver Klasse und nicht Interface).
     */
    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(LocationTransmitterService.NOTIFICATION_LOCATION)) {
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
            else if (intent.getAction().equals(LocationTransmitterService.NOTIFICATION_CANCEL)) {
                stopClockTimer();

                saved = true;

                Button button = (Button) findViewById(R.id.active_event_cancel_button);
                button.setEnabled(false);
            }
        }
    }
}
