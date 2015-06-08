package ws1415.ps1415.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import com.gc.materialdesign.views.ButtonFlat;
import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupFilter;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.GroupMemberListAdapter;
import ws1415.ps1415.adapter.UsergroupAdapter;
import ws1415.ps1415.util.PrefManager;

public class MyUserGroupsActivity extends Activity {
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

        setContentView(R.layout.activity_my_user_groups);
        mResultListView = (ListView) findViewById(R.id.my_groups_list_view);
        mCreateGroupButton = (FloatingActionButton) findViewById(R.id.create_user_group_button_my_groups);
        UserGroupFilter filter = new UserGroupFilter();
        filter.setLimit(MAX_GROUPS_PER_LOAD);
        mAdapter = new UsergroupAdapter(this, filter, -1);
        mResultListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_user_groups, menu);
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
}
