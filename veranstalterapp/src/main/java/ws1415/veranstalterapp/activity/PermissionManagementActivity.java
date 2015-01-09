package ws1415.veranstalterapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.Host;

import java.util.List;

import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.adapter.HostCursorAdapter;
import ws1415.veranstalterapp.task.DeleteHostTask;
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

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            /**
             * Löscht den Host vom Server und von der listView
             *
             * @param adapterView
             * @param view
             * @param i Position des Hosts in der listView
             * @param l
             * @return
             */
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                createSelectionsMenu(i);
                return true;
            }
        });

        new QueryHostsTask().execute(this);
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
        }else if(id == R.id.action_add_host){
            addHost();
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

    /**
     * Startet den AddHostDialog mit allen nötigen Informationen.
     */
    public void addHost(){
        AddHostDialog.givePMActivity(this);
        Intent intent = new Intent(this, AddHostDialog.class);
        startActivity(intent);
    }

    /**
     * Dient zum refreshen der Liste der aktuellen Hosts.
     */
    public void refresh(){
        new QueryHostsTask().execute(this);
    }

    /**
     * Erstellt einen Dialog, welcher aufgerufen wird, wenn ein Item in der ListView lange
     * ausgewählt wird. In diesem Dialog kann man dann auswählen, ob man den ausgewählten
     * Host löschen möchte.
     *
     * @param position
     */
    private void createSelectionsMenu(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(hostList.get(position).getEmail())
                .setItems(R.array.selections_menu_manage_hosts, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int index) {
                        if (index == 0) {
                            new DeleteHostTask(PermissionManagementActivity.this).execute(hostList.get(position).getEmail());
                        }
                    }
                });
        builder.create();
        builder.show();
    }
}