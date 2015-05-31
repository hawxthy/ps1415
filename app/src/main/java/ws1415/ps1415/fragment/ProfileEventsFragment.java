package ws1415.ps1415.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.EventFilter;

import ws1415.ps1415.R;
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
