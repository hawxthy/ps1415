package ws1415.ps1415.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.UserListAdapter;

public class EventParticipantsActivity extends Activity {
    public static final String EXTRA_MAILS = EventParticipantsActivity.class.getName() + ".Mails";
    private UserListAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_participants);

        // "Zurück"-Button in der Actionbar anzeigen
        ActionBar mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setHomeButtonEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        List<String> mails = (List<String>) getIntent().getSerializableExtra(EXTRA_MAILS);
        userAdapter = new UserListAdapter(mails, this);
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
