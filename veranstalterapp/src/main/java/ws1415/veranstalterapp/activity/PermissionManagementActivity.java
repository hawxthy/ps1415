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

/**
 * Klasse zum Verwalten von Veranstaltern und deren Rechten.
 *
 * @author Bernd Eissing, Martin Wrodarczyk
 */
public class PermissionManagementActivity extends Activity {
    HostCursorAdapter adapter;
    ListView listView;
    List<Host> hostList;
    AlertDialog c_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_management);

        listView = (ListView) findViewById(R.id.activtiy_permission_management_list_view);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                createSelectionsMenu(i);
            }
        });

        new QueryHostsTask().execute(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.permission_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_add_host){
            addHost();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Füllt die ListView mit den Veranstaltern vom Server.
     *
     * @param results ArrayList von Veranstaltern
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

        c_dialog = builder.create();
        builder.show();
    }

    public List<Host> getHostList() {
        return hostList;
    }

    public AlertDialog getLastDialog(){
        return c_dialog;
    }
}
