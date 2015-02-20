package ws1415.veranstalterapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ws1415.veranstalterapp.activity.RouteEditorActivity;


/**
 * Dialog zum Hinzufügen einer Route.
 *
 * @author Bernd Eissing, Martin Wrodarczyk
 */
public class AddRouteDialog extends Activity {
    private EditText routeNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route_dialog);

        routeNameEditText = (EditText) findViewById(R.id.activity_add_route_routeName_edittext);
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
        String routeName = routeNameEditText.getText().toString();
        if(!routeName.equals("")) {
            finish();
            Log.d("routeName", routeNameEditText.getText().toString());
            Intent intent = new Intent(this, RouteEditorActivity.class);
            intent.putExtra(RouteEditorActivity.EXTRA_NAME, routeNameEditText.getText().toString());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Routenname darf nicht leer sein", Toast.LENGTH_LONG).show();
        }
    }

}
