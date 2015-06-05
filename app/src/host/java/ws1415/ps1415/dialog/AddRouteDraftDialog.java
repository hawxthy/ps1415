package ws1415.ps1415.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.ServerWaypoint;

import java.util.ArrayList;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.RouteEditorActivity;

/**
 * Dialog für das Erstellen einer neuen Gruppe anhand einer vorher gewählten Route als Vorlage.
 */
public class AddRouteDraftDialog extends Activity {
    public static final String EXTRA_WAYPOINTS = "route_draft_dialog_extra_waypoints";

    private ArrayList<ServerWaypoint> wpList;
    private EditText routeNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route_draft_dialog);

        wpList = (ArrayList<ServerWaypoint>) getIntent().getSerializableExtra(EXTRA_WAYPOINTS);
        routeNameEditText = (EditText) findViewById(R.id.activity_add_route_draft_routeName_edittext);
    }

    /**
     * Bricht den Erstellvorgang ab und setzt die View auf die HoldTabsActivity mit dem ManageRoutesFragment
     *
     * @param view
     */
    public void cancel(View view) {
        finish();
    }

    /**
     * Startet die RouteEditorActivity mit den Informationen des eingegebenen Names und der
     * Wegpunkte der Routenvorlage.
     *
     * @param view
     */
    public void apply(View view) {
        String routeName = routeNameEditText.getText().toString();
        if (!routeName.equals("")) {
            finish();
            Log.d("routeName", routeNameEditText.getText().toString());
            Intent intent = new Intent(this, RouteEditorActivity.class);
            intent.putExtra(RouteEditorActivity.EXTRA_NAME, routeNameEditText.getText().toString());
            intent.putExtra(RouteEditorActivity.EXTRA_WAYPOINTS, wpList);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Routenname darf nicht leer sein", Toast.LENGTH_LONG).show();
        }
    }

}
