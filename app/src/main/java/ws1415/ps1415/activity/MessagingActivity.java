package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.MessagingAdapter;
import ws1415.ps1415.controller.MessageDbController;
import ws1415.ps1415.model.Conversation;

/**
 * Diese Activity dient der Anzeige aller Konversationen des eingeloggten Benutzers.
 *
 * @author Martin Wrodarczyk
 */
public class MessagingActivity extends BaseActivity {
    private ListView mListViewMessaging;
    private MessagingAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        mListViewMessaging = (ListView) findViewById(R.id.messaging_list_view);
        mListViewMessaging.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Conversation item = mAdapter.getItem(i);
                showConversationMenuDialog(item);
                return true;
            }
        });
    }

    /**
     * Ruft alle Konversationen aus der Datenbank ab und übergibt diese dem Adapter.
     */
    @Override
    protected void onStart() {
        super.onStart();
        List<Conversation> conversations = MessageDbController.getInstance(this).getAllConversations();
        mAdapter = new MessagingAdapter(conversations, this);
        mListViewMessaging.setAdapter(mAdapter);
    }

    /**
     * Zeigt das Auswahlmenü zu einer Konversation.
     *
     * @param item Konversation
     */
    private void showConversationMenuDialog(final Conversation item) {
        new AlertDialog.Builder(MessagingActivity.this)
                .setTitle(item.getFirstName() + " " + item.getLastName())
                .setItems(R.array.conversation_menu_array, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent profile_intent = new Intent(MessagingActivity.this, ProfileActivity.class);
                                profile_intent.putExtra("email", item.getEmail());
                                startActivity(profile_intent);
                                break;
                            case 1:
                                showDeleteDialog(item);
                                break;
                        }
                    }
                })
                .show();
    }

    /**
     * Zeigt einen Dialog zum Löschvorgang einer Konversation
     *
     * @param item Konversation
     */
    private void showDeleteDialog(final Conversation item) {
        new AlertDialog.Builder(MessagingActivity.this)
                .setTitle(item.getFirstName() + " " + item.getLastName())
                .setMessage(getString(R.string.sure_delete_conversation_dialog))
                .setPositiveButton(android.R.string.yes, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean succeeded = MessageDbController.getInstance(MessagingActivity.this).
                                deleteConversation(item.getEmail());
                        if (succeeded) {
                            Toast.makeText(MessagingActivity.this,
                                    getString(R.string.conversation_deleted), Toast.LENGTH_LONG).show();
                            mAdapter.removeItem(item);
                        }
                        else Toast.makeText(MessagingActivity.this,
                                getString(R.string.error_delete_conversation), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.no, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.ic_action_discard)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_messaging, menu);
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
