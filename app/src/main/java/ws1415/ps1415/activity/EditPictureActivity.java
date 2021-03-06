package ws1415.ps1415.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Arrays;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.GalleryController;
import ws1415.ps1415.model.PictureVisibility;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.DiskCacheImageLoader;
import ws1415.ps1415.util.ImageUtil;

public class EditPictureActivity extends Activity {
    public static final String EXTRA_PICTURE_ID = EditPictureActivity.class.getName() + ".PictureId";
    public static final String EXTRA_POSITION = EditPictureActivity.class.getName() + ".Position";
    public static final String EXTRA_GALLERY_ID = EditPictureActivity.class.getSimpleName() + ".GalleryId";
    public static final String EXTRA_MIN_PICTURE_VISBILITY = EditPictureActivity.class.getSimpleName() + ".MinPictureVsibility";

    private static final String MEMBER_PICTURE_ID = EditPictureActivity.class.getName() + ".PictureId";
    private static final String MEMBER_GALLERY_ID = EditPictureActivity.class.getSimpleName() + ".GalleryId";
    private static final String MEMBER_MIN_PICTURE_VISBILITY = EditPictureActivity.class.getSimpleName() + ".MinPictureVsibility";

    private static final int CHOOSE_PICTURE_REQUEST_CODE = 1;

    private Long pictureId;
    private int position;
    private Long galleryId;
    private PictureVisibility initialVisibility;
    private PictureVisibility minPictureVisibility;

    private ImageView picture;
    private TextView title;
    private TextView description;
    private Spinner visibility;
    private TextView visibilityHint;

    private File selectedFile;

    private boolean edited = false;
    private boolean saving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_picture);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MEMBER_GALLERY_ID)) {
                galleryId = savedInstanceState.getLong(MEMBER_GALLERY_ID);
            }
            if (savedInstanceState.containsKey(MEMBER_MIN_PICTURE_VISBILITY)) {
                minPictureVisibility = PictureVisibility.valueOf(savedInstanceState.getString(MEMBER_MIN_PICTURE_VISBILITY));
            }
            if (savedInstanceState.containsKey(MEMBER_PICTURE_ID)) {
                pictureId = savedInstanceState.getLong(MEMBER_PICTURE_ID);
            }
        } else {
            if (getIntent().hasExtra(EXTRA_GALLERY_ID)) {
                galleryId = getIntent().getLongExtra(EXTRA_GALLERY_ID, -1);
            }
            if (getIntent().hasExtra(EXTRA_MIN_PICTURE_VISBILITY)) {
                minPictureVisibility = PictureVisibility.valueOf(getIntent().getStringExtra(EXTRA_MIN_PICTURE_VISBILITY));
            }
            if (getIntent().hasExtra(EXTRA_PICTURE_ID)) {
                pictureId = getIntent().getLongExtra(EXTRA_PICTURE_ID, -1);
            }
            position = getIntent().getIntExtra(EXTRA_POSITION, -1);
        }

        picture = (ImageView) findViewById(R.id.picture);
        title = (TextView) findViewById(R.id.title);
        description = (TextView) findViewById(R.id.description);

        visibilityHint = (TextView) findViewById(R.id.visibilityHint);
        visibilityHint.setText(getResources().getStringArray(R.array.picture_visibility_hints)[0]);

        visibility = (Spinner) findViewById(R.id.visibility);
        String[] visibilities = getResources().getStringArray(R.array.picture_visibilities);
        visibilities = Arrays.copyOfRange(visibilities, (minPictureVisibility != null ? minPictureVisibility.ordinal() : 0), visibilities.length);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                visibilities);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibility.setAdapter(spinnerAdapter);

        if (pictureId != null) {
            startLoading();
            GalleryController.getPicture(new ExtendedTaskDelegateAdapter<Void, PictureData>() {
                @Override
                public void taskDidFinish(ExtendedTask task, PictureData pictureData) {
                    picture.setImageBitmap(null);
                    picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    Display display = getWindowManager().getDefaultDisplay();
                    Point p = new Point();
                    display.getSize(p);
                    int height = p.y;
                    DiskCacheImageLoader.getInstance().loadScaledImage(picture, pictureData.getImageBlobKey(), height);
                    title.setText(pictureData.getTitle());
                    description.setText(pictureData.getDescription());
                    initialVisibility = PictureVisibility.valueOf(pictureData.getVisibility());
                    visibility.setSelection(Math.min(initialVisibility.ordinal(), visibility.getAdapter().getCount() - 1));
                    setUpListener();
                    finishLoading();
                }
                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    finishLoading();
                }
            }, pictureId);
        } else {
            setUpListener();
        }
    }

    private void setUpListener() {
        TextWatcher editingWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                edited = true;
            }
        };
        title.addTextChangedListener(editingWatcher);
        description.addTextChangedListener(editingWatcher);
        visibility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PictureVisibility v = getSelectedVisibility();
                if (initialVisibility != null && v.compareTo(initialVisibility) < 0) {
                    findViewById(R.id.changeVisibilityHint).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.changeVisibilityHint).setVisibility(View.GONE);
                }
                edited = initialVisibility != getSelectedVisibility();
                visibilityHint.setText(getResources().getStringArray(R.array.picture_visibility_hints)[v.ordinal()]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Sollte nicht vorkommen
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pictureId != null) {
            outState.putLong(MEMBER_PICTURE_ID, pictureId);
        }
        if (galleryId != null) {
            outState.putLong(MEMBER_GALLERY_ID, galleryId);
        }
        if (minPictureVisibility != null) {
            outState.putString(MEMBER_MIN_PICTURE_VISBILITY, minPictureVisibility.name());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        if (!saving && edited) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.save_picture)
                    .setMessage(R.string.save_picture_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            savePicture();
                        }
                    })
                    .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditPictureActivity.super.finish();
                        }
                    });
            builder.create().show();
        } else {
            super.finish();
        }
    }

    /**
     * Speichert das Bild ab, falls es gültige Metadaten hat.
     */
    private void savePicture() {
        startLoading();
        if (pictureId != null) {
            try {
                GalleryController.editPicture(new ExtendedTaskDelegateAdapter<Void, Void>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Void aVoid) {
                        setResult(0, new Intent().putExtra("position", position));
                        finishLoading();
                        EditPictureActivity.super.finish();
                    }

                    @Override
                    public void taskFailed(ExtendedTask task, String message) {
                        Toast.makeText(EditPictureActivity.this, R.string.error_editing_picture, Toast.LENGTH_LONG).show();
                        finishLoading();
                    }
                }, pictureId, title.getText().toString(), description.getText().toString(), getSelectedVisibility());
            } catch (IllegalArgumentException ex) {
                finishLoading();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.error_invalid_picture)
                        .setMessage(R.string.error_invalid_picture_message)
                        .setPositiveButton(R.string.ok, null);
                builder.create().show();
            }
        } else {
            try {
                GalleryController.uploadPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Picture picture) {
                        finishLoading();
                        EditPictureActivity.super.finish();
                    }

                    @Override
                    public void taskFailed(ExtendedTask task, String message) {
                        Toast.makeText(EditPictureActivity.this, message, Toast.LENGTH_LONG).show();
                        finishLoading();
                    }
                }, selectedFile, title.getText().toString(), description.getText().toString(), getSelectedVisibility(), galleryId);
            } catch (IllegalArgumentException ex) {
                finishLoading();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.error_invalid_picture)
                        .setMessage(R.string.error_invalid_picture_message)
                        .setPositiveButton(R.string.ok, null);
                builder.create().show();
            }
        }
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

                    File tmpSelectedFile = new File(tempPath);
                    picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    try {
                        ImageUtil.loadSubsampledImageInView(tmpSelectedFile, picture, picture.getWidth());
                    } catch (IllegalArgumentException ex) {
                        Toast.makeText(this, R.string.error_image_too_large, Toast.LENGTH_LONG).show();
                    }
                    selectedFile = tmpSelectedFile;
                    edited = true;
                }
                break;
        }
    }

    public void onCancelClick(View view) {
        super.finish();
    }

    private PictureVisibility getSelectedVisibility() {
        return PictureVisibility.values()[(minPictureVisibility != null ? minPictureVisibility.ordinal() : 0) + visibility.getSelectedItemPosition()];
    }

    public void onSaveClick(View view) {
        finish();
    }
}
