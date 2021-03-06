package ws1415.ps1415.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.GroupMemberListAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.UniversalUtil;

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
    private boolean textOkay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_invite_users_to_group);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }


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

        // Die Einladungsnachricht
        textOkay = false;
        setButtonListener();
    }

    private void setButtonListener(){
        mSearchUserEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER){
                    if (mSearchUserEditText.getText().length() > 0) {
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, List<String> strings) {
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                if (strings != null && !strings.isEmpty()) {
                                    setAdapter(strings);
                                } else {
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
                    return true;
                }else{
                    return false;
                }
            }
        });

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
                            } else {
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
                if (usersToInvite.size() > 0) {
                    AlertDialog.Builder altertadd = new AlertDialog.Builder(InviteUsersToGroupActivity.this);
                    LayoutInflater factory = LayoutInflater.from(InviteUsersToGroupActivity.this);
                    final View invitationMessageView = factory.inflate(R.layout.post_message, null);
                    final EditText messageEditText = (EditText) invitationMessageView.findViewById(R.id.post_black_board_edit_text);
                    final TextView failureText = (TextView) invitationMessageView.findViewById(R.id.failure_text_view);
                    failureText.setText(R.string.textTooShort);
                    failureText.setVisibility(View.VISIBLE);
                    textOkay = false;

                    messageEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            if(messageEditText.getText().toString().length() < 2){
                                failureText.setVisibility(View.VISIBLE);
                                textOkay = false;
                            }else{
                                failureText.setVisibility(View.GONE);
                                textOkay = true;
                            }
                        }
                    });

                    altertadd.setView(invitationMessageView);
                    altertadd.setTitle(R.string.invitation_title);
                    altertadd.setPositiveButton(R.string.sendButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(textOkay){
                                setProgressBarIndeterminateVisibility(Boolean.TRUE);
                                mAcceptButton.setVisibility(View.GONE);
                                mCancelButton.setVisibility(View.GONE);
                                GroupController.getInstance().sendInvitation(new ExtendedTaskDelegateAdapter<Void, Void>() {
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
                                        mAcceptButton.setVisibility(View.VISIBLE);
                                        mCancelButton.setVisibility(View.VISIBLE);
                                    }
                                }, groupName, usersToInvite, messageEditText.getText().toString());
                                dialog.dismiss();
                            }
                        }
                    });
                    altertadd.setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    altertadd.show();
                } else {
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
        ArrayList<String> searchResultAndUsersToInvite = new ArrayList<>();
        searchResultAndUsersToInvite.addAll(usersToInvite);
        searchResultAndUsersToInvite.addAll(mails);
        mAdapter = new GroupMemberListAdapter(this, searchResultAndUsersToInvite, groupMembers, usersToInvite);
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
