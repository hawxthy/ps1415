package ws1415.ps1415.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.InfoPair;
import com.skatenight.skatenightAPI.model.Text;
import com.skatenight.skatenightAPI.model.UserInfo;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import ws1415.common.controller.UserController;
import ws1415.common.model.Visibility;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.util.ImageUtil;
import ws1415.ps1415.R;

public class EditProfileActivity extends Activity {
    private static final int REQUEST_CAMERA_CAPTURE = 101;
    private static final int REQUEST_PICTURE_CROP = 102;
    private static final int REQUEST_SELECT_PICTURE = 200;

    private UserInfo mUserInfo;
    private Bitmap mImage;
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

    Calendar DateOfBirthCal = Calendar.getInstance();
    private boolean dateSet;
    private boolean normalClose;

    private boolean changedPicture;
    private Uri pictureUri;
    private File tempFile;

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
        if(mImage != null) {
            mImageViewPicture.setImageBitmap(ImageUtil.getRoundedBitmapFramed(mImage));
        }
        else {
            Bitmap bm = BitmapFactory.decodeResource(getResources(),
                    R.drawable.default_picture);
            mImageViewPicture.setImageBitmap(ImageUtil.getRoundedBitmapFramed(bm));
        }

        setUpDateOfBirth();

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void handleIntent(Intent intent) {
        String userPicture = intent.getStringExtra("userPicture");
        if (userPicture != null) {
            mImage = ImageUtil.DecodeTextToBitmap(new Text().setValue(intent.getStringExtra("userPicture")));
        }
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

    public void setUpImageEdit(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setTitle(R.string.profile_picture)
                .setItems(R.array.image_selection_edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent picturePickerIntent = new Intent(Intent.ACTION_PICK);
                                picturePickerIntent.setType("image/*");
                                startActivityForResult(picturePickerIntent, REQUEST_SELECT_PICTURE);
                                break;
                            case 1:
                                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(captureIntent, REQUEST_CAMERA_CAPTURE);
                                break;
                            case 2:
                                mImage = null;
                                Bitmap bm = BitmapFactory.decodeResource(getResources(),
                                        R.drawable.default_picture);
                                mImageViewPicture.setImageBitmap(ImageUtil.getRoundedBitmapFramed(bm));
                                changedPicture = true;
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_PICTURE:
                    Uri tempUriSelect = createTempFile();
                    pictureUri = data.getData();
                    ImageUtil.performCrop(pictureUri, this, REQUEST_PICTURE_CROP, tempUriSelect);
                    break;
                case REQUEST_CAMERA_CAPTURE:
                    Uri tempUriCamera = createTempFile();
                    pictureUri = data.getData();
                    ImageUtil.performCrop(pictureUri, this, REQUEST_PICTURE_CROP, tempUriCamera);
                    break;
                case REQUEST_PICTURE_CROP:
                    Bundle extras = data.getExtras();
                    mImage = extras.getParcelable("data");
                    mImageViewPicture.setImageBitmap(ImageUtil.getRoundedBitmapFramed(mImage));
                    changedPicture = true;
                    tempFile.delete();
                    break;
            }
        }
    }

    private Uri createTempFile(){
        try {
            tempFile = File.createTempFile("crop", "png", Environment
                    .getExternalStorageDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(tempFile);
    }

    private void setUpDateOfBirth() {
        try {
            if(mUserInfo.getDateOfBirth().getValue() != null) {
                Date date = ProfileActivity.DATE_OF_BIRTH_FORMAT.parse(mUserInfo.getDateOfBirth().getValue());
                DateOfBirthCal.setTime(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final OnDateSetListener date = new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                if (dateSet) {
                    DateOfBirthCal.set(Calendar.YEAR, year);
                    DateOfBirthCal.set(Calendar.MONTH, month);
                    DateOfBirthCal.set(Calendar.DAY_OF_MONTH, day);
                    mEditTextDateOfBirth.setText(ProfileActivity.DATE_OF_BIRTH_FORMAT.
                            format(DateOfBirthCal.getTime()));
                }
            }
        };

        mEditTextDateOfBirth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(EditProfileActivity.this, date, DateOfBirthCal
                        .get(Calendar.YEAR), DateOfBirthCal.get(Calendar.MONTH),
                        DateOfBirthCal.get(Calendar.DAY_OF_MONTH)) {
                    @Override
                    public void dismiss() {
                        if(!normalClose) dateSet = false;
                        super.dismiss();
                    }
                };
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dateSet = true;
                                normalClose = true;
                            }
                        });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dateSet = false;
                                normalClose = true;
                            }
                        });
                dialog.show();
            }
        });
    }

    private void sendData() {
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

        UserController.updateUserProfile(new ExtendedTaskDelegateAdapter<Void, UserInfo>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserInfo userInfo) {
                if(!changedPicture) {
                    editDone();
                } else {
                    UserController.updateUserPicture(new ExtendedTaskDelegateAdapter<Void, Void>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, Void aVoid) {
                            editDone();
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            editFailed(message);
                            super.taskFailed(task, message);
                        }
                    }, mUserInfo.getEmail(), mImage);
                }
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                editFailed(message);
                super.taskFailed(task, message);
            }
        }, mUserInfo, mOptOutSearch, mShowPrivateGroups);

    }

    private void editFailed(String message) {
        setProgressBarIndeterminateVisibility(Boolean.FALSE);
        Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void editDone() {
        setProgressBarIndeterminateVisibility(Boolean.FALSE);
        Toast.makeText(EditProfileActivity.this, "Profilinformationen wurden geändert", Toast.LENGTH_LONG).show();
        Intent profile_intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        profile_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        profile_intent.putExtra("email", mUserInfo.getEmail());
        startActivity(profile_intent);
        finish();
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