package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupFilter;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.UsergroupAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

public class ListUserGroupsActivity extends BaseActivity {
    // maximal Anzahl an gleichzeitig zu ladenen Nutzergruppen
    final private int MAX_GROUPS_PER_LOAD = 10;

    private ListView mUserGroupListView;
    private List<UserGroupMetaData> mUserGroupList;
    private UsergroupAdapter mAdapter;
    private FloatingActionButton createUserGroupButton;
    private AlertDialog c_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_list_user_groups);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        mUserGroupListView = (ListView) findViewById(R.id.user_group_list_view);
        createUserGroupButton = (FloatingActionButton) findViewById(R.id.joinUserGroupButton);

        createUserGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createGroupIntent = new Intent(ListUserGroupsActivity.this, CreateUserGroupActivity.class);
                ListUserGroupsActivity.this.startActivity(createGroupIntent);
            }
        });

        mUserGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openGroupProfile(mAdapter.getItem(i));
            }

        });

        GroupController.getInstance().deleteUnusedBlobKeys(new ExtendedTaskDelegateAdapter<Void, Void>(){
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_user_groups, menu);
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
     * Setzt die Events in die Liste
     */
    @Override
    public void onResume() {
        super.onResume();
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        refresh();
    }

    /**
     * Dient zum Refreshen der Liste der aktuellen UserGroups.
     */
    public void refresh() {
        final UserGroupFilter filter = new UserGroupFilter();
        filter.setLimit(MAX_GROUPS_PER_LOAD);
        setMetaDatasToListView(filter);
    }

    /**
     * Füllt die ListView mit den UserGroups vom Server.
     *
     * @param filter Der Filter für die Nutzergruppen die geladen werden sollen
     */
    public void setMetaDatasToListView(UserGroupFilter filter) {
        mAdapter = new UsergroupAdapter(this, filter,  -1);
        if (mUserGroupListView != null) mUserGroupListView.setAdapter(mAdapter);
    }

    /**
     * Startet das Gruppenprofil. Dabei muss im Intent der Name der Gruppe übergeben werden.
     *
     * @param medaData Die Metadaten zu der Gruppe
     */
    private void openGroupProfile(UserGroup medaData){
        Intent open_group_profile_intent = new Intent(this, GroupProfileActivity.class);
        open_group_profile_intent.putExtra("groupName", medaData.getName());
        startActivity(open_group_profile_intent);
    }
}
