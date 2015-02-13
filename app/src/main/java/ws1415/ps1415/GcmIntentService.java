package ws1415.ps1415;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.model.LatLng;
import com.skatenight.skatenightAPI.model.Event;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ws1415.common.gcm.MessageType;
import ws1415.common.util.LocationUtils;
import ws1415.ps1415.activity.ShowEventsActivity;
import ws1415.ps1415.task.GetEventTask;
import ws1415.ps1415.util.EventUtils;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                // TODO
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                // TODO
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                MessageType type;
                try {
                    type = MessageType.valueOf(extras.getString("type"));
                } catch(IllegalArgumentException e) {
                    type = null;
                }
                switch (type) {
                    case NOTIFICATION_MESSAGE:
                        sendNotification(extras);
                        break;
                    case EVENT_NOTIFICATION_MESSAGE:
                        sendNotification(extras);
                        // Event-Liste in der ShowEventsActivity aktualisieren
                        Intent refreshIntent = new Intent(ShowEventsActivity.REFRESH_EVENTS_ACTION);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(refreshIntent);
                        break;
                    case EVENT_START_MESSAGE:
                        // LocationTransmitterService wird gestartet, wenn das Event gestartet wird.
                        long eventId = Long.parseLong(extras.getString("eventId"));
                        try {
                            Event e = new GetEventTask(null).execute(eventId).get();
                            Date startDate = EventUtils.getInstance(this).getFusedDate(e);
                            List<LatLng> waypoints = LocationUtils.decodePolyline(e.getRoute().getRouteData().getValue());
                            Intent serviceIntent = new Intent(getBaseContext(), LocationTransmitterService.class);
                            serviceIntent.putExtra(LocationTransmitterService.EXTRA_EVENT_ID, eventId);
                            serviceIntent.putParcelableArrayListExtra(LocationTransmitterService.EXTRA_WAYPOINTS, new ArrayList(waypoints));
                            serviceIntent.putExtra(LocationTransmitterService.EXTRA_START_DATE, startDate.getTime());
                            startService(serviceIntent);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Bundle extras) {
        String title = extras.getString("title");
        String msg = extras.getString("content");
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ShowEventsActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
