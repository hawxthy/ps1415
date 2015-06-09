package ws1415.ps1415.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.EventFilter;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.ShowEventActivity;
import ws1415.ps1415.adapter.EventAdapter;

/**
 * Das ProfileEventsFragment wird zur Anzeige der Veranstaltungen eines Benutzers verwendet.
 *
 * @author Martin Wrodarczyk
 */
public class ProfileEventsFragment extends Fragment {
    private ListView mEventListView;
    private EventAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_profile_events, container, false);

        mEventListView = (ListView) rootView.findViewById(R.id.fragment_event_list);
        mEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ProfileEventsFragment.this.getActivity(), ShowEventActivity.class);
                intent.putExtra(ShowEventActivity.EXTRA_EVENT_ID, mAdapter.getItemId(i));
                startActivity(intent);
            }
        });
        if(mAdapter != null) mEventListView.setAdapter(mAdapter);

        return rootView;
    }

    /**
     * Füllt die Liste mit den übergebenen Informationen.
     *
     * @param userMail E-Mail Adresse des Benutzers
     */
    public void setUpData(String userMail){
        EventFilter eventFiler = new EventFilter();
        eventFiler.setUserId(userMail);
        eventFiler.setLimit(10);
        if(this.getActivity() != null) mAdapter = new EventAdapter(this.getActivity(), eventFiler);
        if(mEventListView != null) mEventListView.setAdapter(mAdapter);
    }
}
