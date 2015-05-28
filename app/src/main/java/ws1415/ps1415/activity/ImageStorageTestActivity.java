package ws1415.ps1415.activity;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.PictureData;
import com.skatenight.skatenightAPI.model.PictureFilter;
import com.skatenight.skatenightAPI.model.PictureMetaData;
import com.skatenight.skatenightAPI.model.PictureMetaDataList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ws1415.common.component.BlobKeyImageView;
import ws1415.common.component.EventAdapter;
import ws1415.common.controller.GalleryController;
import ws1415.common.model.PictureVisibility;
import ws1415.common.net.ServiceProvider;
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
                GalleryController.getPicture(new ExtendedTaskDelegateAdapter<Void, PictureData>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, PictureData picture) {
                        imageView.loadFromBlobKey(picture.getImageBlobKey());
                    }
                }, 5751399832879104l);
            }
        });
        final Button loeschen = (Button) findViewById(R.id.btnLoeschen);
        loeschen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loeschen.setEnabled(false);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        final PictureFilter filter = new PictureFilter();
                        filter.setLimit(10);
                        filter.setUserId(ServiceProvider.getEmail());

                        final List<PictureMetaData> pictureList = new LinkedList<>();
                        do {
                            for (PictureMetaData p : pictureList) {
                                try {
                                    ServiceProvider.getService().galleryEndpoint().deletePicture(p.getId()).execute();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            pictureList.clear();

                            try {
                                PictureMetaDataList result = ServiceProvider.getService().galleryEndpoint().listPictures(filter).execute();
                                filter.setCursorString(result.getCursorString());
                                if (result.getList() != null) {
                                    pictureList.addAll(result.getList());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } while(!pictureList.isEmpty());

                        loeschen.post(new Runnable() {
                            @Override
                            public void run() {
                                loeschen.setEnabled(true);
                            }
                        });
                        return null;
                    }
                }.execute();
            }
        });

        ListView listView = (ListView) findViewById(R.id.eventList);
        listView.setAdapter(new EventAdapter());
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
                        GalleryController.uploadPicture(null, new FileInputStream(new File(tempPath)), "Bildtitel", "Dateipfad: " + tempPath, PictureVisibility.PRIVATE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
