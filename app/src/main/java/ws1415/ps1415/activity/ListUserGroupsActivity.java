package ws1415.ps1415.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.UserGroupFilter;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.UsergroupAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 *
 *
 * @author Bernd Eissing
 */
public class ListUserGroupsActivity extends BaseActivity {
    // maximal Anzahl an gleichzeitig zu ladenen Nutzergruppen
    final private int MAX_GROUPS_PER_LOAD = 15;

    // die Views
    private ListView mUserGroupListView;
    private UsergroupAdapter mAdapter;
    private FloatingActionButton createUserGroupButton;
    private MenuItem mSearchItem;

    // Die Filter
    UserGroupFilter mFilterSearch;
    UserGroupFilter mFilterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_list_user_groups);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        mUserGroupListView = (ListView) findViewById(R.id.user_group_list_view);
        createUserGroupButton = (FloatingActionButton) findViewById(R.id.joinUserGroupButton);

        setClickListener();
        GroupController.getInstance().deleteUnusedBlobKeys(new ExtendedTaskDelegateAdapter<Void, Void>() {
        });

        handleIntent(getIntent());
    }

    private  void setClickListener(){
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_user_groups, menu);

        // SearchView initialisieren
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchItem = menu.findItem(R.id.action_search_group);
        SearchView searchView = (SearchView) mSearchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));


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
        if(id == R.id.action_list_groups){
            listAllGroups();
            //TODO an der alten Stelle der Liste anfangen, möglich aber zu Aufwending im moment
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Setzt die Events in die Liste
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    private void handleIntent(Intent intent){
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            final String searchKey = intent.getStringExtra(SearchManager.QUERY);
            mSearchItem.collapseActionView();
            UserGroupFilter filter = new UserGroupFilter();
            filter.setLimit(MAX_GROUPS_PER_LOAD);
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
            listSearchedGroups(searchKey);
        }else{
            // Lade alle Gruppen in Chunks, wenn diese Activity gestartet wird.
            listAllGroups();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * Dient zum Refreshen der Liste der aktuellen UserGroups.
     */
    public void listAllGroups() {
        mFilterList = new UserGroupFilter();
        mFilterList.setLimit(MAX_GROUPS_PER_LOAD);
        setMetaDatasToListView(mFilterList);
    }

    private void listSearchedGroups(String searchKey){
        mFilterSearch = new UserGroupFilter();
        mFilterSearch.setLimit(MAX_GROUPS_PER_LOAD);
        mAdapter = new UsergroupAdapter(this, searchKey, mFilterSearch);
        mUserGroupListView.setAdapter(mAdapter);
    }

    /**
     * Füllt die ListView mit den UserGroups vom Server.
     *
     * @param filter Der Filter für die Nutzergruppen die geladen werden sollen
     */
    public void setMetaDatasToListView(UserGroupFilter filter) {
        mAdapter = new UsergroupAdapter(this, null, filter);
        if (mUserGroupListView != null) mUserGroupListView.setAdapter(mAdapter);
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
