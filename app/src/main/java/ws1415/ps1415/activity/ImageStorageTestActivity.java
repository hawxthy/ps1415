package ws1415.ps1415.activity;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.skatenight.skatenightAPI.model.Gallery;
import com.skatenight.skatenightAPI.model.Picture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ws1415.common.component.BlobKeyImageView;
import ws1415.common.controller.GalleryController;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.R;

public class ImageStorageTestActivity extends BaseActivity {
    private static final int SELECT_IMAGE_REQUEST_CODE = 1;

    private BlobKeyImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_storage_test);

        imageView = (BlobKeyImageView) findViewById(R.id.imageView);

        Button upload = (Button) findViewById(R.id.btnUpload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Bild wählen..."), SELECT_IMAGE_REQUEST_CODE);
            }
        });
        Button download = (Button) findViewById(R.id.btnDownload);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryController.getPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Picture picture) {
                        imageView.loadFromBlobKey(picture.getImageBlobKey());
                    }
                }, 5761329797267456l);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_storage_test, menu);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SELECT_IMAGE_REQUEST_CODE:
                if (data != null) {
                    // TODO: Abrufen von Bildern verstehen
                    Uri selectedImageUri = data.getData();
                    String[] projection = {MediaStore.MediaColumns.DATA};
                    CursorLoader cl = new CursorLoader(this);
                    cl.setUri(selectedImageUri);
                    cl.setProjection(projection);
                    Cursor cursor = cl.loadInBackground();
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();
                    final String tempPath = cursor.getString(column_index);
                    cursor.close();

                    try {
                        GalleryController.uploadPicture(null, new FileInputStream(new File(tempPath)), "Bildtitel", "Dateipfad: " + tempPath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
