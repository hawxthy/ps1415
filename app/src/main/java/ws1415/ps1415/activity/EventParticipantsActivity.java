package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.EventParticipantsAdapter;
import ws1415.ps1415.model.EventRole;

public class EventParticipantsActivity extends Activity {
    public static final String EXTRA_PARTICIPANTS = EventParticipantsActivity.class.getName() + ".Participants";

    private static final String MEMBER_PARTICIPANTS = EventParticipantsActivity.class.getName() + ".Participants";

    private Map<String, EventRole> roles;
    private EventParticipantsAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_participants);

        if (savedInstanceState != null && savedInstanceState.containsKey(MEMBER_PARTICIPANTS)) {
            roles = (Map<String, EventRole>) savedInstanceState.getSerializable(MEMBER_PARTICIPANTS);
        } else if (getIntent().hasExtra(EXTRA_PARTICIPANTS)) {
            roles = (Map<String, EventRole>) getIntent().getSerializableExtra(EXTRA_PARTICIPANTS);
        } else {
            throw new RuntimeException("intent has to have extra " + EXTRA_PARTICIPANTS);
        }

        List<String> mails = new LinkedList<>(roles.keySet());
        Collections.sort(mails, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return roles.get(lhs).compareTo(roles.get(rhs));
            }
        });
        userAdapter = new EventParticipantsAdapter(mails, roles, this);
        ListView participantsList = (ListView) findViewById(R.id.participantsList);
        participantsList.setAdapter(userAdapter);
        participantsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(EventParticipantsActivity.this, ProfileActivity.class);
                intent.putExtra("email", userAdapter.getItem(position).getEmail());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(MEMBER_PARTICIPANTS, (Serializable) roles);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_participants, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
