package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.UserListAdapter;
import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.UniversalUtil;

/**
 * Diese Activity dient dazu, die Freunde des eingeloggten Benutzers anzuzeigen.
 *
 * @author Martin Wrodarczyk
 */
public class FriendsActivity extends BaseActivity {
    private ListView mListViewFriends;
    private UserListAdapter mAdapter;
    private LinearLayout mLayoutHintFriends;
    private Button mButtonHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_friends);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        mLayoutHintFriends = (LinearLayout) findViewById(R.id.friends_hint_message);
        mButtonHint = (Button) findViewById(R.id.friends_hint_button);
        mButtonHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent search_intent = new Intent(FriendsActivity.this, SearchActivity.class);
                FriendsActivity.this.startActivity(search_intent);
            }
        });

        mListViewFriends = (ListView) findViewById(R.id.friends_list_view);
        mAdapter = new UserListAdapter(new ArrayList<String>(), FriendsActivity.this);
        mListViewFriends.setAdapter(mAdapter);

        mListViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String userMail = mAdapter.getItem(i).getEmail();
                Intent profile_intent = new Intent(FriendsActivity.this, ProfileActivity.class);
                profile_intent.putExtra("email", userMail);
                startActivity(profile_intent);
            }
        });
        mListViewFriends.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                createDeleteDialog(i);
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ServiceProvider.getEmail() != null) getFriendList();
    }

    /**
     * Erstellt einen Dialog, der abfragt ob der Freund gelöscht werden soll
     *
     * @param position Position des Freundes in der Liste
     */
    private void createDeleteDialog(final int position) {
        new AlertDialog.Builder(FriendsActivity.this)
                .setTitle(mAdapter.getItem(position).getFirstName())
                .setMessage(getString(R.string.sure_delete_friend_dialog))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        removeFriend(position);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.ic_action_discard)
                .show();
    }

    /**
     * Löscht den Freund an der übergebenen Position der Liste.
     *
     * @param position Position des Freundes in der Liste
     */
    private void removeFriend(final int position) {
        UserController.removeFriend(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                if (aBoolean) {
                    Toast.makeText(FriendsActivity.this, getString(R.string.friend_delete_succeeded), Toast.LENGTH_LONG).show();
                    mAdapter.removeUser(position);
                    if (mAdapter.getMailCount() == 0)
                        mLayoutHintFriends.setVisibility(View.VISIBLE);
                    else mLayoutHintFriends.setVisibility(View.GONE);
                } else {
                    Toast.makeText(FriendsActivity.this, getString(R.string.friend_does_not_exist), Toast.LENGTH_LONG).show();
                    getFriendList();
                }
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(FriendsActivity.this, message, Toast.LENGTH_LONG).show();
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
        }, ServiceProvider.getEmail(), mAdapter.getItem(position).getEmail());
    }

    /**
     * Ruft die Freundesliste ab und setzt den Adapter mit den abgerufenen Daten.
     */
    private void getFriendList() {
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        UserController.listFriends(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> friendList) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                if (mListViewFriends != null  && mAdapter != null) {
                    mAdapter.swapData(friendList);
                    if (mAdapter.getMailCount() == 0) mLayoutHintFriends.setVisibility(View.VISIBLE);
                    else mLayoutHintFriends.setVisibility(View.GONE);
                }
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(FriendsActivity.this, message, Toast.LENGTH_LONG).show();
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
        }, ServiceProvider.getEmail());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh_friends:
                getFriendList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
