package ws1415.ps1415.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.UsergroupActivity;
import ws1415.ps1415.adapter.UsergroupAdapter;
import ws1415.ps1415.task.DeleteUserGroupTask;
import ws1415.ps1415.task.QueryMyUserGroupsTask;
import ws1415.ps1415.task.QueryUserGroupsTask;
import ws1415.ps1415.util.groupUtils;

/**
 * Dies ist ein Fragment, das genutzt wird um die eigenen Gruppen und die vom Benutzer beigetretenen
 * Gruppen anzuzeigen.
 *
 * Created by Martin on 30.01.2015.
 */
public class MyUsergroupsFragment extends Fragment{
    private ListView groupListView;
    private List<UserGroup> userGroups;
    private UsergroupAdapter mAdapter;
    private AlertDialog c_dialog;

    /**
     * Ruft die eigenen Gruppen und die beigetretenen Gruppen vom Server ab.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        new QueryMyUserGroupsTask(this).execute();
    }

    /**
     * Initialisiert die ListView und setzt die Listener für die Funktionen an einer Gruppe.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_user_groups, container, false);

        //ListView initialisieren
        groupListView = (ListView) view.findViewById(R.id.fragment_my_user_groups_list_view);
        if(mAdapter != null) groupListView.setAdapter(mAdapter);

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UserGroup selectedGroup = mAdapter.getItem(i);
                String userEmail = ServiceProvider.getEmail();
                if(!groupUtils.isCreator(userEmail, selectedGroup)) {
                    if (!groupUtils.isUserInGroup(ServiceProvider.getEmail(), selectedGroup)) {
                        c_dialog = groupUtils.createDialogJoin(MyUsergroupsFragment.this.getActivity(), mAdapter.getItem(i));
                        c_dialog.show();
                    } else {
                        c_dialog = groupUtils.createDialogLeave(MyUsergroupsFragment.this.getActivity(), mAdapter.getItem(i));
                        c_dialog.show();
                    }
                } else {
                    c_dialog = groupUtils.createDialogOwner(MyUsergroupsFragment.this.getActivity(), mAdapter.getItem(i));
                    c_dialog.show();
                }
            }
        });

        groupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String email = ServiceProvider.getEmail();
                UserGroup group = mAdapter.getItem(i);
                if (groupUtils.isCreator(email, group)) {
                    c_dialog = groupUtils.createDialogDelete(MyUsergroupsFragment.this.getActivity(), mAdapter.getItem(i));
                    c_dialog.show();
                } else {
                    c_dialog = groupUtils.createDialogDeleteFailed(MyUsergroupsFragment.this.getActivity(), mAdapter.getItem(i));
                    c_dialog.show();
                }
                return true;
            }
        });
        return view;
    }

    /**
     * Aktualisiert die Liste mit den Gruppen.
     */
    public void refresh(){
        new QueryMyUserGroupsTask(this).execute();
    }

    /**
     * Setzt die Gruppen in die ListView.
     *
     * @param results Liste von Gruppen
     */
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
     * Gibt den zuletzt aufgerufenen Dialog aus.
     * Info: Wird nur für das Testen genutzt.
     *
     * @return Dialog
     */
    public AlertDialog getLastDialog(){
        return c_dialog;
    }
}
