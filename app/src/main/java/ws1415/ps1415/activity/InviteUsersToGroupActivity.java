package ws1415.ps1415.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.GroupMemberListAdapter;
import ws1415.ps1415.adapter.UserListAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

public class InviteUsersToGroupActivity extends Activity {
    // Viewelemente
    private EditText mSearchUserEditText;
    private ButtonFlat mSearchButton;
    private ListView mResultListView;
    private GroupMemberListAdapter mAdapter;
    private ButtonFlat mAcceptButton;
    private ButtonFlat mCancelButton;
    private ActionBar mActionBar;

    // Attribute für das Einladen
    List<String> usersToInvite;


    // Attribute zur Gruppe
    private String groupName;
    private List<String> groupMembers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_invite_users_to_group);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        mActionBar = getActionBar();

        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        groupName = getIntent().getStringExtra(GroupProfileActivity.EXTRA_GROUP_NAME);
        if(groupName == null){
            Toast.makeText(this, R.string.noGroupNameInExtra, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        groupMembers = getIntent().getStringArrayListExtra(GroupProfileActivity.EXTRA_GROUP_MEMBERS);
        if(groupMembers == null){
            Toast.makeText(this, R.string.noGroupMembersSubmitted, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Die Views initialisieren
        mSearchUserEditText = (EditText)findViewById(R.id.search_user_edit_text);
        mSearchButton = (ButtonFlat)findViewById(R.id.search_user_button);
        mResultListView = (ListView)findViewById(R.id.invite_user_list_view);
        mAcceptButton = (ButtonFlat)findViewById(R.id.invite_user_button_submit);
        mCancelButton = (ButtonFlat)findViewById(R.id.invite_user_button_abort);

        usersToInvite = new ArrayList<>();

        setButtonListener();
    }

    private void setButtonListener(){
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSearchUserEditText.getText().length() > 0) {
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, List<String> strings) {
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            if (strings != null && !strings.isEmpty()) {
                                setAdapter(strings);
                            }else{
                                Toast.makeText(InviteUsersToGroupActivity.this, R.string.nothingFound, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(InviteUsersToGroupActivity.this, message, Toast.LENGTH_LONG).show();
                            setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        }
                    }, mSearchUserEditText.getText().toString());
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(usersToInvite.size() > 0){
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    GroupController.getInstance().sendInvitation(new ExtendedTaskDelegateAdapter<Void, Void>(){
                        @Override
                        public void taskDidFinish(ExtendedTask task, Void aVoid) {
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            Toast.makeText(InviteUsersToGroupActivity.this, R.string.sendDone, Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(InviteUsersToGroupActivity.this, message, Toast.LENGTH_LONG).show();
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                    },groupName, usersToInvite);
                }else{
                    Toast.makeText(InviteUsersToGroupActivity.this, R.string.noUsersToInvite, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Setzt den Adapter auf die ListView.
     *
     * @param mails
     */
    private void setAdapter(List<String> mails){
        mAdapter = new GroupMemberListAdapter(mails, this, null, groupMembers);
        mResultListView.setAdapter(mAdapter);
    }

    /**
     * Fügt der Liste der Member welche Rechte erhalten sollen einen neuen
     * hinzu, falls diese nicht schon in der Liste enthalten ist.
     *
     * @param email
     */
    public void addMemberToList(String email){
        if(!usersToInvite.contains(email)){
            usersToInvite.add(email);
        }
    }

    /**
     * Entfernt einen Member aus der Liste welche Rechte erhalten sollen, falls
     * dieser in der Liste enthalten ist.
     *
     * @param email
     */
    public void removeMemberFromList(String email){
        if(usersToInvite.contains(email)){
            usersToInvite.remove(email);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_invite_users_to_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
