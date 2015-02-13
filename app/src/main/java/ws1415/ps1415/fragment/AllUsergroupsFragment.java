package ws1415.ps1415.fragment;

import android.app.AlertDialog;
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
import ws1415.ps1415.task.QueryUserGroupsTask;
import ws1415.ps1415.util.groupUtils;

/**
 * Dies ist ein Fragment, das genutzt wird um alle Gruppen auf dem Server anzuzeigen.
 *
 * @author Martin Wrodarczyk, Bernd Eissing
 */
public class AllUsergroupsFragment extends Fragment {
    private ListView userGroupListView;
    private List<UserGroup> userGroupList;
    private UsergroupAdapter mAdapter;
    private AlertDialog c_dialog;

    /**
     * Fragt alle UserGroups vom Server ab und fügt diese in die Liste ein.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new QueryUserGroupsTask().execute(this);
    }

    /**
     * Setzt die Events in die Liste
     */
    @Override
    public void onResume() {
        super.onResume();
        userGroupListView.setAdapter(mAdapter);
    }

    /**
     * Initilisiert die ListView und setzt die Listener für die Funktionen an einer Gruppe.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_user_groups, container, false);

        // ListView initialisieren
        userGroupListView = (ListView) view.findViewById(R.id.fragment_show_user_groups_list_view);

        userGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UserGroup selectedGroup = mAdapter.getItem(i);
                String userEmail = ServiceProvider.getEmail();
                if (!groupUtils.isCreator(userEmail, selectedGroup)) {
                    if (!groupUtils.isUserInGroup(ServiceProvider.getEmail(), selectedGroup)) {
                        c_dialog = groupUtils.createDialogJoin(AllUsergroupsFragment.this.getActivity(), mAdapter.getItem(i));
                        c_dialog.show();
                    } else {
                        c_dialog = groupUtils.createDialogLeave(AllUsergroupsFragment.this.getActivity(), mAdapter.getItem(i));
                        c_dialog.show();
                    }
                } else {
                    c_dialog = groupUtils.createDialogOwner(AllUsergroupsFragment.this.getActivity(), mAdapter.getItem(i));
                    c_dialog.show();
                }
            }
        });

        userGroupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String email = ServiceProvider.getEmail();
                UserGroup group = mAdapter.getItem(i);
                if (groupUtils.isCreator(email, group)) {
                    c_dialog = groupUtils.createDialogDelete(AllUsergroupsFragment.this.getActivity(), mAdapter.getItem(i));
                    c_dialog.show();
                } else {
                    c_dialog = groupUtils.createDialogDeleteFailed(AllUsergroupsFragment.this.getActivity(), mAdapter.getItem(i));
                    c_dialog.show();
                }
                return true;
            }
        });

        return view;
    }

    /**
     * Dient zum Refreshen der Liste der aktuellen UserGroups.
     */
    public void refresh() {
        new QueryUserGroupsTask().execute(this);
    }

    /**
     * Gibt den zuletzt aufgerufenen Dialog aus.
     * Info: Wird nur für das Testen genutzt.
     *
     * @return Dialog
     */
    public AlertDialog getLastDialog() {
        return c_dialog;
    }

    /**
     * Gibt die ListView zurück.
     *
     * @return ListView
     */
    public ListView getListView() {
        return userGroupListView;
    }

    /**
     * Füllt die ListView mit den UserGroups vom Server.
     *
     * @param results ArrayList von UserGroups
     */
    public void setUserGroupsToListView(List<UserGroup> results) {
        userGroupList = results;
        mAdapter = new UsergroupAdapter(getActivity(), results, -1);
        if (userGroupListView != null) userGroupListView.setAdapter(mAdapter);
    }

    /**
     * Löscht die UserGroup aus der Liste
     *
     * @param usergroup die zu löschende UserGroup
     */
    public void deleteUserGroupFromList(UserGroup usergroup) {
        mAdapter.removeListItem(usergroup);
        userGroupList.remove(usergroup);
    }

    /**
     * Erstellt das ActionBar Menu
     *
     * @param menu
     * @param menuInflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.add_user_group_plus_button, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }
}
