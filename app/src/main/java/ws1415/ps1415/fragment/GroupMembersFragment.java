package ws1415.ps1415.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.ArrayList;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.UserListAdapter;

/**
 *
 *
 * @author Bernd Eissing
 */
public class GroupMembersFragment extends Fragment {
    ListView mMemberListView;
    UserListAdapter mAdapter;
    ArrayList<String> members;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_members, container, false);

        mMemberListView = (ListView) rootView.findViewById(R.id.group_profile_members_list_view);
        if(mAdapter != null) mMemberListView.setAdapter(mAdapter);

        return rootView;
    }


    public void setUp(UserGroup group, Context context){
        if(members == null){
            members = new ArrayList<>();
            for(String member : group.getMemberRights().keySet()){
                members.add(member);
            }
        }

        mAdapter = new UserListAdapter(members, context);
        if(mMemberListView != null) mMemberListView.setAdapter(mAdapter);
    }
}
