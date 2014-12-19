package ws1415.veranstalterapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.Host;

import java.util.List;

import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.task.QueryHostsTask;

public class PermissionManagementActivity extends Activity {
    HostCursorAdapter adapter;
    ListView listView;
    List<Host> hostList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_management);

        listView = (ListView) findViewById(R.id.activtiy_permission_management_list_view);

        new QueryHostsTask();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.permission_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Füllt die ListView mit den Events vom Server.
     *
     * @param results ArrayList von Events
     */
    public void setHostsToListView(List<Host> results) {
        hostList = results;
        adapter = new HostCursorAdapter(this, hostList);
        listView.setAdapter(adapter);
    }
}
