package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.Switch;
import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.BooleanWrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.model.UserGroupType;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.ImageUtil;

public class CreateUserGroupActivity extends BaseActivity {
    private static final int SELECT_PHOTO = 1;
    private static final int PICTURE_CROP = 2;
    // Attribute für das Laden von Bildern
    private File tempFile;
    private Uri pictureUri;
    private Bitmap mBitmap;
    private BlobKey mGroupPictureBlobKey;
    private String mBlobKeyString = "nothingToDelete";

    // Attribute zum speichern von getesteten Gruppennamen
    String mTestName;
    private List<String> mTestedPositiveNames;
    private List<String> mTestedNegativeNames;

    // Die EditText Views
    private EditText mGroupNameEditText;
    private EditText mGroupDescriptionEditText;
    private EditText mGroupPasswordEditText;
    private EditText mGroupPasswordAgainEditText;


    // Die TextViews
    private TextView mCheckGroupNameTextView;
    private TextView mCheckPasswordTextView;
    private TextView mCheckImageUploadTextView;
    private TextView mInformUserTextView;

    // Die Buttons
    private ButtonFlat mOpenGroupButton;
    private ButtonFlat mPrivateGroupButton;
    private ImageView mInfoButton;
    private ButtonFlat mCheckNameButton;
    private ButtonFlat mUploadButton;
    private ButtonFlat mPreviewButton;
    private ButtonFlat mCancelButton;
    private ButtonFlat mAcceptButton;

    // Die View zum Anzeigen von Passwörtern
    private View mViewGroup;
    // Die View zum Anzeigen der Öffentlichekitseinstellung
    private View mViewGroupPrivacy;
    // Der Switch zum Anzeigen von der Passwortview
    private Switch mGroupPrivacySwitch;

    //Variablen für das Überprüfen von Eingaben
    private boolean checkGroupName;
    private boolean checkGroupDescription;
    private boolean checkGroupPassword;
    private boolean checkGroupImage;
    private boolean checkSecurityGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_create_user_group);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        // Initialisiere die EditTexte
        mGroupNameEditText = (EditText) findViewById(R.id.create_user_group_group_name);
        mGroupDescriptionEditText = (EditText) findViewById(R.id.create_user_group_group_description);
        mGroupPasswordEditText = (EditText) findViewById(R.id.create_user_group_group_password);
        mGroupPasswordAgainEditText = (EditText) findViewById(R.id.create_user_group_group_password_again);

        // Initialisiere die TextViews
        mCheckGroupNameTextView = (TextView) findViewById(R.id.create_user_group_check_name_text_view);
        mCheckPasswordTextView = (TextView) findViewById(R.id.create_user_group_check_password_text_view);
        mCheckImageUploadTextView = (TextView) findViewById(R.id.create_user_group_upload_text_view);
        mInformUserTextView = (TextView) findViewById(R.id.crea_user_group_inform_user_text_view);

        // Initialisiere die Buttons
        mOpenGroupButton = (ButtonFlat) findViewById(R.id.create_user_group_button_normal_group);
        mPrivateGroupButton = (ButtonFlat) findViewById(R.id.create_user_group_button_security_group);
        mInfoButton = (ImageView) findViewById(R.id.create_user_group_info_image_view);
        mCheckNameButton = (ButtonFlat) findViewById(R.id.create_user_group_button_check_name);
        mUploadButton = (ButtonFlat) findViewById(R.id.create_user_group_button_upload);
        mPreviewButton = (ButtonFlat) findViewById(R.id.create_user_group_button_preview);
        mCancelButton = (ButtonFlat) findViewById(R.id.create_user_group_button_abort);
        mAcceptButton = (ButtonFlat) findViewById(R.id.create_user_group_button_submit);

        mViewGroup = findViewById(R.id.create_user_group_password_view);
        mViewGroupPrivacy = findViewById(R.id.privacy_switch_part);
        mGroupPrivacySwitch = (Switch) findViewById(R.id.create_user_group_switchView);


        mCheckImageUploadTextView.setVisibility(View.GONE);
        mCheckPasswordTextView.setVisibility(View.GONE);
        mInformUserTextView.setVisibility(View.GONE);
        mPreviewButton.setVisibility(View.GONE);

        // Positiv und negativ geprüfte Namen
        mTestedPositiveNames = new ArrayList<>();
        mTestedNegativeNames = new ArrayList<>();

        // Beschreibung muss nicht angegeben werden.
        checkGroupDescription = false;
        checkGroupPassword = false;
        checkGroupName = false;
        checkSecurityGroup = false;
        setUpButtonListener();
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

    /**
     * Setzt die Listener auf die Buttons und die Views.
     */
    private void setUpButtonListener() {
        mGroupPrivacySwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked == true) {
                    mViewGroup.setVisibility(View.VISIBLE);
                } else {
                    mViewGroup.setVisibility(View.GONE);
                    checkGroupPassword = false;
                }
            }
        });

        /**
         *
         * Falls die View zum einstellen der Öffentlichkeit der Gruppe unsichtbar geamcht wurde,
         * dann macht ein Click auf diesen Button dies rückgängig.
         */
        mOpenGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewGroupPrivacy.getVisibility() != View.VISIBLE) {
                    mViewGroupPrivacy.setVisibility(View.VISIBLE);
                    if (!mGroupPrivacySwitch.isCheck() && mViewGroup.getVisibility() == View.VISIBLE) {
                        mViewGroup.setVisibility(View.GONE);
                    }
                    checkSecurityGroup = false;
                }
            }
        });

        /**
         * Falls die View zum Einstellen der Öffentlichkeit der Gruppe sichtbar ist, wird sie mit
         * einem Click auf diesen Button unsichtbar gemacht. Damit wird sicher gestellt, dass
         * Securitygroups immmer private Gruppen sind.
         */
        mPrivateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewGroupPrivacy.getVisibility() != View.GONE) {
                    mViewGroupPrivacy.setVisibility(View.GONE);
                    // Falls die View mit den Passworteingaben nicht sichtbar ist, diese dann sichtbar machen.
                    if (mViewGroup.getVisibility() == View.GONE) {
                        mViewGroup.setVisibility(View.VISIBLE);
                    }
                    checkSecurityGroup = true;
                }
            }
        });
        mInfoButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_big_question));
        mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertadd = new AlertDialog.Builder(CreateUserGroupActivity.this);
                LayoutInflater factory = LayoutInflater.from(CreateUserGroupActivity.this);
                final View infoView = factory.inflate(R.layout.security_group_info_view, null);
                alertadd.setView(infoView);
                alertadd.setMessage(R.string.typeInfoGroup);
                alertadd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertadd.show();
            }
        });

        /**
         * Testet den Namen der Gruppe, der eingegeben wurde. Falls keiner angegeben wurde passiert
         * nichts und falls ein Name bereits gepüft worden ist und nicht gültig war wird dieser in die
         * Liste der geprüften namen eingetragen.
         *
         */
        mCheckNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInformUserTextView.setVisibility(View.GONE);
                mTestName = mGroupNameEditText.getText().toString();
                if (!mTestName.isEmpty()) {
                    if (!mTestedPositiveNames.contains(mTestName) && !mTestedNegativeNames.contains(mTestName)) {
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        GroupController.getInstance().checkGroupName(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, BooleanWrapper booleanWrapper) {
                                if (booleanWrapper.getValue()) {
                                    mCheckGroupNameTextView.setText(R.string.checkGroupNamePositive);
                                    mCheckGroupNameTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_positive));
                                    mTestedPositiveNames.add(mTestName);
                                    checkGroupName = true;
                                } else {
                                    mCheckGroupNameTextView.setText(R.string.checkGroupNameNegative);
                                    mCheckGroupNameTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_negative));
                                    mTestedNegativeNames.add(mTestName);
                                    checkGroupName = false;
                                }
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            }
                        }, mTestName);
                    } else if (mTestedPositiveNames.contains(mTestName)) {
                        mCheckGroupNameTextView.setText(R.string.checkGroupNamePositive);
                        mCheckGroupNameTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_positive));
                        checkGroupName = true;
                    } else {
                        mCheckGroupNameTextView.setText(R.string.checkGroupNameNegative);
                        mCheckGroupNameTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_negative));
                        checkGroupName = false;
                    }
                }
            }
        });

        // EditText für den Namen der Gruppe
        mGroupNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mGroupNameEditText.getText().toString().length() > 2) {
                    mCheckGroupNameTextView.setText(R.string.checkGroupNameString);
                    mCheckGroupNameTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name));
                } else {
                    mCheckGroupNameTextView.setText(R.string.groupNameTooShort);
                    mCheckGroupNameTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_negative));
                }
                checkGroupName = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // EditText für das Passwort
        mGroupPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mGroupPasswordEditText.getText().toString().length() > 4) {
                    if (mGroupPasswordEditText.getText().toString().equals(mGroupPasswordAgainEditText.getText().toString())) {
                        mCheckPasswordTextView.setText(R.string.checkPasswordPositive);
                        mCheckPasswordTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_positive));
                        checkGroupPassword = true;
                    } else {
                        mCheckPasswordTextView.setText(R.string.checkPasswordNegative);
                        mCheckPasswordTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_negative));
                        checkGroupPassword = false;
                    }
                } else {
                    mCheckPasswordTextView.setText(R.string.passwordTooShort);
                    mCheckPasswordTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_negative));
                    checkGroupPassword = false;
                }

                mCheckPasswordTextView.setVisibility(View.VISIBLE);
            }
        });

        // EditText für das wiederholte Passwort
        mGroupPasswordAgainEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mGroupPasswordEditText.getText().toString().length() > 4) {
                    if (mGroupPasswordEditText.getText().toString().equals(mGroupPasswordAgainEditText.getText().toString())) {
                        mCheckPasswordTextView.setText(R.string.checkPasswordPositive);
                        mCheckPasswordTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_positive));
                        checkGroupPassword = true;
                    } else {
                        mCheckPasswordTextView.setText(R.string.checkPasswordNegative);
                        mCheckPasswordTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_negative));
                        checkGroupPassword = false;
                    }
                } else {
                    mCheckPasswordTextView.setText(R.string.passwordTooShort);
                    mCheckPasswordTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_negative));
                    checkGroupPassword = false;
                }

                mCheckPasswordTextView.setVisibility(View.VISIBLE);
            }
        });

        // Button zum uploaden eines Bildes
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picturePickerIntent = new Intent(Intent.ACTION_PICK);
                picturePickerIntent.setType("image/*");
                startActivityForResult(picturePickerIntent, SELECT_PHOTO);
            }
        });

        // Button zum anschauen des hochgeladenen Bildes, ist nicht sichtbar solange kein Bild
        // hochgeladen ist.
        mPreviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProgressBarIndeterminateVisibility(Boolean.TRUE);
                GroupController.getInstance().loadImageForPreview(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                        setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        if (bitmap != null) {
                            AlertDialog.Builder alertadd = new AlertDialog.Builder(CreateUserGroupActivity.this);
                            LayoutInflater factory = LayoutInflater.from(CreateUserGroupActivity.this);
                            final View view = factory.inflate(R.layout.preview_image, null);
                            ImageView previewList = (ImageView) view.findViewById(R.id.preview_image_list);
                            ImageView previewProfile = (ImageView) view.findViewById(R.id.preview_image_profile);
                            previewList.setImageBitmap(ImageUtil.getRoundedBitmap(bitmap));
                            previewProfile.setImageBitmap(ImageUtil.getRoundedBitmap(bitmap));
                            alertadd.setView(view);
                            alertadd.setMessage(R.string.previewImageAlertDialogTitle);
                            alertadd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertadd.show();
                        }
                    }
                }, mGroupPictureBlobKey);
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Je nach dem welche Daten angegeben sind wird hier eine neue Nutzergruppe erstellt
        // und nach dem Erstellen das Profil dieser geladen.
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prüft den namen
                mInformUserTextView.setVisibility(View.GONE);
                if (!checkGroupName) {
                    mInformUserTextView.setText(R.string.checkGroupNamePlease);
                    mInformUserTextView.setVisibility(View.VISIBLE);
                    return;
                }

                // Setze den Gruppentyp
                UserGroupType type;
                if (checkSecurityGroup) {
                    type = UserGroupType.SECURITYGROUP;
                } else {
                    type = UserGroupType.NORMALGROUP;
                }

                // Setze die Öffentlichkeit
                boolean privacy;
                if (type == UserGroupType.SECURITYGROUP) {
                    privacy = true;
                } else if (type == UserGroupType.NORMALGROUP && checkGroupPassword) {
                    privacy = true;
                } else {
                    privacy = false;
                }

                // Teste Bedingungen für das Erstellen von Gruppen
                if (checkSecurityGroup) {
                    if (!checkGroupPassword) {
                        mInformUserTextView.setText(R.string.check_your_password);
                        mInformUserTextView.setVisibility(View.VISIBLE);
                        return;
                    }
                    // Weitere Bedingungen testen, falls sich welche ergeben
                }
                if(privacy){
                    if(!checkGroupPassword){
                        mInformUserTextView.setText(R.string.check_your_password);
                        mInformUserTextView.setVisibility(View.VISIBLE);
                        return;
                    }
                    // Weitere Bedingungen testen, falls sich welche ergeben
                }

                // Setze die Beschreibung
                String description;
                if (mGroupDescriptionEditText.getText().toString().length() > 0) {
                    description = mGroupDescriptionEditText.getText().toString();
                } else {
                    description = getString(R.string.defaultGroupDestription) +" "+mTestName;
                }

                // Setze das Passwort
                String password = null;
                if (checkGroupPassword) {
                    password = mGroupPasswordAgainEditText.getText().toString();
                }

                // Prüft ob eine Securitygruppe erstellt werden soll
                setProgressBarIndeterminateVisibility(Boolean.TRUE);
                mAcceptButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.GONE);
                mPreviewButton.setVisibility(View.GONE);
                mUploadButton.setVisibility(View.GONE);
                GroupController.getInstance().createUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Void aVoid) {
                        setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        startGroupProfile(mTestName);
                    }

                    @Override
                    public void taskFailed(ExtendedTask task, String message) {
                        setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        mAcceptButton.setVisibility(View.VISIBLE);
                        mCancelButton.setVisibility(View.VISIBLE);
                        mPreviewButton.setVisibility(View.VISIBLE);
                        mUploadButton.setVisibility(View.VISIBLE);
                        Toast.makeText(CreateUserGroupActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }, mTestName, type, password, privacy, description, mBlobKeyString);
            }

        });
    }

    /**
     * Diese Methode wird aufgerufen, wenn der Benutzer den Uploadbutton drückt, ein Bild ausgewählt
     * und dieses zugeschnitten hat. Hier wird das Bild aus den Extras geladen, in eine Bitmap
     * umgewandelt und über den GroupController an den Server geschickt.
     *
     * @param requestCode
     * @param resultCode
     * @param imageReturnedIntent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri tempUriSelect = createTempFile();
                    pictureUri = imageReturnedIntent.getData();
                    ImageUtil.performCrop(pictureUri, this, PICTURE_CROP, tempUriSelect);
                }
                break;
            case PICTURE_CROP:
                Bundle extras = imageReturnedIntent.getExtras();
                mBitmap = extras.getParcelable("data");
                tempFile.deleteOnExit();
                if (mBitmap != null) {
                    mCheckImageUploadTextView.setVisibility(View.VISIBLE);
                    if (mPreviewButton.getVisibility() == View.VISIBLE) {
                        mPreviewButton.setVisibility(View.GONE);
                        mCheckImageUploadTextView.setText(R.string.uploadingImage);
                    }
                    // Bild hochladen
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    mUploadButton.setVisibility(View.GONE);
                    mAcceptButton.setVisibility(View.GONE);
                    mCancelButton.setVisibility(View.GONE);
                    GroupController.getInstance().uploadImageForPreview(new ExtendedTaskDelegateAdapter<Void, BlobKey>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, BlobKey blobKey) {
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            mPreviewButton.setVisibility(View.VISIBLE);
                            mUploadButton.setVisibility(View.VISIBLE);
                            mAcceptButton.setVisibility(View.VISIBLE);
                            mCancelButton.setVisibility(View.VISIBLE);
                            mCheckImageUploadTextView.setText(R.string.uploadingDone);
                            mGroupPictureBlobKey = blobKey;
                            mBlobKeyString = blobKey.getKeyString();
                            checkGroupImage = true;
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            mCheckImageUploadTextView.setText(R.string.uploadingImageFailed);
                            checkGroupImage = false;
                            mUploadButton.setVisibility(View.VISIBLE);
                            mAcceptButton.setVisibility(View.VISIBLE);
                            mCancelButton.setVisibility(View.VISIBLE);
                        }
                    }, ImageUtil.BitmapToInputStream(mBitmap), mBlobKeyString);
                }
                break;
        }
    }

    /**
     * Erstellt eine temporäre Datei für das zugeschnittene Bild
     *
     * @return
     */
    private Uri createTempFile() {
        try {
            tempFile = File.createTempFile("crop", ".png", Environment
                    .getExternalStorageDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(tempFile);
    }

    /**
     * Startet das Profil zu der angegebenen Gruppe. Hier muss der Name der Gruppe mit übergeben werden
     * denn das Gruppenprofil benötigt den Namen um die Gruppe vom Server zu laden.
     *
     * @param groupName Der Name der Gruppe
     */
    private void startGroupProfile(String groupName) {
        Intent start_group_profile_intent = new Intent(this, GroupProfileActivity.class);
        start_group_profile_intent.putExtra("groupName", groupName);
        startActivity(start_group_profile_intent);
        finish();
    }
}
