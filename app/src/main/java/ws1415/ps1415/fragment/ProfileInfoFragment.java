package ws1415.ps1415.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;
import java.util.Map.Entry;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.ProfileInfoAdapter;

/**
 * Das ProfileInfoFragment wird zur Anzeige allgemeiner Informationen eines Benutzers verwendet.
 *
 * @author Martin Wrodarczyk
 */
public class ProfileInfoFragment extends Fragment {
    private ListView mInfoListView;
    private ProfileInfoAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_info, container, false);

        mInfoListView = (ListView) rootView.findViewById(R.id.fragment_info_list);
        mInfoListView.setOnItemClickListener(null);
        if(mAdapter != null) mInfoListView.setAdapter(mAdapter);

        return rootView;
    }

    /**
     * Füllt die Liste mit den übergebenen Informationen.
     *
     * @param data Informationen
     */
    public void setUpData(List<Entry<String, String>> data){
        if(this.getActivity() != null) mAdapter = new ProfileInfoAdapter(data, this.getActivity());
        if(mInfoListView != null) mInfoListView.setAdapter(mAdapter);
    }
}
