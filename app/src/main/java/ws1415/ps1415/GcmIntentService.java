package ws1415.ps1415;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Date;

import ws1415.common.gcm.MessageType;
import ws1415.ps1415.activity.ShowEventsActivity;
import ws1415.ps1415.activity.UsergroupActivity;
import ws1415.ps1415.util.PrefManager;

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
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
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

                        // Falls sich die Startzeit geändert hat, dann Alarm aktualisieren
                        boolean dateChanged = Boolean.parseBoolean(extras.getString("date_changed"));
                        if (dateChanged) {
                            long newDate = Long.parseLong(extras.getString("new_date"));
                            long eventId = Long.parseLong(extras.getString("event_id"));
                            LocationTransmitterService.ScheduleService(this, eventId, new Date(newDate));
                        }
                        break;
                    case EVENT_START_MESSAGE:
                        break;
                    case GROUP_DELETED_NOTIFICATION_MESSAGE:
                        sendNotification(extras);
                        // Einstellung für die Gruppe entfernen, falls vorhanden
                        PrefManager.deleteGroupVisibility(this, extras.getString("content"));
                        break;
                    case GROUP_CREATED_NOTIFICATION_MESSAGE:
                        // Gruppen-Listen in den Fragmenten von UserGroupActivity aktualisieren
                        Intent refreshGroupsIntent = new Intent(UsergroupActivity.REFRESH_GROUPS_ACTION);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(refreshGroupsIntent);
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
