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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.EventParticipantsAdapter;
import ws1415.ps1415.adapter.UserListAdapter;
import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.model.EventRole;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

public class ManageEventParticipantsActivity extends Activity {
    public static final String EXTRA_EVENT_ID = ManageEventParticipantsActivity.class.getName() + ".EventId";

    private static final String MEMBER_EVENT_ID = ManageEventParticipantsActivity.class.getName() + ".EventId";

    private ListView participantsList;
    private EventParticipantsAdapter userAdapter;

    private long eventId;
    private Map<String, EventRole> participantRoles = new HashMap<>();

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

        if (savedInstanceState != null && savedInstanceState.containsKey(MEMBER_EVENT_ID)) {
            eventId = savedInstanceState.getLong(EXTRA_EVENT_ID, -1);
        } else if (getIntent().hasExtra(EXTRA_EVENT_ID)) {
            eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
        } else {
            throw new RuntimeException("intent has to contain an extra " + EXTRA_EVENT_ID);
        }

        startLoading();
        EventController.getEvent(new ExtendedTaskDelegateAdapter<Void, EventData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, EventData eventData) {
                // Teilnehmer-Map mit Rollen dekodieren
                participantRoles = new HashMap<>();
                for (String key : eventData.getMemberList().keySet()) {
                    if (!key.equals("etag") && !key.equals("kind")) {
                        participantRoles.put(key, EventRole.valueOf((String) eventData.getMemberList().get(key)));
                    }
                }
                List<String> mails = new LinkedList<>(participantRoles.keySet());
                Collections.sort(mails, new Comparator<String>() {
                    @Override
                    public int compare(String lhs, String rhs) {
                        return participantRoles.get(lhs).compareTo(participantRoles.get(rhs));
                    }
                });
                userAdapter = new EventParticipantsAdapter(mails, participantRoles, ManageEventParticipantsActivity.this);
                participantsList.setAdapter(userAdapter);
                finalizeLoading();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(ManageEventParticipantsActivity.this, message, Toast.LENGTH_LONG).show();
                finalizeLoading();
            }
        }, eventId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MEMBER_EVENT_ID, eventId);
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
        final int initialSelection = participantRoles.get(mail).ordinal();
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
                participantRoles.put(mail, eventRole);
                userAdapter.notifyDataSetChanged();
                finalizeLoading();
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(ManageEventParticipantsActivity.this, message, Toast.LENGTH_LONG).show();
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
