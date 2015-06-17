package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.skatenight.skatenightAPI.model.BoardEntry;

import java.io.File;
import java.io.IOException;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.ImageUtil;
import ws1415.ps1415.util.UniversalUtil;

public class PostBlackBoardActivity extends Activity {
    private static final int SELECT_PHOTO = 1;
    private static final int PICTURE_CROP = 2;

    // Die Views
    private ImageView mMessageImage;
    private EditText mMessage;
    private TextView mErrorMessage;
    private ButtonFlat mCancelButton;
    private ButtonFlat mSendButton;

    // Attribute für das Laden von Bildern
    private File tempFile;
    private Uri pictureUri;
    private Bitmap mBitmap;

    private String groupName;

    private boolean checkBoardMesageTextChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_post_black_board);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }


        groupName = getIntent().getStringExtra(GroupProfileActivity.EXTRA_GROUP_NAME);
        if(groupName == null){
            Toast.makeText(this, R.string.noGroupNameInExtra, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialisiere die Views
        mMessageImage = (ImageView)findViewById(R.id.black_board_image_view);
        mMessage = (EditText)findViewById(R.id.post_black_board_edit_text);
        mSendButton = (ButtonFlat)findViewById(R.id.post_black_board_send_button);
        mCancelButton = (ButtonFlat)findViewById(R.id.post_black_board_cancel_button);
        mErrorMessage = (TextView) findViewById(R.id.failure_text_view);

        // Die TextView initialisieren, da die send global messge Funktion diese auch verwendet und kein Text gesetzt ist.
        mErrorMessage.setText(R.string.boardMessageTooShort);

        setListener();
    }

    private void setListener(){
        // EditText für die Blackboard Message
        mMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mMessage.getText().toString().length() > 1) {
                    mErrorMessage.setVisibility(View.GONE);
                    checkBoardMesageTextChecked = true;
                } else {
                    if (mErrorMessage.getVisibility() == View.GONE) {
                        mErrorMessage.setVisibility(View.VISIBLE);
                    }
                    checkBoardMesageTextChecked = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mMessageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picturePickerIntent = new Intent(Intent.ACTION_PICK);
                picturePickerIntent.setType("image/*");
                startActivityForResult(picturePickerIntent, SELECT_PHOTO);
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkBoardMesageTextChecked){
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, BoardEntry>(){
                        @Override
                        public void taskDidFinish(ExtendedTask task, BoardEntry boardEntry) {
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            //TODO wenn Zeit bessere Lösung finden
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(PostBlackBoardActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }, groupName, mMessage.getText().toString(), ImageUtil.BitmapToInputStream(mBitmap));
                    finish();
                }else{
                    Toast.makeText(PostBlackBoardActivity.this, R.string.textTooShort, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    /**
     * Diese Methode wird aufgerufen, wenn der Benutzer die ImageView drückt, ein Bild ausgewählt
     * und dieses zugeschnitten hat. Hier wird das Bild aus den Extras geladen, in eine Bitmap
     * umgewandelt und über den GroupController an den Server geschickt. Nachdem die geschehen ist,
     * wird das Bild mit dem GroupImageLoader vom Server heruntergeladen und in die ImageView gesetzt.
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
                if (mBitmap != null) {
                     mMessageImage.setImageBitmap(mBitmap);
                }else{
                    Toast.makeText(this, R.string.choose_image_error, Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_black_board, menu);
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