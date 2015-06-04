package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

import de.greenrobot.event.EventBus;
import ws1415.ps1415.controller.TransferController;
import ws1415.ps1415.model.Conversation;
import ws1415.ps1415.model.LocalMessageType;
import ws1415.ps1415.model.Message;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.R;
import ws1415.ps1415.adapter.MessageAdapter;
import ws1415.ps1415.controller.MessageDbController;
import ws1415.ps1415.event.NewMessageEvent;
import ws1415.ps1415.util.UniversalUtil;

/**
 * Diese Activity dient zur Anzeige einer Konversation und Erstellen von neuen Nachrichten in
 * dieser Konversation.
 *
 * @author Martin Wrodarczyk
 */
public class ConversationActivity extends Activity {
    // Für das Wiederherstellen einer Activity
    private static final String STATE_EMAIL = "email";

    // UI-Komponenten
    private ListView mListViewMessages;
    private EditText mEditTextInput;
    private ImageView mImageViewSend;

    // Adapter für den Inhalt der ListView
    private MessageAdapter mAdapter;

    // Daten vom Intent
    private String mEmail;
    private Conversation mConversation;

    // Feld um den Sendevorgang zu prüfen
    private boolean sending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_conversation);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        // Im Falle einer Wiederherstellung der Activity die Email abrufen
        if (savedInstanceState != null) mEmail = savedInstanceState.getString(STATE_EMAIL);

        // Email vom Intent speichern
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        if (email != null) mEmail = email;

        if (!MessageDbController.getInstance(this).existsConversation(mEmail)) {
            Intent messaging_intent = new Intent(ConversationActivity.this, MessagingActivity.class);
            startActivity(messaging_intent);
            finish();
            return;
        }

        // Conversation abrufen
        mConversation = MessageDbController.getInstance(this).getConversation(mEmail);
        String firstName = mConversation.getFirstName();
        String lastName = mConversation.getLastName();

        // Titel setzen
        if (firstName != null & lastName != null) setTitle(firstName + " " + lastName);

        // Anzahl neuer Nachrichten auf 0 setzen, falls diese != 0 sind
        if (mConversation.getCountNewMessages() != 0)
            MessageDbController.getInstance(this).resetNewMessages(mEmail);

        // UI Komponenten initialisieren
        mEditTextInput = (EditText) findViewById(R.id.conversation_input);
        mImageViewSend = (ImageView) findViewById(R.id.conversation_send_image);

        // Liste initialisieren
        mListViewMessages = (ListView) findViewById(R.id.conversation_list_view);
        mListViewMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        mAdapter = new MessageAdapter(new ArrayList<Message>(), this);
        mListViewMessages.setAdapter(mAdapter);

        // Listener für das Senden einer Nachricht
        mImageViewSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sending) {
                    String input = mEditTextInput.getText().toString();
                    if (input.equals("")) {
                        UniversalUtil.showToast(ConversationActivity.this, getString(R.string.input_required));
                    } else {
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        sending = true;
                        final Message message = new Message(new Date(), input, LocalMessageType.OUTGOING_NOT_RECEIVED);
                        long id = MessageDbController.getInstance(ConversationActivity.this).insertMessage(mEmail, message);
                        message.set_id(id);
                        sendMessage(message);
                    }
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_EMAIL, mEmail);
        super.onSaveInstanceState(savedInstanceState);
    }

    // Ruft die Nachrichten ab und setzt die Daten im Adapter
    private void setUpData() {
        mAdapter.setUpData(MessageDbController.getInstance(this).getAllMessages(mEmail));
        mListViewMessages.setSelection(mAdapter.getCount() - 1);
    }

    /**
     * Registriert EventBus und ruft Nachrichten ab
     */
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (mEmail != null) {
            setUpData();
        }
    }

    /**
     * Abmelden des EventBus.
     */
    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Wird aufgerufen wenn eine neue Nachricht auf dem Gerät ankommt.
     *
     * @param event Event mit der Email als Information
     */
    public void onEventMainThread(NewMessageEvent event) {
        if (mEmail != null) {
            setUpData();
            MessageDbController.getInstance(ConversationActivity.this).resetNewMessages(mEmail);
        }
    }

    /**
     * Sendet die Nachricht an den Empfänger.
     *
     * @param localMessage Nachricht
     */
    private void sendMessage(final Message localMessage) {
        TransferController.sendMessage(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                if (aBoolean) {
                    if((mAdapter.getCount()-1 >= 0) && mAdapter.getItem(mAdapter.getCount()-1) != localMessage)
                        mAdapter.addMessage(localMessage);
                    mEditTextInput.getText().clear();
                } else {
                    UniversalUtil.showToast(ConversationActivity.this, getString(R.string.message_could_not_be_sent));
                    MessageDbController.getInstance(ConversationActivity.this).deleteMessage(mEmail, localMessage.get_id());
                }
                sending = false;
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                UniversalUtil.showToast(ConversationActivity.this, message);
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                sending = false;
                MessageDbController.getInstance(ConversationActivity.this).deleteMessage(mEmail, localMessage.get_id());
            }
        }, mEmail, localMessage);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
