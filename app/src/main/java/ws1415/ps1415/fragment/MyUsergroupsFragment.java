package ws1415.ps1415.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.UsergroupAdapter;
import ws1415.ps1415.task.QueryMyUserGroupsTask;
import ws1415.ps1415.util.PrefManager;
import ws1415.ps1415.util.groupUtils;

/**
 * Dies ist ein Fragment, das genutzt wird um die eigenen Gruppen und die vom Benutzer beigetretenen
 * Gruppen anzuzeigen.
 *
 * @author Martin Wrodarczyk
 */
public class MyUsergroupsFragment extends Fragment{
    private ListView visibleGroupsListView;
    private ListView notVisibleGroupsListView;
    private UsergroupAdapter mAdapterVisible;
    private UsergroupAdapter mAdapterNotVisible;
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
        visibleGroupsListView = (ListView) view.findViewById(R.id.fragment_my_user_groups_list_view_visible);
        notVisibleGroupsListView = (ListView) view.findViewById(R.id.fragment_my_user_groups_list_view_not_visible);
        if(mAdapterVisible != null) visibleGroupsListView.setAdapter(mAdapterVisible);
        if(mAdapterNotVisible != null) notVisibleGroupsListView.setAdapter(mAdapterNotVisible);

        visibleGroupsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                createSelectionsMenu(i, mAdapterVisible);
                return true;
            }
        });

        notVisibleGroupsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                createSelectionsMenu(i, mAdapterNotVisible);
                return true;
            }
        });
        return view;
    }

    private void createSelectionsMenu(final int position, final UsergroupAdapter adapter){
        final UserGroup userGroup = adapter.getItem(position);
        String[] items;

        if(groupUtils.isCreator(ServiceProvider.getEmail(), userGroup)){
            items = new String[3];
            items[2] = getString(R.string.context_menu_delete);
        } else{
            items = new String[2];
        }
        if (adapter == mAdapterVisible) {
            items[0] = getString(R.string.context_menu_not_visible);
        }else{
            items[0] = getString(R.string.context_menu_visible);
        }
        items[1] = getString(R.string.context_menu_leave_usergroup);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(userGroup.getName());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case 0:
                        if(adapter == mAdapterVisible){
                            if(mAdapterNotVisible.addListItem(userGroup)){
                                mAdapterVisible.removeListItem(userGroup);
                                PrefManager.setGroupVisibility(MyUsergroupsFragment.this.getActivity(), userGroup.getName(), false);
                            }
                        }else{
                            if(mAdapterVisible.addListItem(userGroup)) {
                                mAdapterNotVisible.removeListItem(userGroup);
                                PrefManager.setGroupVisibility(MyUsergroupsFragment.this.getActivity(), userGroup.getName(), true);
                            }
                        }
                        break;
                    case 1:
                        if (!groupUtils.isCreator(ServiceProvider.getEmail(), userGroup)) {
                            c_dialog = groupUtils.createDialogLeave(MyUsergroupsFragment.this.getActivity(), userGroup);
                            c_dialog.show();
                        }else{
                            c_dialog = groupUtils.createDialogOwner(MyUsergroupsFragment.this.getActivity(), userGroup);
                            c_dialog.show();
                        }
                        break;
                    case 2:
                        c_dialog = groupUtils.createDialogDelete(MyUsergroupsFragment.this.getActivity(), userGroup);
                        c_dialog.show();
                        break;
                }
            }
        });
        c_dialog = builder.create();
        c_dialog.show();
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
        if (results == null) {
            results = new ArrayList<>();
        }

        // Einteilen
        List<UserGroup> tmpVisible = new ArrayList<>();
        List<UserGroup> tmpNotVisible = new ArrayList<>();

        for(int i = 0; i < results.size(); i++){
            if(PrefManager.getGroupVisibility(getActivity(), results.get(i).getName())){
                tmpVisible.add(results.get(i));
            }else{
                tmpNotVisible.add(results.get(i));
            }
        }
        mAdapterVisible = new UsergroupAdapter(getActivity(), tmpVisible, 5);
        mAdapterNotVisible = new UsergroupAdapter(getActivity(), tmpNotVisible, -1);
        if(visibleGroupsListView != null) visibleGroupsListView.setAdapter(mAdapterVisible);
        if(notVisibleGroupsListView != null) notVisibleGroupsListView.setAdapter(mAdapterNotVisible);
    }

    /**
     * Löscht die UserGroup aus der Liste
     *
     * @param usergroup die zu löschende UserGroup
     */
    public void deleteUserGroupFromList(UserGroup usergroup){
        mAdapterVisible.removeListItem(usergroup);
        mAdapterNotVisible.removeListItem(usergroup);
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
