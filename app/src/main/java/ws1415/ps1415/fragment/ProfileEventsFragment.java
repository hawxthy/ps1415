package ws1415.ps1415.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.EventMetaData;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.ProfileEventAdapter;

/**
 * Das ProfileEventsFragment wird zur Anzeige der Veranstaltungen eines Benutzers verwendet.
 *
 * @author Martin Wrodarczyk
 */
public class ProfileEventsFragment extends Fragment {
    private ListView mEventListView;
    private ProfileEventAdapter mAdapter;

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
     * @param data Informationen
     */
    public void setUpData(List<EventMetaData> data){
        if(this.getActivity() != null) mAdapter = new ProfileEventAdapter(data, this.getActivity());
        if(mEventListView != null) mEventListView.setAdapter(mAdapter);
    }
}
