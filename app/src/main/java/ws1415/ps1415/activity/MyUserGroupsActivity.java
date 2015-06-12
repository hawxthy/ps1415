package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.gc.materialdesign.views.ButtonFlat;
import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupFilter;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.GroupMemberListAdapter;
import ws1415.ps1415.adapter.UsergroupAdapter;
import ws1415.ps1415.model.NavDrawerGroupList;
import ws1415.ps1415.util.PrefManager;

public class MyUserGroupsActivity extends BaseFragmentActivity {
    // maximal Anzahl an gleichzeitig zu ladenen Nutzergruppen
    final private int MAX_GROUPS_PER_LOAD = 10;
    // Viewelemente
    private ListView mResultListView;
    private UsergroupAdapter mAdapter;
    private FloatingActionButton mCreateGroupButton;


    // Attribute für das Einladen
    List<String> usersToInvite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.setContentView(NavDrawerGroupList.items, R.layout.activity_my_user_groups);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        //TODO refresh Button machen
        mResultListView = (ListView) findViewById(R.id.my_groups_list_view);
        mCreateGroupButton = (FloatingActionButton) findViewById(R.id.create_user_group_button_my_groups);

        setListener();
        setUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_user_groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh_groups) {
            setUp();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListener(){
        mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent start_create_group_intent = new Intent(MyUserGroupsActivity.this, CreateUserGroupActivity.class);
                startActivity(start_create_group_intent);
            }
        });

        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openGroupProfile(mAdapter.getItem(i));
            }
        });
    }

    private void setUp(){
        UserGroupFilter filter = new UserGroupFilter();
        filter.setLimit(MAX_GROUPS_PER_LOAD);
        mAdapter = new UsergroupAdapter(this, filter, null);
        mResultListView.setAdapter(mAdapter);
    }

    /**
     * Startet das Gruppenprofil. Dabei muss im Intent der Name der Gruppe übergeben werden.
     *
     * @param medaData Die Metadaten zu der Gruppe
     */
    private void openGroupProfile(UserGroupMetaData medaData){
        Intent open_group_profile_intent = new Intent(this, GroupProfileActivity.class);
        open_group_profile_intent.putExtra(GroupProfileActivity.EXTRA_GROUP_NAME, medaData.getName());
        startActivity(open_group_profile_intent);
    }
}
