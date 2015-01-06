package ws1415.veranstalterapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.task.AddHostTask;

public class AddHostDialog extends Activity {
    private EditText addHostEditText;
    private static PermissionManagementActivity pmActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_host_dialog);

        addHostEditText = (EditText) findViewById(R.id.activity_add_host_hostName_edittext);
    }

    /**
     * Bricht den Erstellvorgang ab und setzt die View auf die HoldTabsActivity mit dem ManageRoutesFragment
     *
     * @param view
     */
    public void cancel(View view) {
        finish();
    }

    public void apply(View view){
        String hostName = addHostEditText.getText().toString();
        if(!hostName.equals("")) {
            finish();
            new AddHostTask(pmActivity).execute(hostName);
        } else {
            Toast.makeText(this, "Mail darf nicht leer sein", Toast.LENGTH_LONG).show();
        }
    }

    public static void givePMActivity(PermissionManagementActivity a){
        pmActivity = a;
    }
}
