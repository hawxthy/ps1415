package ws1415.ps1415.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.DynamicField;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.EventData;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.DynamicFieldsAdapter;
import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.controller.RouteController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.DiskCacheImageLoader;
import ws1415.ps1415.util.UniversalUtil;

public class EditEventActivity extends Activity implements ExtendedTaskDelegate<Void,EventData> {
    public static final String EXTRA_EVENT_ID = ShowEventActivity.class.getName() + ".EventId";
    private static final int SELECT_HEADER_IMAGE_REQUEST_CODE = 1;
    /**
     * Enthält die ID des Events, das über diese Activity bearbeitet wird. null, falls ein neues
     * Event erstellt wird.
     */
    private Long eventId;
    private Calendar eventDate = Calendar.getInstance();
    private File headerImageFile;
    private File iconFile;
    private List<File> imageFiles;
    private Route selectedRoute;

    private ImageView headerImage;
    private EditText title;
    private TextView date;
    private EditText description;
    private EditText meetingPlace;
    // TODO Textfeld für Gebühr anpassen, damit Cent in € angezeigt und bei Eingabe konvertiert werden
    private EditText fee;
    private Button route;

    private DynamicFieldsAdapter dynamicFieldsAdapter;

    // TODO Schalter aktualisieren, wenn Treffpunkt oder Gebühr geändert wird
    private boolean edited = false;

    private Route[] routes;
    private String[] routeNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }

        // Die Routen werden zu Beginn einmal abgerufen
        RouteController.getRoutes(new ExtendedTaskDelegateAdapter<Void, List<Route>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<Route> newRoutes) {
                if (newRoutes != null) {
                    routes = new Route[newRoutes.size()];
                    routeNames = new String[newRoutes.size()];
                    int index = 0;
                    for (Route r : newRoutes) {
                        routes[index] = r;
                        routeNames[index] = r.getName();
                        index++;
                    }
                }
            }
        });

        setContentView(R.layout.activity_edit_event);
        headerImage = (ImageView) findViewById(R.id.headerImage);
        title = (EditText) findViewById(R.id.title);
        date = (TextView) findViewById(R.id.date);
        description = (EditText) findViewById(R.id.description);
        meetingPlace = (EditText) findViewById(R.id.meeting_place);
        fee = (EditText) findViewById(R.id.fee);
        route = (Button) findViewById(R.id.route);

        if (getIntent().hasExtra(EXTRA_EVENT_ID)) {
            eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
            EventController.getEvent(this, eventId);
            setTitle(R.string.edit_event);
        } else {
            // Ein neues Event wird erstellt
            EventData templateData = new EventData();
            templateData.setTitle(getString(R.string.template_event_title));
            templateData.setDate(new DateTime(new Date()));
            templateData.setDescription(getString(R.string.template_event_description));
            templateData.setFee(0);
            templateData.setMeetingPlace(getString(R.string.template_event_meeting_place));
            templateData.setDynamicFields(new LinkedList<DynamicField>());
            templateData.setImages(new LinkedList<BlobKey>());
            taskDidFinish(null, templateData);
            setTitle(R.string.create_event);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_event, menu);
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
    public void onBackPressed() {
        if (edited) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.save_event)
                    .setMessage(R.string.save_event_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            findViewById(R.id.eventLoading).setVisibility(View.VISIBLE);

                            // Das Event aus den Informationen in der Activity erstellen
                            Event event = new Event();
                            event.setId(eventId);
                            event.setTitle(title.getText().toString());
                            event.setDate(new DateTime(eventDate.getTime()));
                            event.setDescription(new Text().setValue(description.getText().toString()));
                            event.setMeetingPlace(meetingPlace.getText().toString());
                            event.setFee(Integer.parseInt(fee.getText().toString()));
                            event.setRoute(selectedRoute);
                            event.setDynamicFields(dynamicFieldsAdapter.getList());

                            if (eventId != null) {
                                EventController.editEvent(new ExtendedTaskDelegateAdapter<Void, Event>() {
                                    @Override
                                    public void taskDidFinish(ExtendedTask task, Event event) {
                                        finish();
                                    }
                                    @Override
                                    public void taskFailed(ExtendedTask task, String message) {
                                        Toast.makeText(EditEventActivity.this, R.string.error_saving_event, Toast.LENGTH_LONG).show();
                                        findViewById(R.id.eventLoading).setVisibility(View.GONE);
                                    }
                                }, event);
                            } else {
                                EventController.createEvent(new ExtendedTaskDelegateAdapter<Void, Event>() {
                                    @Override
                                    public void taskDidFinish(ExtendedTask task, Event event) {
                                        finish();
                                    }
                                    @Override
                                    public void taskFailed(ExtendedTask task, String message) {
                                        Toast.makeText(EditEventActivity.this, R.string.error_saving_event, Toast.LENGTH_LONG).show();
                                        findViewById(R.id.eventLoading).setVisibility(View.GONE);
                                    }
                                }, event, iconFile, headerImageFile, imageFiles);
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.create().show();
        } else {
            super.onBackPressed();
        }
    }

    // -------------------- Callback-Methoden für das Abrufen eines Events --------------------
    @Override
    public void taskDidFinish(ExtendedTask task, EventData eventData) {
        if (eventData.getHeaderImage() != null) {
            DiskCacheImageLoader.getInstance().loadImage(headerImage, eventData.getHeaderImage());
            headerImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        title.setText(eventData.getTitle());
        eventDate.setTime(new Date(eventData.getDate().getValue()));
        date.setText(DateFormat.getMediumDateFormat(this).format(eventDate.getTime())
                + " " + DateFormat.getTimeFormat(this).format(eventDate.getTime()));
        description.setText(eventData.getDescription());
        meetingPlace.setText(eventData.getMeetingPlace());
        fee.setText(Integer.toString(eventData.getFee()));

        dynamicFieldsAdapter = new DynamicFieldsAdapter(eventData.getDynamicFields());
        ListView dynamicFields = (ListView) findViewById(R.id.dynamicFields);
        dynamicFields.setAdapter(dynamicFieldsAdapter);

        HorizontalScrollView images = (HorizontalScrollView) findViewById(R.id.images);
        if (eventData.getImages() != null) {
            ImageView imgView;
            for (BlobKey key : eventData.getImages()) {
                imgView = new ImageView(this);
                imgView.setAdjustViewBounds(true);
                DiskCacheImageLoader.getInstance().loadImage(imgView, key);
                images.addView(imgView);
            }
        }

        findViewById(R.id.eventLoading).setVisibility(View.GONE);
    }

    @Override
    public void taskDidProgress(ExtendedTask task, Void... progress) { }

    @Override
    public void taskFailed(ExtendedTask task, String message) {
        Toast.makeText(this, R.string.event_loading_error, Toast.LENGTH_LONG).show();
        findViewById(R.id.eventLoading).setVisibility(View.GONE);
    }
    // -------------------- Callback-Methoden für das Abrufen eines Events --------------------
    
    public void onHeaderImageClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)), SELECT_HEADER_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SELECT_HEADER_IMAGE_REQUEST_CODE:
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

                    headerImageFile = new File(tempPath);
                    headerImage.setImageURI(selectedImageUri);
                    headerImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    edited = true;
                }
                break;
        }
    }

    public void onEditDateClick(View view) {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_datetime_picker);
        d.setTitle(R.string.choose_datetime);
        final DatePicker datePicker = (DatePicker) d.findViewById(R.id.datePicker);
        datePicker.updateDate(eventDate.get(Calendar.YEAR), eventDate.get(Calendar.MONTH), eventDate.get(Calendar.DAY_OF_MONTH));
        final TimePicker timePicker = (TimePicker) d.findViewById(R.id.timePicker);
        timePicker.setCurrentHour(eventDate.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(eventDate.get(Calendar.MINUTE));
        timePicker.setIs24HourView(true);
        Button ok = (Button) d.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edited = true;
                eventDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
                date.setText(DateFormat.getMediumDateFormat(EditEventActivity.this).format(eventDate.getTime())
                        + " " + DateFormat.getTimeFormat(EditEventActivity.this).format(eventDate.getTime()));
                d.dismiss();
            }
        });
        Button cancel = (Button) d.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.cancel();
            }
        });
        d.show();
    }

    public void onRouteClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_route)
                .setItems(routeNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedRoute = routes[which];
                        route.setText(selectedRoute.getName());
                        edited = true;
                    }
                });
        builder.create().show();
    }
}
