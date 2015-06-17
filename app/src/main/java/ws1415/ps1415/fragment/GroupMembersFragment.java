package ws1415.ps1415.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.ProfileActivity;
import ws1415.ps1415.adapter.GroupMemberListAdapter;

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
        final View rootView = inflater.inflate(R.layout.fragment_group_members, container, false);

        mMemberListView = (ListView) rootView.findViewById(R.id.group_profile_members_list_view);
        if (mAdapter != null) mMemberListView.setAdapter(mAdapter);

        return rootView;
    }


    public void setUp(UserGroup group, List<String> rights, final Context context) {
        this.rights = rights;
        members = new ArrayList<>();
        for (String member : group.getMemberRights().keySet()) {
            members.add(member);
        }

        if(mMemberListView != null){
            mMemberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent start_profile_intent = new Intent(context, ProfileActivity.class);
                    start_profile_intent.putExtra("email", mAdapter.getItem(i).getEmail());
                    context.startActivity(start_profile_intent);
                }
            });
        }

        mAdapter = new GroupMemberListAdapter(members, context, rights, null);
        if (mMemberListView != null) mMemberListView.setAdapter(mAdapter);


    }
}
