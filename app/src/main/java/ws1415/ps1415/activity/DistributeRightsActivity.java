package ws1415.ps1415.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.Switch;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.GroupMemberListAdapter;
import ws1415.ps1415.controller.RightController;
import ws1415.ps1415.model.Right;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

public class DistributeRightsActivity extends Activity {
    private String groupName;
    private List<String> recieveRightsChangeList;
    private List<String> rightsGrantedList;
    private List<String> rightsTakenList;
    private List<String> groupMembers;

    // Die Switch Views
    private Switch distributeRightsSwitch;
    private Switch changeGroupPictureSwitch;
    private Switch globalMessageSwitch;
    private Switch inviteGroupSwitch;
    private Switch deleteMemberSwitch;
    private Switch postBlacBoardSwitch;

    // Die Buttons
    private ButtonFlat acceptButton;
    private ButtonFlat cancelButton;

    ListView memberListView;
    GroupMemberListAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_distribute_rights);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        groupName = getIntent().getStringExtra("groupName");
        groupMembers = getIntent().getStringArrayListExtra("groupMembers");

        if(groupName == null){
            Toast.makeText(this, R.string.noGoupNameSubmitted, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if(groupMembers == null){
            Toast.makeText(this, R.string.noGroupMembersSubmitted, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialisiere sie Switches
        distributeRightsSwitch = (Switch)findViewById(R.id.switch_distribute_rights);
        changeGroupPictureSwitch = (Switch)findViewById(R.id.switch_change_group_picture);
        globalMessageSwitch = (Switch)findViewById(R.id.switch_global_message);
        inviteGroupSwitch = (Switch)findViewById(R.id.switch_invite_group);
        deleteMemberSwitch = (Switch)findViewById(R.id.switch_delete_member);
        postBlacBoardSwitch = (Switch)findViewById(R.id.switch_edit_black_board);

        // Initialisiere die Buttons
        acceptButton = (ButtonFlat)findViewById(R.id.distribute_rights_button_submit);
        cancelButton = (ButtonFlat)findViewById(R.id.distribute_rights_button_abort);

        // ListView initialisieren und Adapter setzen
        memberListView = (ListView)findViewById(R.id.distribute_rights_list_view);
        mAdapter = new GroupMemberListAdapter(groupMembers, this, null);
        memberListView.setAdapter(mAdapter);

        // Felder initialisieren
        recieveRightsChangeList = new ArrayList<>();
        rightsGrantedList = new ArrayList<>();
        // Zu Anfang sind alle Switches auf "off", also werden auch alle Rechte entzogen
        rightsTakenList = new ArrayList<>();
        rightsTakenList.add(Right.DISTRIBUTERIGHTS.name());
        rightsTakenList.add(Right.CHANGEGROUPPICTURE.name());
        rightsTakenList.add(Right.DELETEMEMBER.name());
        rightsTakenList.add(Right.INVITEGROUP.name());
        rightsTakenList.add(Right.EDITBLACKBOARD.name());
        rightsTakenList.add(Right.GLOBALMESSAGE.name());
        setClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_distribute_rights, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Fügt der Liste der Member welche Rechte erhalten sollen einen neuen
     * hinzu, falls diese nicht schon in der Liste enthalten ist.
     *
     * @param email
     */
    public void addMemberToList(String email){
        if(!recieveRightsChangeList.contains(email)){
            recieveRightsChangeList.add(email);
        }
    }

    /**
     * Entfernt einen Member aus der Liste welche Rechte erhalten sollen, falls
     * dieser in der Liste enthalten ist.
     *
     * @param email
     */
    public void removeMemberFromList(String email){
        if(recieveRightsChangeList.contains(email)){
            recieveRightsChangeList.remove(email);
        }
    }

    /**
     * Setzt die Listener auf die Switches. Damit die zu verteilenen Rechte oder
     * die Rechte die weggenommen werden sollen in die Listen dafür eingeordnet
     * werden.
     */
    private void setClickListener(){
        distributeRightsSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if(checked){
                    rightsGrantedList.add(Right.DISTRIBUTERIGHTS.name());
                    rightsTakenList.remove(Right.DISTRIBUTERIGHTS.name());
                }else{
                    rightsGrantedList.remove(Right.DISTRIBUTERIGHTS.name());
                    rightsTakenList.add(Right.DISTRIBUTERIGHTS.name());
                }

            }
        });
        changeGroupPictureSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked) {
                    rightsGrantedList.add(Right.CHANGEGROUPPICTURE.name());
                    rightsTakenList.remove(Right.CHANGEGROUPPICTURE.name());
                } else {
                    rightsGrantedList.remove(Right.CHANGEGROUPPICTURE.name());
                    rightsTakenList.add(Right.CHANGEGROUPPICTURE.name());
                }

            }
        });
        inviteGroupSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if(checked){
                    rightsGrantedList.add(Right.INVITEGROUP.name());
                    rightsTakenList.remove(Right.INVITEGROUP.name());
                }else{
                    rightsGrantedList.remove(Right.INVITEGROUP.name());
                    rightsTakenList.add(Right.INVITEGROUP.name());
                }

            }
        });
        deleteMemberSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if(checked){
                    rightsGrantedList.add(Right.DELETEMEMBER.name());
                    rightsTakenList.remove(Right.DELETEMEMBER.name());
                }else{
                    rightsGrantedList.remove(Right.DELETEMEMBER.name());
                    rightsTakenList.add(Right.DELETEMEMBER.name());
                }

            }
        });
        globalMessageSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked) {
                    rightsGrantedList.add(Right.GLOBALMESSAGE.name());
                    rightsTakenList.remove(Right.GLOBALMESSAGE.name());
                } else {
                    rightsGrantedList.remove(Right.GLOBALMESSAGE.name());
                    rightsTakenList.add(Right.GLOBALMESSAGE.name());
                }

            }
        });
        postBlacBoardSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked) {
                    rightsGrantedList.add(Right.EDITBLACKBOARD.name());
                    rightsTakenList.remove(Right.EDITBLACKBOARD.name());
                } else {
                    rightsGrantedList.remove(Right.EDITBLACKBOARD.name());
                    rightsTakenList.add(Right.EDITBLACKBOARD.name());
                }

            }
        });

        /**
         * Wenn mit der Rechteverteilung abgeschlossen ist und Mitglieder ausgewählt wurden
         * dann werden die Änderungen über den RightController an den Server übertragen.
          */
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recieveRightsChangeList.size() > 0){
                    if(rightsGrantedList.size() > 0){
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        RightController.getInstance().giveRightsToUsers(new ExtendedTaskDelegateAdapter<Void, Void>(){
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                finish();
                            }

                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(DistributeRightsActivity.this, message, Toast.LENGTH_LONG).show();
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            }
                        },groupName, recieveRightsChangeList, rightsGrantedList);
                    }
                    if(rightsTakenList.size() > 0){
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        RightController.getInstance().takeRightsFromUsers(new ExtendedTaskDelegateAdapter<Void, Void>(){
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                finish();
                            }

                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(DistributeRightsActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        }, groupName, recieveRightsChangeList, rightsTakenList);
                    }
                }else{
                    Toast.makeText(DistributeRightsActivity.this, R.string.noMembersToGrantRights, Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
