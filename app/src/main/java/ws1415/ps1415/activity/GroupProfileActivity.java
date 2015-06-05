package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import ws1415.ps1415.fragment.GroupMembersFragment;
import ws1415.ps1415.widget.SlidingTabLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupBlackBoardTransport;
import com.skatenight.skatenightAPI.model.UserGroupMembers;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.GroupProfileTabsAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.GroupImageLoader;

public class GroupProfileActivity extends BaseFragmentActivity {
    private ImageView mGroupPicture;
    private TextView mGroupNameTextView;
    private FloatingActionButton mJoinButton;
    private ViewPager mViewPager;
    private SlidingTabLayout mTabLayout;
    private GroupProfileTabsAdapter mAdapter;

    private String groupName;
    private String groupPassword;
    private String groupCreator;
    private UserGroup group;
    private boolean checkIsMember;

    String[] tabs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        mGroupPicture = (ImageView) findViewById(R.id.group_profile_image_view);
        mGroupNameTextView = (TextView) findViewById(R.id.group_profile_group_name_text_view);
        mJoinButton = (FloatingActionButton) findViewById(R.id.group_profile_join_button);
        mViewPager = (ViewPager) findViewById(R.id.group_profile_view_pager);
        mTabLayout = (SlidingTabLayout) findViewById(R.id.group_profile_tab_layout);

        groupName = getIntent().getStringExtra("groupName");
        groupCreator = getIntent().getStringExtra("groupCreator");
        mGroupNameTextView.setText(groupName);


        GroupImageLoader.getInstance().setGroupImageToImageView(this, groupName, mGroupPicture);


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

        // Initialisiere die Tabs
        //TODO auf Martin warten
        //setUpMember();
        checkIsMember = false;
        getGroup();
        setUpBlackBoard();
        setUpNewsBoard();
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

    /**
     * Ruft die Mitglieder der Gruppe ab. Ein Toast mit einer Fehlermeldung wird
     * ausgegeben, falls ein Fehler auftritt.
     */
    private void getGroup() {
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                GroupProfileActivity.this.group = group;
                //TODO nullpointer
                //mAdapter.getGroupMembersFragment().setUp(group, GroupProfileActivity.this);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG);
            }
        }, groupName);
    }

    private void getGroupBlackBoard() {
        GroupController.getInstance().getBlackBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupBlackBoardTransport>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupBlackBoardTransport userGroupBlackBoardTransport) {
                if (userGroupBlackBoardTransport != null) {
                    mAdapter.getGroupBlackBoardFragment().setUp(userGroupBlackBoardTransport, groupName, GroupProfileActivity.this);
                }
            }
        }, groupName);
    }

    private void setUpMember() {
    }

    private void setUpBlackBoard() {
        getGroupBlackBoard();
    }

    private void setUpNewsBoard() {

    }

    private void setClickListener() {
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkIsMember && !group.getPrivat()){
                    AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
                    LayoutInflater factory = LayoutInflater.from(GroupProfileActivity.this);
                    altertadd.setMessage(R.string.postMessageTitle);
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
                }

            }
        });
    }
}
