package ws1415.ps1415;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.UserPrimaryData;

import java.util.Date;

import de.greenrobot.event.EventBus;
import ws1415.ps1415.activity.GroupProfileActivity;
import ws1415.ps1415.controller.TransferController;
import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.gcm.MessageType;
import ws1415.ps1415.model.Conversation;
import ws1415.ps1415.model.LocalMessageType;
import ws1415.ps1415.model.Message;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.activity.ConversationActivity;
import ws1415.ps1415.activity.ListEventsActivity;
import ws1415.ps1415.activity.UsergroupActivity;
import ws1415.ps1415.controller.MessageDbController;
import ws1415.ps1415.event.NewMessageEvent;
import ws1415.ps1415.util.PrefManager;
import ws1415.ps1415.util.UniversalUtil;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    private Context mContext;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = this;
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
                } catch (Exception e) {
                    type = null;
                }
                switch (type) {
                    case NOTIFICATION_MESSAGE:
                        sendNotificationMessaging(extras, new Intent(this, ListEventsActivity.class));
                        break;
                    case EVENT_NOTIFICATION_MESSAGE:
                        sendNotificationMessaging(extras, new Intent(this, ListEventsActivity.class));
                        // Event-Liste in der ShowEventsActivity aktualisieren
                        Intent refreshIntent = new Intent(ListEventsActivity.REFRESH_EVENTS_ACTION);
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
                        // TODO R: Entfernen
                        break;
                    case GROUP_DELETED_NOTIFICATION_MESSAGE:
                        sendNotificationMessaging(extras, null);
                        // Einstellung für die Gruppe entfernen, falls vorhanden
                        PrefManager.deleteGroupVisibility(this, extras.getString("content"));
                        break;
                    case GROUP_CREATED_NOTIFICATION_MESSAGE:
                        // Gruppen-Listen in den Fragmenten von UserGroupActivity aktualisieren
                        Intent refreshGroupsIntent = new Intent(UsergroupActivity.REFRESH_GROUPS_ACTION);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(refreshGroupsIntent);
                        break;
                    case USER_NEW_MESSAGE:
                        String receiver = extras.getString("receiver");
                        if (PrefManager.getSelectedUserMail(this).equals(receiver)) {
                            Long messageId = Long.parseLong(extras.getString("messageId"));
                            Long sendDate = Long.parseLong(extras.getString("sendDate"));
                            String content = extras.getString("content");
                            String sender = extras.getString("sender");
                            UniversalUtil.simpleLogin(mContext);
                            handleNewMessage(receiver, sender, sendDate, content);
                            if (ServiceProvider.getEmail() != null)
                                TransferController.sendConfirmation(null, sender, messageId, sendDate);
                        }
                        break;
                    case USER_CONFIRMATION_MESSAGE:
                        String receiverConf = extras.getString("receiver");
                        if (PrefManager.getSelectedUserMail(this).equals(receiverConf)) {
                            Long messageId = Long.parseLong(extras.getString("messageId"));
                            Long sendDate = Long.parseLong(extras.getString("sendDate"));
                            String sender = extras.getString("sender");
                            updateMessageReceived(receiverConf, messageId, sendDate, sender);
                        }
                        break;
                    case GLOBAL_GROUP_MESSAGE:
                        sendNotificationMessaging(extras, null);
                        break;
                    case INVITATION_TO_GROUP_MESSAGE:
                        //TODO Button zum joinen hinzufügen
                        Intent group_intent = new Intent(this, GroupProfileActivity.class);
                        group_intent.putExtra("groupName", extras.getString("title"));
                        sendNotificationMessaging(extras, group_intent);
                        break;
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Setzt mit den Informationen der Bestätigung die Nachricht auf "erhalten".
    private void updateMessageReceived(String receiver, Long messageId, Long sendDate, String sender) {
        boolean succeed = MessageDbController.getInstance(this, receiver).
                updateMessageReceived(messageId, sendDate);
        if (succeed) EventBus.getDefault().post(new NewMessageEvent(sender));
    }

    /**
     * Verarbeitet den Erhalt einer neuen Nachricht. Dabei wird geprüft, ob bereits eine
     * Konversation zu dem Sender der Nachricht existiert. Existiert keine, so wird eine
     * neue Konversation erstellt. Abschließend wird die neue Nachricht hinzugefügt.
     */
    private void handleNewMessage(final String receiver, final String sender, Long sendDate, final String content) {
        if (!MessageDbController.getInstance(this, receiver).existsConversation(sender)) {
            if (ServiceProvider.getEmail() != null) {
                handleNewConversation(receiver, sender, content, sendDate);
                return;
            }
        }
        createNewMessage(receiver, sender, sendDate, content);
    }

    /**
     * Fügt die empfangene Nachricht hinzu.
     */
    private void createNewMessage(String receiver, String sender, Long sendDate, String content) {
        Message message = new Message(new Date(sendDate), content, LocalMessageType.INCOMING);
        long id = MessageDbController.getInstance(this, receiver).insertMessage(sender, message);
        if(id >= 0){
            Conversation con = MessageDbController.getInstance(this, receiver).getConversation(sender);
            sendNotificationMessaging(sender, content, con.getFirstName(), con.getLastName());
            EventBus.getDefault().post(new NewMessageEvent(sender));
        }
    }

    /**
     * Lädt Informationen zu dem Sender der Nachricht vom Server und erstellt mit Hilfe dieser
     * eine neue Konversation. Im Falle, dass die Informationen nicht abgerufen werden können,
     * wird eine Konversation mit der E-Mail Adresse als Namen erstellt.
     */
    private void handleNewConversation(final String receiver, final String sender, final String content,
                                       final Long sendDate) {
        UserController.getPrimaryData(new ExtendedTaskDelegateAdapter<Void, UserPrimaryData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserPrimaryData userPrimaryData) {
                BlobKey picture = userPrimaryData.getPicture();
                String pictureKey = (picture == null) ? null : picture.getKeyString();
                String firstName = (userPrimaryData.getFirstName() == null) ? "" : userPrimaryData.getFirstName();
                String lastName = (userPrimaryData.getLastName() == null) ? "" : userPrimaryData.getLastName();
                Conversation conversation = new Conversation(sender, pictureKey, firstName, lastName);
                MessageDbController.getInstance(mContext, receiver).insertConversation(conversation);
                createNewMessage(receiver, sender, sendDate, content);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Conversation conversation = new Conversation(sender, null, getString(R.string.unknown_user), "");
                MessageDbController.getInstance(mContext, receiver).insertConversation(conversation);
                createNewMessage(receiver, sender, sendDate, content);
            }
        }, sender);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotificationMessaging(Bundle extras, Intent intent) {
        String title = extras.getString("title");
        String msg = extras.getString("content");
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        if (intent != null) {
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
            mBuilder.setContentIntent(contentIntent);
        }

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void sendNotificationMessaging(String sender, String content, String senderFirstName,
                                           String senderLastName) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra("email", sender);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(senderFirstName + " " + senderLastName)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(content))
                        .setContentText(content);

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);

        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;

        mNotificationManager.notify(NOTIFICATION_ID, note);
    }
}
