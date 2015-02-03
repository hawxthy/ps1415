package ws1415.ps1415.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.UsergroupAdapter;
import ws1415.ps1415.task.DeleteUserGroupTask;
import ws1415.ps1415.task.QueryMyUserGroupsTask;
import ws1415.ps1415.task.QueryUserGroupsTask;
import ws1415.ps1415.util.groupUtils;

/**
 * Created by Martin on 30.01.2015.
 */
public class MyUsergroupsFragment extends Fragment implements UsergroupsInterface{
    private ListView groupListView;
    private List<UserGroup> userGroups;
    private UsergroupAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        new QueryMyUserGroupsTask(this).execute();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_user_groups, container, false);

        //ListView initialisieren
        groupListView = (ListView) view.findViewById(R.id.fragment_my_user_groups_list_view);
        if(mAdapter != null) groupListView.setAdapter(mAdapter);

        return view;
    }

    /**
     * Aktualisiert die Liste mit den Gruppen.
     */
    public void refresh(){
        new QueryMyUserGroupsTask(this).execute();
    }

    public void setUserGroupsToListView(List<UserGroup> results){
        userGroups = results;
        mAdapter = new UsergroupAdapter(getActivity(), results);
        if(groupListView != null) groupListView.setAdapter(mAdapter);
    }

    /**
     * Löscht die UserGroup aus der Liste
     *
     * @param usergroup die zu löschende UserGroup
     */
    public void deleteUserGroupFromList(UserGroup usergroup){
        mAdapter.removeListItem(userGroups.indexOf(usergroup));
        userGroups.remove(usergroup);
    }

    /**
     * Löscht die UserGroup vom Server
     *
     * @param usergroup die zu löschende UserGroup
     */
    private void deleteUserGroup(UserGroup usergroup){
        new DeleteUserGroupTask(this).execute(usergroup);
    }
}
