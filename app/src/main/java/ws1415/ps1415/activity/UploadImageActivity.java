package ws1415.ps1415.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.UserGroupPicture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.R;
import ws1415.ps1415.util.ImageUtil;

public class UploadImageActivity extends BaseActivity {
    private static final int SELECT_PHOTO = 1;
    private String selectedImagePath;
    private Button downloadPictureButton;
    private Button choosePictureButton;
    private String keyString;
    private ImageView blobStoreImageView;
    final private String TEST_GROUP_NAME = "Testgruppe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        downloadPictureButton = (Button) findViewById(R.id.downloadPicture);
        choosePictureButton = (Button) findViewById(R.id.choosePictureButton);
        blobStoreImageView = (ImageView) findViewById(R.id.imageView);

        downloadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupController.getInstance().getUserGroupPicture(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Bitmap picture) {
                        blobStoreImageView.setImageBitmap(picture);
                    }
                }, TEST_GROUP_NAME);
            }
        });

        choosePictureButton.setOnClickListener(new View.OnClickListener() {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload_image, menu);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    InputStream is = null;
                    try {
                        Bundle extras = imageReturnedIntent.getExtras();
                        Bitmap cropedBitmap = extras.getParcelable("data");

                        if(cropedBitmap != null){
                            Toast.makeText(this, "Uploading image", Toast.LENGTH_LONG).show();

                            GroupController.getInstance().changePicture(new ExtendedTaskDelegateAdapter<Void, UserGroupPicture>() {
                                @Override
                                public void taskDidFinish(ExtendedTask task, UserGroupPicture picture) {
                                    keyString = picture.getPictureBlobKey().getKeyString();
                                    doneLoading();
                                    int i =0;
                                }
                            }, TEST_GROUP_NAME, ImageUtil.BitmapToInputStream(cropedBitmap));
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

    private String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }

        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            return uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public void doneLoading() {
        Toast.makeText(this, "Done loading picture", Toast.LENGTH_LONG).show();
    }
}
