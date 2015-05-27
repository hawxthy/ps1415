package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import java.util.Date;

import ws1415.common.controller.MessageController;
import ws1415.common.model.LocalMessageType;
import ws1415.common.model.Message;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.R;
import ws1415.ps1415.adapter.MessageAdapter;
import ws1415.ps1415.controller.MessageDbController;
import ws1415.ps1415.util.UniversalUtil;

/**
 * Diese Activity dient zur Anzeige einer Konversation und Erstellen von neuen Nachrichten in
 * dieser Konversation.
 *
 * @author Martin Wrodarczyk
 */
public class ConversationActivity extends Activity {
    // UI-Komponenten
    private ListView mListViewMessages;
    private EditText mEditTextInput;
    private ImageView mImageViewSend;

    // Adapter für den Inhalt der ListView
    private MessageAdapter mAdapter;

    // Daten vom Intent
    private String mEmail;
    private String mFirstName;
    private String mLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");
        mFirstName = intent.getStringExtra("firstName");
        mLastName = intent.getStringExtra("lastName");

        if (mFirstName != null & mLastName != null) {
            setTitle(mFirstName + " " + mLastName);
        }

        mListViewMessages = (ListView) findViewById(R.id.conversation_list_view);
        mEditTextInput = (EditText) findViewById(R.id.conversation_input);
        mImageViewSend = (ImageView) findViewById(R.id.conversation_send_image);
        mListViewMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        mImageViewSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = mEditTextInput.getText().toString();
                if (input.equals(""))
                    Toast.makeText(ConversationActivity.this, getString(R.string.input_required),
                            Toast.LENGTH_LONG).show();
                else {
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    final Message message = new Message(new Date(), input, LocalMessageType.OUTGOING_NOT_RECEIVED);
                    long id = MessageDbController.getInstance(ConversationActivity.this).insertMessage(mEmail, message);
                    message.set_id(id);
                    sendMessage(message);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mEmail != null) {
            mAdapter = new MessageAdapter(MessageDbController.getInstance(this).getAllMessages(mEmail), this);
            mListViewMessages.setAdapter(mAdapter);
            mListViewMessages.setSelection(mAdapter.getCount() - 1);
        }
    }

    /**
     * Sendet die Nachricht an den Empfänger.
     *
     * @param localMessage Nachricht
     */
    private void sendMessage(final Message localMessage) {
        MessageController.sendMessage(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                if (aBoolean) {
                    mAdapter.addMessage(localMessage);
                    mEditTextInput.getText().clear();
                } else {
                    UniversalUtil.showToast(ConversationActivity.this, getString(R.string.message_could_not_be_sent));
                    MessageDbController.getInstance(ConversationActivity.this).deleteMessage(localMessage.get_id());
                }
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                UniversalUtil.showToast(ConversationActivity.this, message);
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                MessageDbController.getInstance(ConversationActivity.this).deleteMessage(localMessage.get_id());
            }
        }, mEmail, mFirstName + " " + mLastName, localMessage);
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
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
