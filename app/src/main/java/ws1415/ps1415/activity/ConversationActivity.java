package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.MessageAdapter;
import ws1415.ps1415.controller.MessageDbController;

/**
 * Diese Activity dient zur Anzeige einer Konversation und Erstellen von neuen Nachrichten in
 * dieser Konversation.
 *
 * @author Martin Wrodarczyk
 */
public class ConversationActivity extends Activity {
    private ListView mListViewMessages;
    private MessageAdapter mAdapter;
    private String mEmail;
    private String mFirstName;
    private String mLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");
        mFirstName = intent.getStringExtra("firstName");
        mLastName = intent.getStringExtra("lastName");

        if(mFirstName != null & mLastName != null){
            setTitle(mFirstName + " " + mLastName);
        }

        mListViewMessages = (ListView) findViewById(R.id.conversation_list_view);
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(mEmail != null) {
            mAdapter = new MessageAdapter(MessageDbController.getInstance(this).getAllMessages(mEmail), this);
            mListViewMessages.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
