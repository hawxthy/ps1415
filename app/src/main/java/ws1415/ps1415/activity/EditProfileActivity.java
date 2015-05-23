package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.InfoPair;
import com.skatenight.skatenightAPI.model.UserInfo;

import ws1415.common.controller.UserController;
import ws1415.common.model.Visibility;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.R;

public class EditProfileActivity extends Activity {
    private UserInfo mUserInfo;
    private boolean mOptOutSearch;
    private Visibility mShowPrivateGroups;

    private ImageView mImageViewPicture;
    private EditText mEditTextFirstName;
    private EditText mEditTextLastName;
    private Spinner mSpinnerLastNameVisibility;
    private Spinner mSpinnerGender;
    private EditText mEditTextDateOfBirth;
    private Spinner mSpinnerDateOfBirthVisibility;
    private EditText mEditTextCity;
    private Spinner mSpinnerCityVisibility;
    private EditText mEditTextPlz;
    private Spinner mSpinnerPlzVisibility;
    private EditText mEditTextDescription;
    private Spinner mSpinnerDescriptionVisibility;
    private CheckBox mCheckBoxOptOut;
    private Spinner mSpinnerPrivateGroupsVisibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit_profile);

        Intent intent = getIntent();
        handleIntent(intent);

        mImageViewPicture = (ImageView) findViewById(R.id.activity_edit_profile_picture);
        mEditTextFirstName = (EditText) findViewById(R.id.activity_edit_profile_first_name);
        mEditTextLastName = (EditText) findViewById(R.id.activity_edit_profile_last_name);
        mSpinnerLastNameVisibility = (Spinner) findViewById(R.id.activity_edit_profile_last_name_visibility);
        mSpinnerGender = (Spinner) findViewById(R.id.activity_edit_profile_set_gender);
        mEditTextDateOfBirth = (EditText) findViewById(R.id.activity_edit_profile_dateofbirth);
        mSpinnerDateOfBirthVisibility = (Spinner) findViewById(R.id.activity_edit_profile_dateofbirth_visibility);
        mEditTextCity = (EditText) findViewById(R.id.activity_edit_profile_city);
        mSpinnerCityVisibility = (Spinner) findViewById(R.id.activity_edit_profile_city_visibility);
        mEditTextPlz = (EditText) findViewById(R.id.activity_edit_profile_plz);
        mSpinnerPlzVisibility = (Spinner) findViewById(R.id.activity_edit_profile_plz_visibility);
        mEditTextDescription = (EditText) findViewById(R.id.activity_edit_profile_description);
        mSpinnerDescriptionVisibility = (Spinner) findViewById(R.id.activity_edit_profile_description_visibility);
        mCheckBoxOptOut = (CheckBox) findViewById(R.id.activity_edit_profile_findable);
        mSpinnerPrivateGroupsVisibility = (Spinner) findViewById(R.id.activity_edit_profile_private_groups_visibility);

        mEditTextFirstName.setText(mUserInfo.getFirstName());
        mEditTextLastName.setText(mUserInfo.getLastName().getValue());
        mSpinnerLastNameVisibility.setSelection(mUserInfo.getLastName().getVisibility());
        mSpinnerGender.setSelection(mUserInfo.getGender());
        mEditTextDateOfBirth.setText(mUserInfo.getDateOfBirth().getValue());
        mSpinnerDateOfBirthVisibility.setSelection(mUserInfo.getDateOfBirth().getVisibility());
        mEditTextCity.setText(mUserInfo.getCity().getValue());
        mSpinnerCityVisibility.setSelection(mUserInfo.getCity().getVisibility());
        mEditTextPlz.setText(mUserInfo.getPostalCode().getValue());
        mSpinnerPlzVisibility.setSelection(mUserInfo.getPostalCode().getVisibility());
        mEditTextDescription.setText(mUserInfo.getDescription().getValue());
        mSpinnerDescriptionVisibility.setSelection(mUserInfo.getDescription().getVisibility());
        mCheckBoxOptOut.setChecked(mOptOutSearch);
        mSpinnerPrivateGroupsVisibility.setSelection(mShowPrivateGroups.getId());

        getActionBar().setDisplayHomeAsUpEnabled(true);


    }

    private void handleIntent(Intent intent) {
        mUserInfo = new UserInfo();
        mUserInfo.setEmail(intent.getStringExtra("email"));
        mUserInfo.setFirstName(intent.getStringExtra("firstName"));
        mUserInfo.setGender(intent.getIntExtra("gender", 0));
        mUserInfo.setLastName(new InfoPair()
                .setValue(intent.getStringExtra("lastName"))
                .setVisibility(intent.getIntExtra("lastNameVisibility", 0)));
        mUserInfo.setDateOfBirth(new InfoPair()
                .setValue(intent.getStringExtra("dateOfBirth"))
                .setVisibility(intent.getIntExtra("dateOfBirthVisibility", 0)));
        mUserInfo.setCity(new InfoPair()
                .setValue(intent.getStringExtra("city"))
                .setVisibility(intent.getIntExtra("cityVisibility", 0)));
        mUserInfo.setPostalCode(new InfoPair()
                .setValue(intent.getStringExtra("plz"))
                .setVisibility(intent.getIntExtra("plzVisibility", 0)));
        mUserInfo.setDescription(new InfoPair()
                .setValue(intent.getStringExtra("description"))
                .setVisibility(intent.getIntExtra("descriptionVisibility", 0)));
        mOptOutSearch = intent.getBooleanExtra("optOutSearch", false);
        mShowPrivateGroups = Visibility.getValue(intent.getIntExtra("showPrivateGroups", 0));
    }

    private void sendData(){
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        mUserInfo.setFirstName(mEditTextFirstName.getText().toString());
        mUserInfo.getLastName().setValue(mEditTextLastName.getText().toString());
        mUserInfo.getLastName().setVisibility(mSpinnerLastNameVisibility.getSelectedItemPosition());
        mUserInfo.setGender(mSpinnerGender.getSelectedItemPosition());
        mUserInfo.getDateOfBirth().setValue(mEditTextDateOfBirth.getText().toString());
        mUserInfo.getDateOfBirth().setVisibility(mSpinnerDateOfBirthVisibility.getSelectedItemPosition());
        mUserInfo.getCity().setValue(mEditTextCity.getText().toString());
        mUserInfo.getCity().setVisibility(mSpinnerCityVisibility.getSelectedItemPosition());
        mUserInfo.getPostalCode().setValue(mEditTextPlz.getText().toString());
        mUserInfo.getPostalCode().setVisibility(mSpinnerPlzVisibility.getSelectedItemPosition());
        mUserInfo.getDescription().setValue(mEditTextDescription.getText().toString());
        mUserInfo.getDescription().setVisibility(mSpinnerDescriptionVisibility.getSelectedItemPosition());
        mOptOutSearch = mCheckBoxOptOut.isChecked();
        mShowPrivateGroups = Visibility.getValue(mSpinnerPrivateGroupsVisibility.getSelectedItemPosition());

        UserController.updateUserProfile(new ExtendedTaskDelegateAdapter<Void, UserInfo>(){
            @Override
            public void taskDidFinish(ExtendedTask task, UserInfo userInfo) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                Toast.makeText(EditProfileActivity.this, "Profilinformationen wurden geändert", Toast.LENGTH_LONG).show();
                Intent profile_intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                profile_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                profile_intent.putExtra("email", mUserInfo.getEmail());
                startActivity(profile_intent);
                finish();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_LONG).show();
                super.taskFailed(task, message);
            }
        }, mUserInfo, mOptOutSearch, mShowPrivateGroups);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                sendData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
