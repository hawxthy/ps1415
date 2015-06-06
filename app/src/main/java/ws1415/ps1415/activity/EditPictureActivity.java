package ws1415.ps1415.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.Picture;
import com.skatenight.skatenightAPI.model.PictureData;

import java.io.File;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.GalleryController;
import ws1415.ps1415.model.PictureVisibility;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.DiskCacheImageLoader;
import ws1415.ps1415.util.ImageUtil;

public class EditPictureActivity extends Activity {
    public static final String EXTRA_PICTURE_ID = EditPictureActivity.class.getName() + ".PictureId";

    private static final int CHOOSE_PICTURE_REQUEST_CODE = 1;

    private Long pictureId;

    private ImageView picture;
    private TextView title;
    private TextView description;
    private Spinner visibility;
    private TextView visibilityHint;

    private File selectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_picture);

        picture = (ImageView) findViewById(R.id.picture);
        title = (TextView) findViewById(R.id.title);
        description = (TextView) findViewById(R.id.description);

        visibilityHint = (TextView) findViewById(R.id.visibilityHint);
        visibilityHint.setText(getResources().getStringArray(R.array.picture_visibility_hints)[0]);

        visibility = (Spinner) findViewById(R.id.visibility);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.picture_visibilities, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibility.setAdapter(spinnerAdapter);
        visibility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                visibilityHint.setText(getResources().getStringArray(R.array.picture_visibility_hints)[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Sollte nicht vorkommen
            }
        });

        if (getIntent().hasExtra(EXTRA_PICTURE_ID)) {
            pictureId = getIntent().getLongExtra(EXTRA_PICTURE_ID, -1);
            startLoading();
            GalleryController.getPicture(new ExtendedTaskDelegateAdapter<Void, PictureData>() {
                @Override
                public void taskDidFinish(ExtendedTask task, PictureData pictureData) {
                    picture.setImageBitmap(null);
                    picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    DiskCacheImageLoader.getInstance().loadScaledImage(picture, pictureData.getImageBlobKey(), picture.getWidth());
                    title.setText(pictureData.getTitle());
                    description.setText(pictureData.getDescription());
                    visibility.setSelection(PictureVisibility.valueOf(pictureData.getVisibility()).ordinal());
                    finishLoading();
                }
                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    finishLoading();
                }
            }, pictureId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_picture, menu);
        return true;
    }

    /**
     * Startet die Ladeanimation.
     */
    private void startLoading() {
        findViewById(R.id.pictureUploading).setVisibility(View.VISIBLE);
    }

    /**
     * Beendet die Ladeanimation.
     */
    private void finishLoading() {
        findViewById(R.id.pictureUploading).setVisibility(View.GONE);
    }

    public void onChoosePictureClick(View view) {
        if (pictureId != null) {
            // Falls Bild editiert wird, dann Auswahl eines anderen Bildes nicht zulassen
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.picture_can_not_be_changed).setNeutralButton(R.string.ok, null);
            builder.create().show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)), CHOOSE_PICTURE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CHOOSE_PICTURE_REQUEST_CODE:
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    String[] projection = {MediaStore.MediaColumns.DATA};
                    CursorLoader cl = new CursorLoader(this);
                    cl.setUri(selectedImageUri);
                    cl.setProjection(projection);
                    Cursor cursor = cl.loadInBackground();
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();
                    String tempPath = cursor.getString(column_index);
                    cursor.close();

                    selectedFile = new File(tempPath);
                    picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    ImageUtil.loadSubsampledImageInView(selectedFile, picture, picture.getWidth());
                }
                break;
        }
    }

    public void onCancelClick(View view) {
        finish();
    }

    public void onSaveClick(View view) {
        startLoading();
        if (pictureId != null) {
            GalleryController.editPicture(new ExtendedTaskDelegateAdapter<Void, Void>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Void aVoid) {
                    finishLoading();
                    finish();
                }
                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    Toast.makeText(EditPictureActivity.this, R.string.error_editing_picture, Toast.LENGTH_LONG).show();
                    finishLoading();
                }
            }, pictureId, title.getText().toString(), description.getText().toString(), PictureVisibility.values()[visibility.getSelectedItemPosition()]);
        } else {
            GalleryController.uploadPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Picture picture) {
                    finishLoading();
                    finish();
                }

                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    Toast.makeText(EditPictureActivity.this, R.string.error_uploading_picture, Toast.LENGTH_LONG).show();
                    finishLoading();
                }
            }, selectedFile, title.getText().toString(), description.getText().toString(), PictureVisibility.values()[visibility.getSelectedItemPosition()]);
        }
    }
}
