package ws1415.veranstalterapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


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
    public void cancel(View view){
        Intent intent = new Intent(this, HoldTabsActivity.class);
        startActivity(intent);
        HoldTabsActivity.getViewPager().setCurrentItem(2);
        HoldTabsActivity.getActionBar2().setSelectedNavigationItem(2);
    }

    public void apply(View view){
        finish();
        Intent intent = new Intent(this, RouteEditorActivity.class);
        intent.putExtra("routeName", routeNameEditText.getText());
        startActivity(intent);
    }

}
