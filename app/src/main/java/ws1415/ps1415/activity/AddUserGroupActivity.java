package ws1415.ps1415.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.skatenight.skatenightAPI.SkatenightAPI;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.fragment.AllUsergroupsFragment;
import ws1415.ps1415.task.AddUserGroupTask;
import ws1415.ps1415.task.QueryMyUserGroupsTask;
import ws1415.ps1415.task.QueryUserGroupsTask;


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
    public void apply(View view) throws Exception {
        final String hostName = addHostEditText.getText().toString();
        if (!hostName.equals("")) {
            new QueryUserGroupsTask().execute(new AllUsergroupsFragment() {
                @Override
                public void setUserGroupsToListView(List<UserGroup> groupList) {
                    for (int i = 0; i < groupList.size(); i++) {
                        if (groupList.get(i).getName().equals(hostName)) {
                            Toast.makeText(AddUserGroupActivity.this, "Name darf nicht schon vergeben sein", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    finish();
                    new AddUserGroupTask(UsergroupActivity.getUserGroupActivity()).execute(hostName);
                }
            });
        }else {
            Toast.makeText(this, "Name darf nicht leer sein", Toast.LENGTH_LONG).show();
        }
    }
}
