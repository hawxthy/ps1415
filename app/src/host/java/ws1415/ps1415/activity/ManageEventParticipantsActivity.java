package ws1415.ps1415.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.EventData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.UserListAdapter;
import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.model.EventRole;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

public class ManageEventParticipantsActivity extends Activity {
    public static final String EXTRA_EVENT_ID = ManageEventParticipantsActivity.class.getName() + ".EventId";

    private ListView participantsList;
    private UserListAdapter userAdapter;

    private long eventId;
    private Map<String, Object> participantRoles = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_manage_event_participants);

        participantsList = (ListView) findViewById(R.id.participantsList);
        participantsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onParticipantClick(userAdapter.getItem(position).getEmail());
            }
        });

        if (getIntent().hasExtra(EXTRA_EVENT_ID)) {
            eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
            startLoading();
            EventController.getEvent(new ExtendedTaskDelegateAdapter<Void, EventData>() {
                @Override
                public void taskDidFinish(ExtendedTask task, EventData eventData) {
                    userAdapter = new UserListAdapter(new LinkedList<>(eventData.getMemberList().keySet()),
                            ManageEventParticipantsActivity.this);
                    participantsList.setAdapter(userAdapter);
                    participantRoles.putAll(eventData.getMemberList());
                    finalizeLoading();
                }
                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    Toast.makeText(ManageEventParticipantsActivity.this, R.string.error_loading_participants, Toast.LENGTH_LONG).show();
                    finalizeLoading();
                }
            }, eventId);
        } else {
            throw new RuntimeException("intent has to contain an extra " + EXTRA_EVENT_ID);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_event_participants, menu);
        return true;
    }

    /**
     * Wird aufgerufen, wenn auf einen Benutzer geklickt wird.
     * @param mail    Die E-Mail des Benutzers, auf den geklickt wurde.
     */
    private void onParticipantClick(final String mail) {
        final int initialSelection = EventRole.valueOf(participantRoles.get(mail).toString()).ordinal();
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            private int selection = initialSelection;
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    changeRole(mail, EventRole.values()[selection]);
                } else {
                    selection = which;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_role)
                .setSingleChoiceItems(R.array.event_roles, initialSelection, clickListener)
                .setPositiveButton(R.string.save, clickListener)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    private void changeRole(final String mail, final EventRole eventRole) {
        startLoading();
        EventController.assignRole(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                participantRoles.put(mail, eventRole.name());
                finalizeLoading();
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(ManageEventParticipantsActivity.this, R.string.error_changing_role, Toast.LENGTH_LONG).show();
                finalizeLoading();
            }
        }, eventId, mail, eventRole);
    }

    /**
     * Startet die Ladeanimation.
     */
    private void startLoading() {
        findViewById(R.id.participantsLoading).setVisibility(View.VISIBLE);
    }
    /**
     * Beendet die Ladeanimation.
     */
    private void finalizeLoading() {
        findViewById(R.id.participantsLoading).setVisibility(View.GONE);
    }
}
