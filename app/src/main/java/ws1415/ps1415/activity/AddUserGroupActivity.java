package ws1415.ps1415.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.List;

import ws1415.common.controller.GroupController;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.R;
import ws1415.common.task.QueryUserGroupsTask;

/**
 * Diese Activity ist ein Dialog, der einen Gruppennamen erwartet und eine Gruppe anschließend
 * bei positiver Rückmeldung erstellt.
 *
 * @author Bernd Eissing, Martin Wrodarczyk
 */
public class AddUserGroupActivity extends Activity {
    private EditText addGroupEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_group);

        addGroupEditText = (EditText) findViewById(R.id.activity_add_user_group_edittext);
    }

    /**
     * Bricht den Hinzufügevorgang ab und kehrt zur UserGroupActivity zurück.
     *
     * @param view
     */
    public void cancel(View view) {
        finish();
    }

    /**
     * Fügt einen neue Guppe hinzu und prüft ob das Feld für den Gruppennamen leer gelassen wurde.
     *
     * @param view
     */
    public void apply(View view) throws Exception {
        final String groupName = addGroupEditText.getText().toString();
        if (!groupName.equals("")) {
            new QueryUserGroupsTask(new ExtendedTaskDelegateAdapter<Void, List<UserGroup>>() {
                @Override
                public void taskDidFinish(ExtendedTask task, List<UserGroup> userGroups) {
                    if (userGroups != null) {
                        for (int i = 0; i < userGroups.size(); i++) {
                            if (userGroups.get(i).getName().equals(groupName)) {
                                Toast.makeText(AddUserGroupActivity.this, "Name darf nicht schon vergeben sein", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }
                    finish();
                    GroupController.getInstance().createUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, Void aVoid) {
                            UsergroupActivity.getUserGroupActivity().refresh();
                        }
                    }, groupName);
                }
            }).execute();
        } else {
            Toast.makeText(this, "Name darf nicht leer sein", Toast.LENGTH_LONG).show();
        }
    }
}
