package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.Switch;
import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.BooleanWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.model.NavDrawerGroupList;
import ws1415.ps1415.model.UserGroupType;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.ImageUtil;

public class CreateUserGroupActivity extends BaseActivity {
    private static final int SELECT_PHOTO = 1;
    private BlobKey mGroupPictureBlobKey;
    String mTestName;
    private List<String> mTestedPositiveNames;
    private List<String> mTestedNegativeNames;
    private String mBlobKeyString = "nothingToDelete";

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


    private ButtonFlat mCheckNameButton;
    private ButtonFlat mUploadButton;
    private ButtonFlat mPreviewButton;
    private ButtonFlat mCancelButton;
    private ButtonFlat mAcceptButton;

    // Die View zum Anzeigen von Passwörtern
    private View mViewGroup;
    // Der Switch zum Anzeigen von der Passwortview
    private Switch mGroupPrivacySwitch;

    //Variablen für das Überprüfen von Eingaben
    private boolean checkGroupName;
    private boolean checkGroupDescription;
    private boolean checkGroupPassword;
    private boolean checkGroupImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(NavDrawerGroupList.items, R.layout.activity_create_user_group);

        mGroupNameEditText = (EditText) findViewById(R.id.create_user_group_group_name);
        mGroupDescriptionEditText = (EditText) findViewById(R.id.create_user_group_group_description);
        mGroupPasswordEditText = (EditText) findViewById(R.id.create_user_group_group_password);
        mGroupPasswordAgainEditText = (EditText) findViewById(R.id.create_user_group_group_password_again);

        mCheckGroupNameTextView = (TextView) findViewById(R.id.create_user_group_check_name_text_view);
        mCheckPasswordTextView = (TextView) findViewById(R.id.create_user_group_check_password_text_view);
        mCheckImageUploadTextView = (TextView) findViewById(R.id.create_user_group_upload_text_view);
        mInformUserTextView = (TextView)findViewById(R.id.crea_user_group_inform_user_text_view);


        mCheckNameButton = (ButtonFlat) findViewById(R.id.create_user_group_button_check_name);
        mUploadButton = (ButtonFlat) findViewById(R.id.create_user_group_button_upload);
        mPreviewButton = (ButtonFlat) findViewById(R.id.create_user_group_button_preview);
        mCancelButton = (ButtonFlat) findViewById(R.id.create_user_group_button_abort);
        mAcceptButton = (ButtonFlat) findViewById(R.id.create_user_group_button_submit);

        mViewGroup = findViewById(R.id.create_user_group_password_view);
        mGroupPrivacySwitch = (Switch) findViewById(R.id.create_user_group_switchView);

        mCheckImageUploadTextView.setVisibility(View.GONE);
        mCheckPasswordTextView.setVisibility(View.GONE);
        mInformUserTextView.setVisibility(View.GONE);
        mPreviewButton.setVisibility(View.GONE);

        mTestedPositiveNames = new ArrayList<>();
        mTestedNegativeNames = new ArrayList<>();
        // Beschreibung muss nicht angegeben werden.
        checkGroupDescription = false;
        checkGroupImage = false;
        checkGroupPassword = false;
        checkGroupName = false;
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

    private void setUpButotnListener() {
        mGroupPrivacySwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked == true) {
                    mViewGroup.setVisibility(View.VISIBLE);
                } else {
                    mViewGroup.setVisibility(View.GONE);
                }
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

        mGroupNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCheckGroupNameTextView.setText(R.string.checkGroupNameString);
                mCheckGroupNameTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name));

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mTestName = null;
            }
        });

        mGroupPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mGroupPasswordEditText.getText().toString().equals(mGroupPasswordAgainEditText.getText().toString())) {
                    mCheckPasswordTextView.setText(R.string.checkPasswordPositive);
                    mCheckPasswordTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_positive));
                    checkGroupPassword = true;
                } else {
                    mCheckPasswordTextView.setText(R.string.checkPasswordNegative);
                    mCheckPasswordTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_negative));
                    checkGroupPassword = false;
                }
                mCheckPasswordTextView.setVisibility(View.VISIBLE);
            }
        });

        mGroupPasswordAgainEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mGroupPasswordEditText.getText().toString().equals(mGroupPasswordAgainEditText.getText().toString())) {
                    mCheckPasswordTextView.setText(R.string.checkPasswordPositive);
                    mCheckPasswordTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_positive));
                    checkGroupPassword = true;
                } else {
                    mCheckPasswordTextView.setText(R.string.checkPasswordNegative);
                    mCheckPasswordTextView.setTextColor(CreateUserGroupActivity.this.getResources().getColor(R.color.check_group_name_negative));
                    checkGroupPassword = false;
                }
                mCheckPasswordTextView.setVisibility(View.VISIBLE);
            }
        });

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra("crop", "true");
                photoPickerIntent.putExtra("return-data", true);
                photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        mPreviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProgressBarIndeterminateVisibility(Boolean.TRUE);
                GroupController.getInstance().loadImageForPreview(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                        if (bitmap != null) {
                            AlertDialog.Builder altertadd = new AlertDialog.Builder(CreateUserGroupActivity.this);
                            LayoutInflater factory = LayoutInflater.from(CreateUserGroupActivity.this);
                            final View view = factory.inflate(R.layout.preview_image, null);
                            ImageView previewList = (ImageView) view.findViewById(R.id.preview_image_list);
                            ImageView previewProfile = (ImageView) view.findViewById(R.id.preview_image_profile);
                            previewList.setImageBitmap(bitmap);
                            previewProfile.setImageBitmap(bitmap);
                            altertadd.setView(view);
                            altertadd.setMessage(R.string.previewImageAlertDialogTitle);
                            altertadd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            altertadd.show();
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

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInformUserTextView.setVisibility(View.GONE);
                final String groupName = mTestName;
                if(groupName == null){
                    mInformUserTextView.setText(R.string.checkGroupNamePlease);
                    mInformUserTextView.setVisibility(View.VISIBLE);
                    return;
                }
                String password = mGroupPasswordAgainEditText.getText().toString();
                String description;
                if(checkGroupDescription){
                    description = mGroupDescriptionEditText.getText().toString();
                }else{
                    description = getString(R.string.defaultGroupDestription)+groupName;
                }
                if (checkGroupName && !mGroupPrivacySwitch.isCheck()) {
                    if(checkGroupImage){
                        GroupController.getInstance().createOpenUserGroupWithPicture(new ExtendedTaskDelegateAdapter<Void, Void>(){
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                startGroupProfile(groupName);
                            }

                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                mInformUserTextView.setText(R.string.creatingGroupError);
                                mInformUserTextView.setVisibility(View.VISIBLE);
                            }
                        },groupName, description, mBlobKeyString);
                    }else{
                        GroupController.getInstance().createOpenUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>(){
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                startGroupProfile(groupName);
                            }

                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                mInformUserTextView.setText(R.string.creatingGroupError);
                                mInformUserTextView.setVisibility(View.VISIBLE);
                            }
                        }, groupName, description);
                    }
                }else if(checkGroupName && mGroupPrivacySwitch.isCheck()){
                    if(checkGroupPassword){
                        if(checkGroupImage){
                            GroupController.getInstance().createPrivateUserGroupWithPicture(new ExtendedTaskDelegateAdapter<Void, Void>() {
                                @Override
                                public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                    startGroupProfile(groupName);
                                }

                                @Override
                                public void taskFailed(ExtendedTask task, String message) {
                                    mInformUserTextView.setText(R.string.creatingGroupError);
                                    mInformUserTextView.setVisibility(View.VISIBLE);
                                }
                            }, groupName, UserGroupType.NORMALGROUP, mGroupPasswordAgainEditText.getText().toString(),description, mBlobKeyString);
                        }else{
                            GroupController.getInstance().createPrivateUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
                                @Override
                                public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                    startGroupProfile(groupName);
                                }

                                @Override
                                public void taskFailed(ExtendedTask task, String message) {
                                    mInformUserTextView.setText(R.string.creatingGroupError);
                                    mInformUserTextView.setVisibility(View.VISIBLE);
                                }
                            }, groupName, UserGroupType.NORMALGROUP, description, mGroupPasswordAgainEditText.getText().toString());
                        }
                    }
                }
                mInformUserTextView.setVisibility(View.VISIBLE);
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
                    InputStream is = null;
                    try {
                        Bundle extras = imageReturnedIntent.getExtras();
                        Bitmap cropedBitmap = extras.getParcelable("data");

                        if (cropedBitmap != null) {
                            mCheckImageUploadTextView.setVisibility(View.VISIBLE);
                            setProgressBarIndeterminateVisibility(Boolean.TRUE);
                            if (mPreviewButton.getVisibility() == View.VISIBLE) {
                                mPreviewButton.setVisibility(View.GONE);
                                mCheckImageUploadTextView.setText(R.string.uploadingImage);
                            }

                            GroupController.getInstance().uploadImageForPreview(new ExtendedTaskDelegateAdapter<Void, BlobKey>() {
                                @Override
                                public void taskDidFinish(ExtendedTask task, BlobKey blobKey) {
                                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                    mPreviewButton.setVisibility(View.VISIBLE);
                                    mCheckImageUploadTextView.setText(R.string.uploadingDone);
                                    mGroupPictureBlobKey = blobKey;
                                    mBlobKeyString = blobKey.getKeyString();
                                    checkGroupImage = true;
                                }

                                @Override
                                public void taskFailed(ExtendedTask task, String message) {
                                    mCheckImageUploadTextView.setText(R.string.uploadingImageFailed);
                                    checkGroupImage = false;
                                }
                            }, ImageUtil.BitmapToInputStream(cropedBitmap), mBlobKeyString);
                        }
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        }
    }

    /**
     * Startet das Profil zu der angegebenen Gruppe.
     *
     * @param groupName Der Name der Gruppe
     */
    private void startGroupProfile(String groupName){
        Intent start_group_profile_intent = new Intent(this, GroupProfileActivity.class);
        start_group_profile_intent.putExtra("groupName", groupName);
        startActivity(start_group_profile_intent);
        finish();
    }
}
