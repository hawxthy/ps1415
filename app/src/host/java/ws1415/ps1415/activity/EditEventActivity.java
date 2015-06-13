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
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import ws1415.ps1415.LocationTransmitterService;
import ws1415.ps1415.R;
import ws1415.ps1415.adapter.DynamicFieldsAdapter;
import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.controller.RouteController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.DiskCacheImageLoader;
import ws1415.ps1415.util.FormatterUtil;
import ws1415.ps1415.util.ImageUtil;
import ws1415.ps1415.util.UniversalUtil;

public class EditEventActivity extends Activity implements ExtendedTaskDelegate<Void,EventData> {
    public static final String EXTRA_EVENT_ID = ShowEventActivity.class.getName() + ".EventId";

    private static final String MEMBER_EVENT_ID = ShowEventActivity.class.getName() + ".EventId";

    private static final int SELECT_HEADER_IMAGE_REQUEST_CODE = 1;
    private static final int SELECT_ICON_IMAGE_REQUEST_CODE = 2;
    private static final int SELECT_IMAGES_REQUEST_CODE = 3;
    /**
     * Enthält die ID des Events, das über diese Activity bearbeitet wird. null, falls ein neues
     * Event erstellt wird.
     */
    private Long eventId;
    private Calendar eventDate = Calendar.getInstance();
    private File iconFile;
    private File headerImageFile;
    private List<File> imageFiles = new LinkedList<>();
    private Route selectedRoute;

    private ImageView icon;
    private ImageView headerImage;
    private EditText title;
    private EditText date;
    private EditText description;
    private EditText meetingPlace;
    private EditText fee;
    private Button route;
    private DynamicFieldsAdapter dynamicFieldsAdapter;
    private LinearLayout images;
    private HorizontalScrollView imagesScroller;

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

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(EditEventActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });

        setContentView(R.layout.activity_edit_event);

        icon = (ImageView) findViewById(R.id.icon);
        headerImage = (ImageView) findViewById(R.id.headerImage);
        title = (EditText) findViewById(R.id.title);
        date = (EditText) findViewById(R.id.date);
        description = (EditText) findViewById(R.id.description);
        meetingPlace = (EditText) findViewById(R.id.meeting_place);
        fee = (EditText) findViewById(R.id.fee);
        fee.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !FormatterUtil.isCurrencyString(fee.getText().toString())) {
                    fee.setError(getResources().getString(R.string.error_wrong_currency_format));
                }
            }
        });
        route = (Button) findViewById(R.id.route);
        imagesScroller = (HorizontalScrollView) findViewById(R.id.imagesScroller);
        images = (LinearLayout) findViewById(R.id.images);

        startLoading();

        if (savedInstanceState == null && !getIntent().hasExtra(EXTRA_EVENT_ID)) {
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
        } else {
            if (savedInstanceState != null && savedInstanceState.containsKey(MEMBER_EVENT_ID)) {
                eventId = savedInstanceState.getLong(MEMBER_EVENT_ID);
            } else if (getIntent().hasExtra(EXTRA_EVENT_ID)) {
                eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
            }
            EventController.getEvent(this, eventId);
            setTitle(R.string.edit_event);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (eventId != null) {
            outState.putLong(MEMBER_EVENT_ID, eventId);
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
        int id = item.getItemId();

        if (id == R.id.action_save_event) {
            finish();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Startet die Ladeanimation.
     */
    private void startLoading() {
        findViewById(R.id.eventLoading).setVisibility(View.VISIBLE);
    }

    /**
     * Beendet die Ladeanimation.
     */
    private void finishLoading() {
        findViewById(R.id.eventLoading).setVisibility(View.GONE);
    }

    /**
     * Sorgt dafür, dass beim Schließen der Activity nachgefragt wird, ob Änderungen gespeichert werden sollen.
     */
    @Override
    public void finish() {
        if (edited || (dynamicFieldsAdapter != null && dynamicFieldsAdapter.isEdited())) {
            if (!FormatterUtil.isCurrencyString(fee.getText().toString())) {
                Toast.makeText(this, R.string.error_wrong_currency_format, Toast.LENGTH_LONG).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.save_event)
                    .setMessage(R.string.save_event_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startLoading();

                            // Das Event aus den Informationen in der Activity erstellen
                            Event event = new Event();
                            event.setId(eventId);
                            event.setTitle(title.getText().toString());
                            event.setDate(new DateTime(eventDate.getTime()));
                            event.setDescription(new Text().setValue(description.getText().toString()));
                            event.setMeetingPlace(meetingPlace.getText().toString());
                            if (!fee.getText().toString().isEmpty()) {
                                event.setFee(FormatterUtil.getCentsFromCurrencyString(fee.getText().toString()));
                            } else {
                                event.setFee(0);
                            }
                            event.setRoute(selectedRoute);
                            event.setDynamicFields(dynamicFieldsAdapter.getList());

                            if (eventId != null) {
                                try {
                                    EventController.editEvent(new ExtendedTaskDelegateAdapter<Void, Event>() {
                                        @Override
                                        public void taskDidFinish(ExtendedTask task, Event event) {
                                            EditEventActivity.super.finish();
                                        }

                                        @Override
                                        public void taskFailed(ExtendedTask task, String message) {
                                            Toast.makeText(EditEventActivity.this, message, Toast.LENGTH_LONG).show();
                                            findViewById(R.id.eventLoading).setVisibility(View.GONE);
                                        }
                                    }, event);
                                } catch(IllegalArgumentException e) {
                                    // Event ist ungültig
                                    finishLoading();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(EditEventActivity.this);
                                    builder.setTitle(R.string.error_invalid_event)
                                            .setMessage(R.string.error_invalid_event_message)
                                            .setPositiveButton(R.string.ok, null);
                                    builder.create().show();
                                }
                            } else {
                                try {
                                    EventController.createEvent(new ExtendedTaskDelegateAdapter<Void, Event>() {
                                        @Override
                                        public void taskDidFinish(ExtendedTask task, Event event) {
                                            LocationTransmitterService.ScheduleService(EditEventActivity.this, event.getId(), new Date(event.getDate().getValue()));
                                            EditEventActivity.super.finish();
                                        }

                                        @Override
                                        public void taskFailed(ExtendedTask task, String message) {
                                            Toast.makeText(EditEventActivity.this, message, Toast.LENGTH_LONG).show();
                                            findViewById(R.id.eventLoading).setVisibility(View.GONE);
                                        }
                                    }, event, iconFile, headerImageFile, imageFiles);
                                } catch(IllegalArgumentException e) {
                                    // Event ist ungültig
                                    finishLoading();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(EditEventActivity.this);
                                    builder.setTitle(R.string.error_invalid_event)
                                            .setMessage(R.string.error_invalid_event_message)
                                            .setPositiveButton(R.string.ok, null);
                                    builder.create().show();
                                }
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditEventActivity.super.finish();
                        }
                    })
                    .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        } else {
            super.finish();
        }
    }

    // -------------------- Callback-Methoden für das Abrufen eines Events --------------------
    @Override
    public void taskDidFinish(ExtendedTask task, EventData eventData) {
        if (eventData.getIcon() != null) {
            DiskCacheImageLoader.getInstance().loadImage(icon, eventData.getIcon());
        }
        if (eventData.getHeaderImage() != null) {
            DiskCacheImageLoader.getInstance().loadImage(headerImage, eventData.getHeaderImage());
            headerImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        if (task == null) {
            // Falls es sich um Template-Daten handelt, dann nur den Hint der Textfelder setzen
            title.setHint(eventData.getTitle());
            description.setHint(eventData.getDescription());
            meetingPlace.setHint(eventData.getMeetingPlace());
            fee.setText(FormatterUtil.formatCents(0));
        } else {
            title.setText(eventData.getTitle());
            description.setText(eventData.getDescription());
            meetingPlace.setText(eventData.getMeetingPlace());
            if (fee != null) {
                fee.setText(FormatterUtil.formatCents(eventData.getFee()));
            } else {
                fee.setText(FormatterUtil.formatCents(0));
            }
        }
        eventDate.setTime(new Date(eventData.getDate().getValue()));
        date.setText(DateFormat.getMediumDateFormat(this).format(eventDate.getTime())
                + " " + DateFormat.getTimeFormat(this).format(eventDate.getTime()));

        dynamicFieldsAdapter = new DynamicFieldsAdapter(eventData.getDynamicFields(), true);
        ListView dynamicFields = (ListView) findViewById(R.id.dynamicFields);
        dynamicFields.setAdapter(dynamicFieldsAdapter);

        if (eventData.getImages() != null) {
            ImageView imgView;
            for (BlobKey key : eventData.getImages()) {
                imgView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                imgView.setLayoutParams(params);
                imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                DiskCacheImageLoader.getInstance().loadScaledImage(imgView, key, imagesScroller.getWidth() - 20);
                images.addView(imgView);
            }
        }

        if (eventData.getRoute() != null) {
            for (Route r : routes) {
                if (r.getId().equals(eventData.getRoute().getId())) {
                    selectedRoute = r;
                }
            }
            if (selectedRoute != null) {
                route.setText(selectedRoute.getName());
            }
        }

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
        meetingPlace.addTextChangedListener(editingWatcher);
        fee.addTextChangedListener(editingWatcher);

        finishLoading();
    }

    @Override
    public void taskDidProgress(ExtendedTask task, Void... progress) { }

    @Override
    public void taskFailed(ExtendedTask task, String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finishLoading();
    }
    // -------------------- Callback-Methoden für das Abrufen eines Events --------------------

    public void onIconImageClick(View view) {
        if (eventId != null) {
            Toast.makeText(this, R.string.error_can_not_change_picture, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)), SELECT_ICON_IMAGE_REQUEST_CODE);
    }

    public void onHeaderImageClick(View view) {
        if (eventId != null) {
            Toast.makeText(this, R.string.error_can_not_change_picture, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)), SELECT_HEADER_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImageUri = null;
        String tempPath = null;
        if (requestCode >= 1 && requestCode <= 3 && data != null) {
            // Falls Bilder abgerufen werden, dann Pfad des gewählten Bildes auslesen
            selectedImageUri = data.getData();
            String[] projection = {MediaStore.MediaColumns.DATA};
            CursorLoader cl = new CursorLoader(this);
            cl.setUri(selectedImageUri);
            cl.setProjection(projection);
            Cursor cursor = cl.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            tempPath = cursor.getString(column_index);
            cursor.close();
        }

        switch (requestCode) {
            case SELECT_HEADER_IMAGE_REQUEST_CODE:
                if (tempPath != null && selectedImageUri != null) {
                    headerImageFile = new File(tempPath);
                    ImageUtil.loadSubsampledImageInView(headerImageFile, headerImage, headerImage.getWidth());
                    edited = true;
                }
                break;
            case SELECT_ICON_IMAGE_REQUEST_CODE:
                if (tempPath != null && selectedImageUri != null) {
                    iconFile = new File(tempPath);
                    icon.setImageURI(selectedImageUri);
                    edited = true;
                }
                break;
            case SELECT_IMAGES_REQUEST_CODE:
                if (tempPath != null && selectedImageUri != null) {
                    final File f = new File(tempPath);
                    imageFiles.add(f);
                    ImageView v = new ImageView(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.CENTER_VERTICAL;
                    v.setLayoutParams(params);
                    v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ImageUtil.loadSubsampledImageInView(f, v, imagesScroller.getWidth() - 20);
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(final View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditEventActivity.this);
                            builder.setTitle(R.string.delete_picture)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            images.removeView(v);
                                            imageFiles.remove(f);
                                            images.requestLayout();
                                        }
                                    })
                                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.create().show();
                            return false;
                        }
                    });
                    images.addView(v);
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

    public void onAddDynamicFieldClick(View view) {
        dynamicFieldsAdapter.addField();
        edited = true;
    }

    public void onAddImageClick(View view) {
        if (eventId != null) {
            Toast.makeText(this, R.string.error_can_not_change_picture, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)), SELECT_IMAGES_REQUEST_CODE);
    }
}
