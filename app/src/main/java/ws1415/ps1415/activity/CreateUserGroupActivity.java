package ws1415.ps1415.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import ws1415.ps1415.R;
import ws1415.ps1415.model.FloatingHintEditText;
import ws1415.ps1415.model.NavDrawerGroupList;

public class CreateUserGroupActivity extends BaseActivity {
    private FloatingHintEditText groupNameEditText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(NavDrawerGroupList.items, R.layout.activity_create_user_group);

//        groupNameEditText = (FloatingHintEditText) findViewById(R.id.create_user_group_group_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_user_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
