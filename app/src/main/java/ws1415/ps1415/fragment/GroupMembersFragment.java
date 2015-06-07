package ws1415.ps1415.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.GroupMemberListAdapter;
import ws1415.ps1415.adapter.UserListAdapter;

/**
 * @author Bernd Eissing
 */
public class GroupMembersFragment extends Fragment {
    private ListView mMemberListView;
    private GroupMemberListAdapter mAdapter;
    private ArrayList<String> members;
    private List<String> rights;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_members, container, false);

        mMemberListView = (ListView) rootView.findViewById(R.id.group_profile_members_list_view);
        mMemberListView.setClickable(true);
        if (mAdapter != null) mMemberListView.setAdapter(mAdapter);

        return rootView;
    }


    public void setUp(UserGroup group, List<String> rights, Context context) {
        this.rights = rights;
        members = new ArrayList<>();
        for (String member : group.getMemberRights().keySet()) {
            members.add(member);
        }

        mAdapter = new GroupMemberListAdapter(members, context, rights, null);
        if (mMemberListView != null) mMemberListView.setAdapter(mAdapter);
    }
}
