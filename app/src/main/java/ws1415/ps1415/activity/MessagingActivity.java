package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.UserPrimaryData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;
import ws1415.common.controller.UserController;
import ws1415.common.model.Conversation;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.R;
import ws1415.ps1415.adapter.MessagingAdapter;
import ws1415.ps1415.controller.MessageDbController;
import ws1415.ps1415.event.NewMessageEvent;
import ws1415.ps1415.util.UniversalUtil;

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
        // Prüft ob der Benutzer eingeloggt ist
        UniversalUtil.checkLogin(this);

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_messaging);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        mListViewMessaging = (ListView) findViewById(R.id.messaging_list_view);
        mAdapter = new MessagingAdapter(new ArrayList<Conversation>(), this);
        mListViewMessaging.setAdapter(mAdapter);
        mListViewMessaging.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Conversation conversation = mAdapter.getItem(i);
                Intent conversation_intent = new Intent(MessagingActivity.this, ConversationActivity.class);
                conversation_intent.putExtra("email", conversation.getEmail());
                conversation_intent.putExtra("firstName", conversation.getFirstName());
                conversation_intent.putExtra("lastName", conversation.getLastName());
                conversation_intent.putExtra("newMessagesCount", conversation.getCountNewMessages());
                startActivity(conversation_intent);
            }
        });
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
    private void setUpData() {
        List<Conversation> conversations = MessageDbController.getInstance(this).getAllConversations();
        conversations = sortConversation(conversations);
        mAdapter.setUpData(conversations);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        setUpData();
    }

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
    public void onEvent(NewMessageEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setUpData();
            }
        });
    }

    private List<Conversation> sortConversation(List<Conversation> conversations) {
        Collections.sort(conversations, new Comparator<Conversation>() {
                    public int compare(Conversation c1, Conversation c2) {
                        if(c2.getLastMessage() == null && c1.getLastMessage() == null) return 0;
                        if(c2.getLastMessage() == null) return -1;
                        if(c1.getLastMessage() == null) return 1;
                        return c2.getLastMessage().getSendDate().compareTo(c1.getLastMessage().getSendDate());
                    }
                }
        );
        return conversations;
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
                                setProgressBarIndeterminateVisibility(Boolean.TRUE);
                                updateProfileData(item);
                                setUpData();
                                break;
                            case 2:
                                showDeleteDialog(item);
                                break;
                        }
                    }
                })
                .show();
    }

    /**
     * Aktualisiert die Profildaten eines Benutzers.
     * @param item
     */
    private void updateProfileData(Conversation item) {
        UserController.getPrimaryData(new ExtendedTaskDelegateAdapter<Void, UserPrimaryData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserPrimaryData userPrimaryData) {
                if (userPrimaryData != null) {
                    String pictureKey = (userPrimaryData.getPicture() == null) ? "" :
                            userPrimaryData.getPicture().getKeyString();
                    String firstName = (userPrimaryData.getFirstName() == null) ? "" :
                            userPrimaryData.getFirstName();
                    String lastName = (userPrimaryData.getLastName() == null) ? "" :
                            userPrimaryData.getLastName();
                    boolean succeed = MessageDbController.getInstance(MessagingActivity.this).updateConversation(userPrimaryData.getEmail(),
                            pictureKey, firstName, lastName);
                    if(succeed) {
                        UniversalUtil.showToast(MessagingActivity.this, getString(R.string.profile_updated));
                        setUpData();
                    } else {
                        UniversalUtil.showToast(MessagingActivity.this, getString(R.string.error_update));
                    }
                } else {
                    UniversalUtil.showToast(MessagingActivity.this, getString(R.string.error_update));
                }
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                UniversalUtil.showToast(MessagingActivity.this, message);
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
        }, item.getEmail());
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
                        } else Toast.makeText(MessagingActivity.this,
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
