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

/**
 * Created by Martin, Bernd on 30.01.2015.
 */
    public class AllUsergroupsFragment extends Fragment {
    private ListView userGroupListView;
    private List<UserGroup> userGroupList;
    private UsergroupAdapter mAdapter;
    AlertDialog c_dialog;

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
                if(!isCreator(userEmail, selectedGroup)) {
                    if (!isUserInGroup(ServiceProvider.getEmail(), selectedGroup)) {
                        createDialogJoin(i);
                    } else {
                        createDialogLeave(i);
                    }
                } else {
                    createDialogOwner(i);
                }
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
        userGroupListView.setAdapter(mAdapter);
    }

    /**
     * Erstellt einen Dialog, welcher aufgerufen wird, wenn ein Item in der ListView lange
     * ausgewählt wird. In diesem Dialog kann man dann auswählen, ob man der ausgewählten
     * Gruppe beitreten möchte.
     *
     * @param position
     */
    private void createDialogJoin(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mAdapter.getItem(position).getName());
        builder.setMessage(R.string.dialog_join_group);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new JoinUserGroupTask(AllUsergroupsFragment.this).execute(mAdapter.getItem(position).getName());
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        c_dialog = builder.create();
        builder.show();
    }

    /**
     * Erstellt einen Dialog, welcher aufgerufen wird, wenn ein Item in der ListView lange
     * ausgewählt wird. In diesem Dialog kann man dann auswählen, ob man die ausgewählten
     * Gruppe verlassen möchte.
     *
     * @param position
     */
    private void createDialogLeave(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mAdapter.getItem(position).getName());
        builder.setMessage(R.string.dialog_leave_group);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new LeaveUserGroupTask(AllUsergroupsFragment.this).execute(mAdapter.getItem(position).getName());
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create();
        builder.show();
    }

    /**
     * Erstellt einen Dialog, der den Benutzer darauf hinweist, dass er seiner selbst erstellten
     * Gruppen nicht verlassen kann.
     */
    private void createDialogOwner(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mAdapter.getItem(position).getName());
        builder.setMessage(R.string.dialog_group_owner);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create();
        builder.show();
    }

    /**
     * Prüft ob der Benutzer mit der angegebenen Email-Adresse Mitglied in der Gruppe ist.
     *
     * @param email Email-Adresse des Benutzers
     * @param group Gruppe
     * @return
     */
    private boolean isUserInGroup(String email, UserGroup group){
        List<String> members = group.getMembers();
        for(int i=0; i<members.size(); i++){
            if(members.get(i).equals(email)) return true;
        }
        return false;
    }

    /**
     * Prüft ob der Benutzer mit der angegebenen Email-Adresse der Ersteller der Gruppe ist.
     *
     * @param email Email-Adresse des Benutzers
     * @param group Gruppe
     * @return
     */
    private boolean isCreator(String email, UserGroup group){
        String creator = group.getCreator().getEmail();
        if(creator.equals(email)) return true;
        return false;
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

    public AlertDialog getLastDialog(){
        return c_dialog;
    }

    public ListView getListView(){
        return userGroupListView;
    }
}
