package ws1415.ps1415.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.GroupMetaData;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.ProfileGroupAdapter;

/**
 * Das ProfileGroupsFragment wird zur Anzeige der Gruppen eines Benutzers verwendet.
 *
 * @author Martin Wrodarczyk
 */
public class ProfileGroupsFragment extends Fragment {
    private ListView mGroupListView;
    private ProfileGroupAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_profile_groups, container, false);

        mGroupListView = (ListView) rootView.findViewById(R.id.fragment_group_list);
        if(mAdapter != null) mGroupListView.setAdapter(mAdapter);

        return rootView;
    }

    /**
     * Füllt die Liste mit den übergebenen Informationen.
     *
     * @param data Informationen
     */
    public void setUpData(List<GroupMetaData> data){
        mAdapter = new ProfileGroupAdapter(data, this.getActivity());
        if(mGroupListView != null) mGroupListView.setAdapter(mAdapter);
    }
}
