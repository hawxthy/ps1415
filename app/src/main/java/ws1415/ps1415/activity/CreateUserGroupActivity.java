package ws1415.ps1415.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.Switch;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;

import ws1415.ps1415.R;
import ws1415.ps1415.model.NavDrawerGroupList;

public class CreateUserGroupActivity extends BaseActivity {
    private FloatLabeledEditText groupNameEditText;
    private FloatLabeledEditText groupDescriptionEditText;
    private FloatLabeledEditText groupPasswordEditText;
    private FloatLabeledEditText groupPasswordAgainEditText;
    private ButtonRectangle checkNameButton;

    private View mViewGroup;

    private Switch groupPrivacySwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(NavDrawerGroupList.items, R.layout.activity_create_user_group);

        groupNameEditText = (FloatLabeledEditText) findViewById(R.id.create_user_group_group_name);
        groupDescriptionEditText = (FloatLabeledEditText) findViewById(R.id.create_user_group_group_description);
        groupPasswordEditText = (FloatLabeledEditText) findViewById(R.id.create_user_group_group_password);
        groupPasswordAgainEditText = (FloatLabeledEditText) findViewById(R.id.create_user_group_group_password_again);
        checkNameButton = (ButtonRectangle) findViewById(R.id.create_user_group_button_check_name);

        mViewGroup = findViewById(R.id.create_user_group_password_view);

        groupPrivacySwitch = (Switch) findViewById(R.id.create_user_group_switchView);

        setUpButotnListener();
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

    private void setUpButotnListener(){
        groupPrivacySwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if(checked == true){
                    mViewGroup.setVisibility(View.VISIBLE);
                }else{
                    mViewGroup.setVisibility(View.GONE);
                }
            }
        });

        checkNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNameButton.setBackgroundResource(R.color.colorPrimary);
                checkNameButton.setBackgroundResource(R.drawable.ic_done_white_24dp);
            }
        });
    }
}
