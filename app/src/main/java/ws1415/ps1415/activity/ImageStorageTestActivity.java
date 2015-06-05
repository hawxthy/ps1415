package ws1415.ps1415.activity;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.EventFilter;
import com.skatenight.skatenightAPI.model.EventMetaData;
import com.skatenight.skatenightAPI.model.EventMetaDataList;
import com.skatenight.skatenightAPI.model.PictureFilter;
import com.skatenight.skatenightAPI.model.PictureMetaData;
import com.skatenight.skatenightAPI.model.PictureMetaDataList;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.EventAdapter;
import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.controller.GalleryController;
import ws1415.ps1415.model.PictureVisibility;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

public class ImageStorageTestActivity extends BaseActivity {
    private static final int SELECT_IMAGE_REQUEST_CODE = 1;
    private static final int SELECT_EVENT_ICON_CODE = 2;

    private Button btnTestEventsErstellen;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_storage_test);


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

        btnTestEventsErstellen = (Button) findViewById(R.id.testEventsErstellen);
        btnTestEventsErstellen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Bild wählen..."), SELECT_EVENT_ICON_CODE);
            }
        });

        Button btnEventsLoeschen = (Button) findViewById(R.id.eventsLoeschen);
        btnEventsLoeschen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        EventFilter filter = new EventFilter();
                        filter.setLimit(30);

                        final List<EventMetaData> eventsToDelete = new LinkedList<>();
                        do {
                            for (EventMetaData event : eventsToDelete) {
                                try {
                                    ServiceProvider.getService().eventEndpoint().deleteEvent(event.getId()).execute();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            eventsToDelete.clear();

                            try {
                                EventMetaDataList result = ServiceProvider.getService().eventEndpoint().listEvents(filter).execute();
                                if (result.getList() != null) {
                                    filter.setCursorString(result.getCursorString());
                                    eventsToDelete.addAll(result.getList());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } while(!eventsToDelete.isEmpty());
                        return null;
                    }
                }.execute();
            }
        });

        listView = (ListView) findViewById(R.id.eventList);
        EventFilter filter = new EventFilter();
        filter.setLimit(10);
        listView.setAdapter(new EventAdapter(this, filter));
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

                    GalleryController.uploadPicture(null, new File(tempPath), "Bildtitel", "Dateipfad: " + tempPath, PictureVisibility.PRIVATE);
                }
                break;

            case SELECT_EVENT_ICON_CODE:
                if (data != null) {
                    btnTestEventsErstellen.setEnabled(false);

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
                    final File file = new File(tempPath);

                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            Route route = new Route();
                            route.setName("Test");
                            route.setLength("10 km");
                            try {
                                route = ServiceProvider.getService().routeEndpoint().addRoute(route).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            long time = new Date().getTime();
                            for (int i = 1; i <= 30; i++) {
                                final int index = i;
                                Event event = new Event();
                                event.setDescription(new Text().setValue("Beschreibung"));
                                event.setMeetingPlace("Münster");
                                event.setFee(100);
                                event.setRoute(route);
                                event.setTitle("Testevent #" + i);
                                event.setDate(new DateTime(time + i * 86400000));
                                EventController.createEvent(new ExtendedTaskDelegateAdapter<Void, Event>() {
                                    @Override
                                    public void taskDidFinish(ExtendedTask task, Event event) {
                                        Log.d("TEST", "Testevent #" + index + " erstellt");
                                    }
                                }, event, file, file, Arrays.asList(file));
                            }
                            btnTestEventsErstellen.post(new Runnable() {
                                @Override
                                public void run() {
                                    btnTestEventsErstellen.setEnabled(true);
                                }
                            });
                            return null;
                        }
                    }.execute();
                }
                break;
        }
    }

    public void refreshList(View view) {
        ((EventAdapter) listView.getAdapter()).refresh();
    }
}
