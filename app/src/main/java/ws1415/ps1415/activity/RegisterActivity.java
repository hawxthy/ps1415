package ws1415.ps1415.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.File;
import java.io.IOException;

import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.gcm.GCMUtil;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.Constants;
import ws1415.ps1415.R;
import ws1415.ps1415.util.ImageUtil;
import ws1415.ps1415.util.LocalGCMUtil;
import ws1415.ps1415.util.PrefManager;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends Activity {
    private static final String TAG = "Skatenight";

    // RequestCodes für die ActivityOnResult-Methode
    private static final int REQUEST_CAMERA_CAPTURE = 101;
    private static final int REQUEST_PICTURE_CROP = 102;
    private static final int REQUEST_SELECT_PICTURE = 200;
    public static final int REQUEST_ACCOUNT_PICKER = 1;

    // UI-Komponenten
    private ImageView mPictureView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private Button mRegisterButton;
    private View mProgressView;
    private View mLoginFormView;

    // Für das Einloggen mit dem Google Account
    private GoogleAccountCredential credential;
    private static String accountName;

    // Für das Verwalten des Profilbildes
    private Bitmap selectedPicture;
    private Uri pictureUri;
    private File tempFile;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;

        // Credentials auswählen
        credential = GoogleAccountCredential.usingAudience(this, "server:client_id:" + Constants.WEB_CLIENT_ID);
        if(accountName == null) {
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }

        mPictureView = (ImageView) findViewById(R.id.user_picture);
        mFirstNameView = (EditText) findViewById(R.id.first_name);
        mLastNameView = (EditText) findViewById(R.id.last_name);
        mRegisterButton = (Button) findViewById(R.id.register_button);

        // Default-Bild setzen
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_picture);
        mPictureView.setImageBitmap(ImageUtil.getRoundedBitmapFramed(bm));

        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }

    /**
     * Versucht einen Benutzer zu erstellen.
     */
    public void attemptRegister() {
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        boolean correctParameters = true;

        if(firstName.length() < 3){
            correctParameters = false;
            Toast.makeText(RegisterActivity.this, getString(R.string.first_name_too_short), Toast.LENGTH_LONG).show();
        }

        if(accountName == null || accountName.equals("")){
            correctParameters = false;
            Toast.makeText(RegisterActivity.this, getString(R.string.email_not_chosed), Toast.LENGTH_LONG).show();
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }

        if(correctParameters) {
            showProgress(true);
            createUser(firstName, lastName);
        }
    }

    private void createUser(String firstName, String lastName) {
        UserController.createUser(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                ServiceProvider.login(credential);
                initGCM(context);
                PrefManager.setSelectedUserMail(getApplicationContext(), accountName);
                if (aBoolean) {
                    Toast.makeText(getApplicationContext(), "Benutzer erfolgreich erstellt", Toast.LENGTH_LONG).show();
                    uploadUserPicture();
                } else {
                    Toast.makeText(getApplicationContext(), "Benutzer existiert bereits. Login erfolgreich.", Toast.LENGTH_LONG).show();
                    context.startActivity(new Intent(RegisterActivity.this, ListEventsActivity.class));
                    finish();
                }
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(getApplicationContext(), "Serverfehler", Toast.LENGTH_LONG).show();
            }
        }, accountName, firstName, lastName);
    }

    private void uploadUserPicture() {
        if(selectedPicture != null) {
            UserController.uploadUserPicture(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                    if (aBoolean)
                        Toast.makeText(context, "Bild hochgeladen", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(context, "Konnte Bild nicht hochladen", Toast.LENGTH_LONG).show();
                    context.startActivity(new Intent(RegisterActivity.this, ListEventsActivity.class));
                    finish();
                }

                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    Toast.makeText(getApplicationContext(), "Serverfehler beim Profilbildupload", Toast.LENGTH_LONG).show();
                    context.startActivity(new Intent(RegisterActivity.this, ListEventsActivity.class));
                    finish();
                }
            }, accountName, ImageUtil.BitmapToInputStream(selectedPicture));
        } else {
            context.startActivity(new Intent(RegisterActivity.this, ListEventsActivity.class));
            finish();
        }
    }

    /**
     * Leitet die Auswahl des Bildes weiter.
     *
     * @param view
     */
    public void setUpImage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle(R.string.profile_picture)
                .setItems(R.array.image_selection, new DialogInterface.OnClickListener() {
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
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * Prüft, ob für einen Nutzer bereits ein Benutzer existiert.
     */
    private void attemptLogin(){
        showProgress(true);
        UserController.existsUser(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                if (aBoolean) {
                    Toast.makeText(getApplicationContext(), "Benutzer erfolgreich eingeloggt", Toast.LENGTH_LONG).show();
                    PrefManager.setSelectedUserMail(getApplicationContext(), accountName);
                    ServiceProvider.login(credential);
                    initGCM(context);
                    context.startActivity(new Intent(RegisterActivity.this, ListEventsActivity.class));
                    finish();
                } else {
                    showProgress(false);
                }
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(getApplicationContext(), "Serverfehler", Toast.LENGTH_LONG).show();
            }
        }, accountName);
    }

    /**
     * Zeigt die Progressbar ohne die restlichen UI-Komponenten.
     */
    public void showProgress(final boolean show) {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ACCOUNT_PICKER:
                    if (data != null && data.getExtras() != null) {
                        accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            credential.setSelectedAccountName(accountName);
                            attemptLogin();
                        }
                    } else {
                        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                    }
                    break;
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
                    selectedPicture = extras.getParcelable("data");
                    mPictureView.setImageBitmap(ImageUtil.getRoundedBitmapFramed(selectedPicture));
                    tempFile.deleteOnExit();
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

    private void initGCM(Context context) {
        context = this;
        if (GCMUtil.checkPlayServices(this)) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String regid = LocalGCMUtil.getRegistrationId(context);

            if (regid.isEmpty()) {
                LocalGCMUtil.registerInBackground(context, gcm);
            } else {
                LocalGCMUtil.sendRegistrationIdToBackend(regid);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }
}

