package ws1415.ps1415.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ws1415.ps1415.R;
import ws1415.ps1415.fragment.AllUsergroupsFragment;
import ws1415.ps1415.task.AddUserGroupTask;


public class AddUserGroupActivity extends Activity {
    private static AllUsergroupsFragment allUsergroupsFragment;
    private EditText addHostEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_group);

        addHostEditText = (EditText) findViewById(R.id.activity_add_user_group_edittext);
    }

    /**
     * Bricht den Hinzufügevorgang ab und kehrt zur PermissionManagementActivity zurück.
     *
     * @param view
     */
    public void cancel(View view) {
        finish();
    }

    /**
     * Fügt einen neuen Veranstalter der Liste von Veranstaltern hinzu und prüft ob das Feld für die
     * E-Mail leer gelassen wurde.
     *
     * @param view
     */
    public void apply(View view){
        String hostName = addHostEditText.getText().toString();
        if(!hostName.equals("")) {
            finish();
            new AddUserGroupTask(allUsergroupsFragment).execute(hostName);
        } else {
            Toast.makeText(this, "Name darf nicht leer sein", Toast.LENGTH_LONG).show();
        }
    }


    public static void giveAllUsergroupsFragment(AllUsergroupsFragment fragment){
        allUsergroupsFragment = fragment;
    }


}
