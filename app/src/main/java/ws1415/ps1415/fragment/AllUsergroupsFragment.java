package ws1415.ps1415.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import ws1415.ps1415.task.JoinUserGroupTask;
import ws1415.ps1415.task.LeaveUserGroupTask;
import ws1415.ps1415.task.QueryUserGroupsTask;
import ws1415.ps1415.util.groupUtils;

/**
 * Created by Martin, Bernd on 30.01.2015.
 */
    public class AllUsergroupsFragment extends Fragment implements UsergroupsInterface{
    private ListView userGroupListView;
    private List<UserGroup> userGroupList;
    private UsergroupAdapter mAdapter;

    /**
     * Fragt alle UserGroups vom Server ab und fügt diese in die Liste ein
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        new QueryUserGroupsTask().execute(this);
    }

    /**
     * Setzt die Events in die Liste
     */
    @Override
    public void onResume(){
        super.onResume();

        userGroupListView.setAdapter(mAdapter);
    }

    /**
     *
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_user_groups, container, false);

        // ListView initialisieren
        userGroupListView = (ListView) view.findViewById(R.id.fragment_show_user_groups_list_view);

        userGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UserGroup selectedGroup = mAdapter.getItem(i);
                String userEmail = ServiceProvider.getEmail();
                if(!groupUtils.isCreator(userEmail, selectedGroup)) {
                    if (!groupUtils.isUserInGroup(ServiceProvider.getEmail(), selectedGroup)) {
                        groupUtils.createDialogJoin(i, mAdapter, AllUsergroupsFragment.this);
                    } else {
                        groupUtils.createDialogLeave(i, mAdapter, AllUsergroupsFragment.this);
                    }
                } else {
                    groupUtils.createDialogOwner(i, mAdapter, AllUsergroupsFragment.this);
                }
            }
        });

        userGroupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String email = ServiceProvider.getEmail();
                UserGroup group = mAdapter.getItem(i);
                if(groupUtils.isCreator(email, group)){
                    groupUtils.createDialogDelete(i, mAdapter, AllUsergroupsFragment.this);
                } else {
                    groupUtils.createDialogDeleteFailed(i, mAdapter, AllUsergroupsFragment.this);
                }
                return true;
            }
        });

        return view;
    }

    /**
     * Füllt die ListView mit den UserGroups vom Server.
     *
     * @param results ArrayList von UserGroups
     */
    public void setUserGroupsToListView(List<UserGroup> results) {
        userGroupList = results;
        mAdapter = new UsergroupAdapter(getActivity(), results);
        if(userGroupListView != null) {
            userGroupListView.setAdapter(mAdapter);
        }
    }

    /**
     * Löscht die UserGroup aus der Liste
     *
     * @param usergroup die zu löschende UserGroup
     */
    public void deleteUserGroupFromList(UserGroup usergroup){
        mAdapter.removeListItem(userGroupList.indexOf(usergroup));
        userGroupList.remove(usergroup);
    }

    /**
     * Löscht die UserGroup vom Server
     *
     * @param usergroup die zu löschende UserGroup
     */
    private void deleteUserGroup(UserGroup usergroup){
        new DeleteUserGroupTask(this).execute(usergroup);
    }

    /**
     * Erstellt das ActionBar Menu
     *
     * @param menu
     * @param menuInflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.add_user_group_plus_button, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    /**
     * Dient zum Refreshen der Liste der aktuellen UserGroups.
     */
    public void refresh(){
        new QueryUserGroupsTask().execute(this);
    }

}
