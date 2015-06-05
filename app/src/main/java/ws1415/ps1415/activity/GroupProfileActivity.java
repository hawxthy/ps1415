package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.fragment.GroupMembersFragment;
import ws1415.ps1415.model.NavDrawerGroupList;
import ws1415.ps1415.widget.SlidingTabLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.GroupProfileTabsAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.GroupImageLoader;

public class GroupProfileActivity extends BaseFragmentActivity {
    // Die View Elemente
    private ImageView mGroupPicture;
    private TextView mGroupNameTextView;
    private FloatingActionButton mJoinButton;
    private ViewPager mViewPager;
    private SlidingTabLayout mTabLayout;
    private GroupProfileTabsAdapter mAdapter;

    // Attribute zur Gruppe
    private String groupName;
    private UserGroup group;
    private boolean checkIsMember;

    String[] tabs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialisiere die Views
        super.setContentView(NavDrawerGroupList.items, R.layout.activity_group_profile);
        mGroupPicture = (ImageView) findViewById(R.id.group_profile_image_view);
        mGroupNameTextView = (TextView) findViewById(R.id.group_profile_group_name_text_view);
        mJoinButton = (FloatingActionButton) findViewById(R.id.group_profile_join_button);
        mViewPager = (ViewPager) findViewById(R.id.group_profile_view_pager);
        mTabLayout = (SlidingTabLayout) findViewById(R.id.group_profile_tab_layout);

        // Gruppennamen aus dem Intent holen, falls nicht vorhanden Activity nicht starten.
        groupName = getIntent().getStringExtra("groupName");
        if(groupName == null){
            Toast.makeText(this, R.string.noGroupNameInExtra, Toast.LENGTH_LONG).show();
            return;
        }

        // Beschrifte die Tabs
        tabs = new String[]{
                getResources().getString(R.string.title_fragment_group_members),
                getResources().getString(R.string.title_fragment_group_black_board),
                getResources().getString(R.string.title_fragment_group_news_board)};

        mAdapter = new GroupProfileTabsAdapter(getSupportFragmentManager(), tabs);
        mViewPager.setAdapter(mAdapter);

        // Give the TabLayout the ViewPager
        mTabLayout.setDistributeEvenly(true);
        mTabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mTabLayout.setViewPager(mViewPager);
        mViewPager.setCurrentItem(1);

        setClickListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_profile, menu);
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

    @Override
    protected void onResume() {
        super.onResume();

        setUpProfile();
    }

    /**
     *
     *
     */
    private void setUpProfile(){
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                // group nuss nicht abgefragt werden, denn der Server gibt bereist einen Fehler wenn eine
                // Gruppe mit dem Namen nicht existiert
                GroupProfileActivity.this.group = group;
                mAdapter.getGroupMembersFragment().setUp(group, GroupProfileActivity.this);
                if (group.getMemberRights().keySet().contains(ServiceProvider.getEmail())) {
                    checkIsMember = true;

                } else {
                    checkIsMember = false;
                }
                GroupImageLoader.getInstance().setGroupImageToImageView(GroupProfileActivity.this, group.getBlobKey().getKeyString(),mGroupPicture);
                mGroupNameTextView.setText(group.getName());
                mAdapter.getGroupMembersFragment().setUp(group, GroupProfileActivity.this);
                mAdapter.getGroupBlackBoardFragment().setUp(group.getBlackBoard(), groupName, GroupProfileActivity.this);
                mAdapter.getGroupNewsBoardFragment().setUp(group.getNewsBoard(), groupName, GroupProfileActivity.this);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG);
            }
        }, groupName);
    }

    /**
     * Setzt die Listener für die Buttons.
     *
     */
    private void setClickListener() {
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkIsMember && !group.getPrivat()){
                    // Zeige einen Dialog ohne Passwort um eine öffentliche Gruppe zu joinen
                    AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
                    LayoutInflater factory = LayoutInflater.from(GroupProfileActivity.this);
                    altertadd.setMessage(R.string.title_alert_dialog_join_user_group);
                    altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GroupController.getInstance().joinUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>(){
                                @Override
                                public void taskDidFinish(ExtendedTask task, BooleanWrapper booleanWrapper) {
                                    if(booleanWrapper.getValue()){
                                        checkIsMember = true;
                                        //TODO Buttons sichtbar machen
                                    }
                                }
                                @Override
                                public void taskFailed(ExtendedTask task, String message) {
                                    Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                                }
                            }, groupName);
                            dialog.dismiss();
                        }
                    });
                    altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    altertadd.show();
                }else if(!checkIsMember && group.getPrivat()){
                    // Zeige Dialog mit Passwort um eine private Gruppe zu joinen
                    AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
                    LayoutInflater factory = LayoutInflater.from(GroupProfileActivity.this);
                    final View passwordView = factory.inflate(R.layout.preview_image, null);
                    final EditText passwordEditText = (EditText)passwordView.findViewById(R.id.password_view);
                    altertadd.setMessage(R.string.title_alert_dialog_join_user_group);
                    altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(!passwordEditText.getText().toString().isEmpty()){
                                GroupController.getInstance().joinPrivateUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
                                    @Override
                                    public void taskDidFinish(ExtendedTask task, BooleanWrapper booleanWrapper) {
                                        if (booleanWrapper.getValue()) {
                                            checkIsMember = true;
                                            //TODO Buttons sichtbar machen
                                        }
                                    }

                                    @Override
                                    public void taskFailed(ExtendedTask task, String message) {
                                        Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                                    }
                                }, groupName, passwordEditText.getText().toString());
                                dialog.dismiss();
                            }
                        }
                    });
                    altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                }else{
                    Toast.makeText(GroupProfileActivity.this, R.string.alreadyInGroupString, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    /**
     * Gibt die Rechte des Benutzers zurück, die er in dieser Gruppe hat.
     * Falls er kein Mitglied ist, ist das Resultat null
     * @return Die Rechte innerhalb dieser Gruppe oder null
     */
    public List<String> getRights(){
        return  (ArrayList<String>)group.getMemberRights().get(ServiceProvider.getEmail());
    }
}
