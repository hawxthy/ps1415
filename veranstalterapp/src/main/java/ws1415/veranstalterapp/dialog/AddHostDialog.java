package ws1415.veranstalterapp.dialog;

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
import ws1415.veranstalterapp.activity.PermissionManagementActivity;
import ws1415.veranstalterapp.task.AddHostTask;

/**
 * Dialog zum Hinzufügen eines neuen Veranstalters.
 *
 * @author Bernd Eissing, Martin Wrodarczyk.
 */
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
            new AddHostTask(pmActivity).execute(hostName);
        } else {
            Toast.makeText(this, "Mail darf nicht leer sein", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Statische Methode zum übergeben der Instanz der PermissionManagementActivity.
     *
     * @param a die PermissionManagementActivity
     */
    public static void givePMActivity(PermissionManagementActivity a){
        pmActivity = a;
    }
}
