package ws1415.ps1415.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.UserGroupFilter;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.GroupProfileActivity;
import ws1415.ps1415.adapter.UsergroupAdapter;

/**
 * Das ProfileGroupsFragment wird zur Anzeige der Gruppen eines Benutzers verwendet.
 *
 * @author Martin Wrodarczyk
 */
public class ProfileGroupsFragment extends Fragment {
    private ListView mGroupListView;
    private UsergroupAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_profile_groups, container, false);

        mGroupListView = (ListView) rootView.findViewById(R.id.fragment_group_list);
        mGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent open_group_profile_intent = new Intent(ProfileGroupsFragment.this.getActivity(), GroupProfileActivity.class);
                open_group_profile_intent.putExtra("groupName", mAdapter.getItem(i).getName());
                open_group_profile_intent.putExtra("groupCreator", mAdapter.getItem(i).getCreator());
                startActivity(open_group_profile_intent);
            }
        });
        if(mAdapter != null) mGroupListView.setAdapter(mAdapter);

        return rootView;
    }

    /**
     * Füllt die Liste mit den übergebenen Informationen.
     *
     * @param data Informationen
     */
    public void setUpData(List<String> data){
        UserGroupFilter filter = new UserGroupFilter();
        filter.setLimit(15);
        if(this.getActivity() != null) mAdapter = new UsergroupAdapter(this.getActivity(), filter, data);
        if(mGroupListView != null) mGroupListView.setAdapter(mAdapter);
    }
}
