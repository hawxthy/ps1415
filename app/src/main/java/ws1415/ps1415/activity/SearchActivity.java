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
import android.widget.Toast;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.UserListAdapter;
import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.UniversalUtil;

/**
 * Diese Activity wird dafür genutzt, um das Ergebnis einer Benutzersuche anzuzeigen.
 *
 * @author Martin Wrodarczyk
 */
public class SearchActivity extends BaseActivity {
    private ListView mResultListView;
    private UserListAdapter mAdapter;
    private MenuItem mSearchItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_search);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        mResultListView = (ListView) findViewById(R.id.search_users_list_view);
        if(mAdapter != null) mResultListView.setAdapter(mAdapter);
        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String userMail = mAdapter.getItem(i).getEmail();
                Intent profile_intent = new Intent(SearchActivity.this, ProfileActivity.class);
                profile_intent.putExtra("email", userMail);
                startActivity(profile_intent);
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
            final String query = intent.getStringExtra(SearchManager.QUERY);
            setTitle(query);
            mSearchItem.collapseActionView();
            UserController.searchUsers(new ExtendedTaskDelegateAdapter<Void, List<String>>(){
                @Override
                public void taskDidFinish(ExtendedTask task, final List<String> stringList) {
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    if(stringList == null){
                        Toast.makeText(SearchActivity.this, getString(R.string.no_user_found), Toast.LENGTH_LONG).show();
                        mAdapter = null;
                        mResultListView.setAdapter(null);
                    } else {
                        setUpList(stringList);
                    }
                }
                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    Toast.makeText(SearchActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }, query);
        }
    }

    /**
     * Füllt die Liste mit dem Ergebnis des Querys.
     *
     * @param userMails E-Mail Adressen der Benutzer des Ergebnisses
     */
    private void setUpList(List<String> userMails){
        mAdapter = new UserListAdapter(userMails, this);
        if(mResultListView != null) mResultListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // SearchView initialisieren
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) mSearchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
