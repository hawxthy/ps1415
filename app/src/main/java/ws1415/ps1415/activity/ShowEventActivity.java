package ws1415.ps1415.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.EventData;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.ServerWaypoint;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.model.EventParticipationVisibility;
import ws1415.ps1415.model.EventRole;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;
import ws1415.ps1415.R;
import ws1415.ps1415.adapter.DynamicFieldsAdapter;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.DiskCacheImageLoader;
import ws1415.ps1415.util.FormatterUtil;
import ws1415.ps1415.util.LocationUtils;
import ws1415.ps1415.util.UniversalUtil;

public class ShowEventActivity extends Activity implements ExtendedTaskDelegate<Void,EventData> {
    public static final String EXTRA_EVENT_ID = ShowEventActivity.class.getName() + ".EventId";

    private MenuItem joinLeaveEventItem;
    private MenuItem settingsItem;

    private EventData event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_show_event);

        // "Zurück"-Button in der Actionbar anzeigen
        ActionBar mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setHomeButtonEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        startLoading();
        Long eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
        EventController.getEvent(this, eventId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_event, menu);
        joinLeaveEventItem = menu.findItem(R.id.action_join_leave_event);
        settingsItem = menu.findItem(R.id.action_settings);
        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (!event.getMemberList().containsKey(ServiceProvider.getEmail())) {
                Toast.makeText(this, R.string.error_participation_needed, Toast.LENGTH_LONG).show();
            } else {
                final int initialSelection = EventParticipationVisibility.valueOf(event.getParticipationVisibility()).ordinal();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                    private EventParticipationVisibility selection = EventParticipationVisibility.values()[initialSelection];
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            changeVisibility(selection);
                        } else {
                            selection = EventParticipationVisibility.values()[which];
                        }
                    }
                };
                builder.setTitle(R.string.choose_participation_visibility)
                        .setSingleChoiceItems(R.array.event_participation_visibilities, initialSelection, clickListener)
                        .setPositiveButton(R.string.save, clickListener)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.create().show();
            }
            return true;
        } else if (id == R.id.action_join_leave_event) {
            if (event.getMemberList().containsKey(ServiceProvider.getEmail())) {
                joinLeaveEventItem.setVisible(false);
                setProgressBarIndeterminateVisibility(true);
                EventController.leaveEvent(new ExtendedTaskDelegateAdapter<Void, Void>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Void aVoid) {
                        joinLeaveEventItem.setIcon(R.drawable.ic_action_join_event);
                        joinLeaveEventItem.setTitle(R.string.join_event);
                        event.getMemberList().remove(ServiceProvider.getEmail());
                        event.setParticipationVisibility(null);
                        finish();
                    }
                    @Override
                    public void taskFailed(ExtendedTask task, String message) {
                        Toast.makeText(ShowEventActivity.this, R.string.error_leaving_event, Toast.LENGTH_LONG).show();
                        finish();
                    }
                    private void finish() {
                        joinLeaveEventItem.setVisible(true);
                        setProgressBarIndeterminateVisibility(false);
                    }
                }, event.getId());
            } else {
                final int initialSelection = 2;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                    private EventParticipationVisibility selection = EventParticipationVisibility.values()[initialSelection];
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            joinEvent(selection);
                        } else {
                            selection = EventParticipationVisibility.values()[which];
                        }
                    }
                };
                builder.setTitle(R.string.choose_participation_visibility)
                        .setSingleChoiceItems(R.array.event_participation_visibilities, initialSelection, clickListener)
                        .setPositiveButton(R.string.ok, clickListener)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.create().show();
            }
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // -------------------- Callback-Methoden für das Abrufen eines Events --------------------
    @Override
    public void taskDidFinish(ExtendedTask task, EventData eventData) {
        event = eventData;

        // Falls der eingeloggte Benutzer am Event teilnimmt, dann Icon zum Verlassen anzeigen
        if (event.getMemberList().containsKey(ServiceProvider.getEmail())) {
            joinLeaveEventItem.setIcon(R.drawable.ic_action_leave_event);
            joinLeaveEventItem.setTitle(R.string.leave_event);
        }

        setTitle(eventData.getTitle());
        ImageView headerImage = (ImageView) findViewById(R.id.headerImage);
        if (eventData.getHeaderImage() != null) {
            DiskCacheImageLoader.getInstance().loadScaledImage(headerImage, eventData.getHeaderImage(), headerImage.getWidth());
        } else {
            headerImage.setVisibility(View.GONE);
        }
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(eventData.getTitle());
        TextView date = (TextView) findViewById(R.id.date);
        Date tmpDate = new Date(eventData.getDate().getValue());
        date.setText(DateFormat.getMediumDateFormat(this).format(tmpDate) + " " + DateFormat.getTimeFormat(this).format(tmpDate));
        TextView description = (TextView) findViewById(R.id.description);
        description.setText(eventData.getDescription());
        TextView meetingPlace = (TextView) findViewById(R.id.meeting_place);
        meetingPlace.setText(eventData.getMeetingPlace());
        TextView fee = (TextView) findViewById(R.id.fee);
        if (eventData.getFee() != null) {
            fee.setText(FormatterUtil.formatCents(eventData.getFee()));
        } else {
            fee.setText(FormatterUtil.formatCents(0));
        }
        ListView dynamicFields = (ListView) findViewById(R.id.dynamicFields);
        dynamicFields.setDivider(null);
        dynamicFields.setAdapter(new DynamicFieldsAdapter(eventData.getDynamicFields(), false));

        HorizontalScrollView imagesScroller = (HorizontalScrollView) findViewById(R.id.imagesScroller);
        LinearLayout images = (LinearLayout) findViewById(R.id.images);
        if (eventData.getImages() != null && !eventData.getImages().isEmpty()) {
            ImageView imgView;
            for (BlobKey key : eventData.getImages()) {
                imgView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                imgView.setLayoutParams(params);
                imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                DiskCacheImageLoader.getInstance().loadScaledImage(imgView, key, imagesScroller.getWidth() - 20);
                images.addView(imgView);
            }
        } else {
            imagesScroller.setVisibility(View.GONE);
        }

        // Route laden und anzeigen
        final GoogleMap googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.routeFragment)).getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        Route route = eventData.getRoute();
        String encodedPath = route.getRouteData().getValue();
        try {
            List<LatLng> line = LocationUtils.decodePolyline(encodedPath);

            final PolylineOptions routePoly = new PolylineOptions()
                    .addAll(line)
                    .color(Color.BLUE);

            googleMap.clear();
            googleMap.addPolyline(routePoly);

            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    if (routePoly != null) {
                        // Grenzwerte der Strecke berechnen und Karte zentrieren
                        LatLngBounds.Builder builder = LatLngBounds.builder();
                        for (LatLng point : routePoly.getPoints()) {
                            builder.include(point);
                        }
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                    }
                }
            });

            TextView routeTitle = (TextView) findViewById(R.id.routeTitle);
            routeTitle.setText(route.getName());
            TextView routeLength = (TextView) findViewById(R.id.routeLength);
            routeLength.setText(route.getLength());
        } catch (ParseException e) {
            Toast.makeText(getApplicationContext(), "Route parsing failed.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // Wegpunkte laden, falls vorhanden
        List<ServerWaypoint> waypoints = route.getWaypoints();
        for (ServerWaypoint wp : waypoints) {
            RouteEditorActivity.Waypoint tmp = RouteEditorActivity.Waypoint.create(
                    new LatLng(wp.getLatitude(), wp.getLongitude()), wp.getTitle());
            googleMap.addMarker(tmp.getMarkerOptions());
        }

        finishLoading();
    }

    @Override
    public void taskDidProgress(ExtendedTask task, Void... progress) { }

    @Override
    public void taskFailed(ExtendedTask task, String message) {
        Toast.makeText(this, R.string.event_loading_error, Toast.LENGTH_LONG).show();
        finishLoading();
    }
    // -------------------- Callback-Methoden für das Abrufen eines Events --------------------

    // -------------- Callback-Methoden für die Auswahl einer Teilnahme-Sichtbarkeit --------------

    private void joinEvent(final EventParticipationVisibility visibility) {
        joinLeaveEventItem.setVisible(false);
        setProgressBarIndeterminateVisibility(true);
        EventController.joinEvent(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                joinLeaveEventItem.setIcon(R.drawable.ic_action_leave_event);
                joinLeaveEventItem.setTitle(R.string.leave_event);
                // TODO R: Bessere Lösung überlegen, da davon ausgegangen wird, dass PARTICIPANT die Standardrole ist
                event.getMemberList().put(ServiceProvider.getEmail(), EventRole.PARTICIPANT.name());
                event.setParticipationVisibility(visibility.name());
                finish();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(ShowEventActivity.this, R.string.error_joining_event, Toast.LENGTH_LONG).show();
                finish();
            }

            private void finish() {
                joinLeaveEventItem.setVisible(true);
                setProgressBarIndeterminateVisibility(false);
            }
        }, event.getId(), visibility);
    }

    private void changeVisibility(final EventParticipationVisibility visibility) {
        joinLeaveEventItem.setVisible(false);
        settingsItem.setVisible(false);
        setProgressBarIndeterminateVisibility(true);
        EventController.changeParticipationVisibility(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                event.setParticipationVisibility(visibility.name());
                finish();
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(ShowEventActivity.this, R.string.error_changing_visibility, Toast.LENGTH_LONG).show();
                finish();
            }
            private void finish() {
                joinLeaveEventItem.setVisible(true);
                settingsItem.setVisible(true);
                setProgressBarIndeterminateVisibility(false);
            }
        }, event.getId(), visibility);
    }

    // -------------- Callback-Methoden für die Auswahl einer Teilnahme-Sichtbarkeit --------------

    public void onParticipantsClick(View view) {
        Intent intent = new Intent(this, EventParticipantsActivity.class);
        List<String> mails = new LinkedList<>(event.getMemberList().keySet());
        intent.putExtra(EventParticipantsActivity.EXTRA_MAILS, (Serializable) mails);
        startActivity(intent);
    }

    public void onGalleriesClick(View view) {
        Intent intent = new Intent(this, ListPicturesActivity.class);
        intent.putExtra(ListPicturesActivity.EXTRA_CONTAINER_CLASS, Event.class.getSimpleName());
        intent.putExtra(ListPicturesActivity.EXTRA_CONTAINER_ID, event.getId());
        intent.putExtra(ListPicturesActivity.EXTRA_TITLE, event.getTitle());
        startActivity(intent);
    }
}
