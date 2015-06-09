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

import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserProfileEdit;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.model.Gender;
import ws1415.ps1415.model.Visibility;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.ImageUtil;
import ws1415.ps1415.util.UniversalUtil;
import ws1415.ps1415.util.UserImageLoader;

/**
 * Diese Activity wird zum Editieren eines Benutzerprofils genutzt. Dabei bekommt dieser über einen
 * Intent die Informationen zu dem Benutzer.
 *
 * @author Martin Wrodarczyk
 */
public class EditProfileActivity extends Activity {
    // RequestCodes für die ActivityOnResult-Methode
    private static final int REQUEST_CAMERA_CAPTURE = 101;
    private static final int REQUEST_PICTURE_CROP = 102;
    private static final int REQUEST_SELECT_PICTURE = 200;

    // Zum Speichern der Daten, die über den Intent übergeben worden sind
    private String mEmail;
    private UserProfileEdit mUserProfile;
    private Bitmap mImage;

    // UI Komponenten
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

    // Felder, die für den DatePicker für das Geburtsdatum genutzt werden
    Calendar DateOfBirthCal = Calendar.getInstance();
    private boolean dateSet;
    private boolean normalClose;

    // Felder für das Verwalten einer Profilbildänderung
    private boolean changedPicture;
    private Uri pictureUri;
    private File tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit_profile);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        // Intent verarbeiten
        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");

        // UI-Komponenten initialisieren
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

        // Standard-Bild vorerst laden
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_picture);
        mImageViewPicture.setImageBitmap(ImageUtil.getRoundedBitmapFramed(bm));

        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        UserController.getUserProfileEdit(new ExtendedTaskDelegateAdapter<Void, UserProfileEdit>(){
            @Override
            public void taskDidFinish(ExtendedTask task, UserProfileEdit userProfileEdit) {
                mUserProfile = userProfileEdit;
                setUpDateOfBirth(userProfileEdit.getUserInfo());
                setUpViews(userProfileEdit);
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                UniversalUtil.showToast(EditProfileActivity.this, message);
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
        }, mEmail);

        // Up-Navigation aktivieren
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /**
     * Setzt die geladenen Informationen in die Views.
     */
    private void setUpViews(UserProfileEdit userProfileEdit) {
        // UI-Komponenten mit den Daten des Intents füllen
        UserInfo userInfo = userProfileEdit.getUserInfo();
        mEditTextFirstName.setText(userProfileEdit.getUserInfo().getFirstName());
        mEditTextLastName.setText(userInfo.getLastName().getValue());
        mSpinnerLastNameVisibility.setSelection(Visibility.valueOf(userInfo.getLastName().getVisibility()).getId());
        mSpinnerGender.setSelection(Gender.valueOf(userInfo.getGender()).getId());
        mEditTextDateOfBirth.setText(userInfo.getDateOfBirth().getValue());
        mSpinnerDateOfBirthVisibility.setSelection(Visibility.valueOf(userInfo.getDateOfBirth().getVisibility()).getId());
        mEditTextCity.setText(userInfo.getCity().getValue());
        mSpinnerCityVisibility.setSelection(Visibility.valueOf(userInfo.getCity().getVisibility()).getId());
        mEditTextPlz.setText(userInfo.getPostalCode().getValue());
        mSpinnerPlzVisibility.setSelection(Visibility.valueOf(userInfo.getPostalCode().getVisibility()).getId());
        mEditTextDescription.setText(userInfo.getDescription().getValue());
        mSpinnerDescriptionVisibility.setSelection(Visibility.valueOf(userInfo.getDescription().getVisibility()).getId());
        mCheckBoxOptOut.setChecked(userProfileEdit.getOptOutSearch());
        mSpinnerPrivateGroupsVisibility.setSelection(Visibility.valueOf(userProfileEdit.getShowPrivateGroups()).getId());
        UserImageLoader.getInstance(this).displayImageFramed(userProfileEdit.getUserPicture(), mImageViewPicture);
    }

    /**
     * Beim Drücken des Profilbildes wird hier das Dialogauswahlfenster erstellt, bei dem
     * man wählen kann, ob man ein Bild aus dem Album wählen möchte, ein Bild aufnehmen will oder
     * sein Profilbild löschen möchte. Dementsprechend werden dann die Activities gestartet.
     *
     * @param view
     */
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
                                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_picture);
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

    // Erstellt temporäre Datei um das gecroppte Bild zwischenzuspeichern
    private Uri createTempFile(){
        try {
            tempFile = File.createTempFile("crop", ".png", Environment
                    .getExternalStorageDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(tempFile);
    }

    // Initialisiert das Geburtsdatum-Feld und setzt den Listener für den DatePicker
    private void setUpDateOfBirth(UserInfo userInfo) {
        try {
            if(userInfo.getDateOfBirth().getValue() != null) {
                Date date = ProfileActivity.DATE_OF_BIRTH_FORMAT.parse(userInfo.getDateOfBirth().getValue());
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

    /**
     * Verarbeitet die eingegebenen Daten, prüft ob die Eingaben korrekt sind und updated das
     * Profil mit diesen Daten.
     */
    private void sendData() {
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        mUserProfile.getUserInfo().setFirstName(mEditTextFirstName.getText().toString());
        mUserProfile.getUserInfo().getLastName().setValue(mEditTextLastName.getText().toString());
        mUserProfile.getUserInfo().getLastName().setVisibility(Visibility.getValue(mSpinnerLastNameVisibility.getSelectedItemPosition()).name());
        mUserProfile.getUserInfo().setGender(Gender.getValue(mSpinnerGender.getSelectedItemPosition()).name());
        mUserProfile.getUserInfo().getDateOfBirth().setValue(mEditTextDateOfBirth.getText().toString());
        mUserProfile.getUserInfo().getDateOfBirth().setVisibility(Visibility.getValue(mSpinnerDateOfBirthVisibility.getSelectedItemPosition()).name());
        mUserProfile.getUserInfo().getCity().setValue(mEditTextCity.getText().toString());
        mUserProfile.getUserInfo().getCity().setVisibility(Visibility.getValue(mSpinnerCityVisibility.getSelectedItemPosition()).name());
        mUserProfile.getUserInfo().getPostalCode().setValue(mEditTextPlz.getText().toString());
        mUserProfile.getUserInfo().getPostalCode().setVisibility(Visibility.getValue(mSpinnerPlzVisibility.getSelectedItemPosition()).name());
        mUserProfile.getUserInfo().getDescription().setValue(mEditTextDescription.getText().toString());
        mUserProfile.getUserInfo().getDescription().setVisibility(Visibility.getValue(mSpinnerDescriptionVisibility.getSelectedItemPosition()).name());
        mUserProfile.setOptOutSearch(mCheckBoxOptOut.isChecked());
        mUserProfile.setShowPrivateGroups(Visibility.getValue(mSpinnerPrivateGroupsVisibility.getSelectedItemPosition()).name());

        if(mUserProfile.getUserInfo().getFirstName().length() < 3){
            Toast.makeText(this, getString(R.string.first_name_too_short), Toast.LENGTH_LONG).show();
            setProgressBarIndeterminateVisibility(Boolean.FALSE);
            return;
        } else {
            mUserProfile.setUserPicture(null);
        }
        UserController.updateUserProfile(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                if(aBoolean) {
                    if (!changedPicture) {
                        editDone();
                    } else {
                        if (mImage != null) uploadUserPicture();
                        else removeUserPicture();
                    }
                } else {
                    editFailed("Benutzerdaten konnten nicht geändert werden");
                }
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                editFailed(message);
            }
        }, mUserProfile);

    }

    /**
     * Lädt das neue Profilbild hoch.
     */
    private void uploadUserPicture() {
        UserController.uploadUserPicture(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                editDone();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                editFailed(message);
            }
        }, mUserProfile.getEmail(), ImageUtil.BitmapToInputStream(mImage));
    }

    /**
     * Löscht das Profilbild.
     */
    private void removeUserPicture(){
        UserController.removeUserPicture(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                editDone();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                editFailed(message);
            }
        }, mUserProfile.getEmail());
    }

    /**
     * Wird bei einem Fehler beim Updaten des Profils ausgeführt.
     *
     * @param message Fehlernachricht
     */
    private void editFailed(String message) {
        setProgressBarIndeterminateVisibility(Boolean.FALSE);
        Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Wird beim erfolgreichen Updaten des Profils ausgeführt.
     */
    private void editDone() {
        setProgressBarIndeterminateVisibility(Boolean.FALSE);
        Toast.makeText(EditProfileActivity.this, "Profilinformationen wurden geändert", Toast.LENGTH_LONG).show();
        Intent profile_intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        profile_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        profile_intent.putExtra("email", mUserProfile.getEmail());
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
                if(mUserProfile != null) sendData();
                else UniversalUtil.showToast(this, getString(R.string.loading_data));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
