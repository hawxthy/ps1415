package ws1415.veranstalterapp.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.activity.RouteEditorActivity;
import ws1415.veranstalterapp.fragment.EditorWaypointsFragment;

/**
 * Dialog um einen vorher ausgewählten Wegpunkt einer Route zu editieren.
 *
 * @author Bernd Eissing
 */
public class EditWaypointDialog extends Activity {
    public static final String EXTRA_INDEX = "index";
    private int index;
    private EditText waypointNameEditText;
    private RouteEditorActivity routeEditorActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_waypoint);

        routeEditorActivity = RouteEditorActivity.getRouteEditorActivity();
        index = getIntent().getExtras().getInt(EXTRA_INDEX);
        waypointNameEditText = (EditText) findViewById(R.id.activity_edit_waypoint_waypointName_edittext);
    }

    /**
     * Bricht den Editiervorgang ab.
     *
     * @param view
     */
    public void cancel(View view) {
        finish();
    }

    /**
     * Methode zum Ändern des Namens eines Wegpunktes.
     *
     * @param view
     */
    public void apply(View view){
        String waypointName = waypointNameEditText.getText().toString();
        if(!waypointName.equals("")) {
            finish();
            routeEditorActivity.getArrayAdapter().getItem(index).getMarkerOptions().title(waypointName);
            routeEditorActivity.getArrayAdapter().notifyDataSetChanged();
            routeEditorActivity.repaintWayppoint(routeEditorActivity.getArrayAdapter().getItem(index));
        } else {
            Toast.makeText(this, "Name darf nicht leer sein", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Methode zum Löschen eines Wegpunktes
     *
     * @param view
     */
    public void remove(View view){
        finish();
        routeEditorActivity.removeWaypoint(index);
    }

}
