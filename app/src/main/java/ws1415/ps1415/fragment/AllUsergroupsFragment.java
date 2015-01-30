package ws1415.ps1415.fragment;

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
import ws1415.ps1415.adapter.UsergroupAdapter;
import ws1415.ps1415.task.DeleteUserGroupTask;
import ws1415.ps1415.task.QueryUserGroupsTask;

/**
 * Created by Martin, Bernd on 30.01.2015.
 */
    public class AllUsergroupsFragment extends Fragment {
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
            /**
             * Tut im moment noch nichts
             *
             * @param adapterView
             * @param view
             * @param i
             * @param l
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO Gruppe beitreten implementieren.
                //Intent intent = new Intent(getActivity(), ShowInformationActivity.class);
                //intent.putExtra("event", eventList.get(i).getKey().getId());
                //startActivity(intent);
            }
        });

        userGroupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            /**
             * Läscht die Route vom Server und von der ListView
             *
             * @param adapterView
             * @param view
             * @param i Position der Route in der ListView
             * @param l
             * @return
             */
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO Löschen der Gruppe implementieren.
                //createSelectionsMenu(i);
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
        userGroupListView.setAdapter(mAdapter);
    }

    /**
     * Erstellt einen Dialog, welcher aufgerufen wird, wenn ein Item in der ListView lange
     * ausgewählt wird. In diesem Dialog kann man dann auswählen, ob man die ausgewählte
     * Veranstaltung löschen möchte.
     *
     * @param position
     */
    /*
    private void createSelectionsMenu(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(EventUtils.getInstance(getActivity()).getUniqueField(8, eventList.get(position)).getValue())
                .setItems(R.array.selections_menu, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int index) {
                        if (index == 0) {
                            editEvent(eventList.get(position));
                        }
                        else if (index == 1) {
                            deleteEvent(eventList.get(position));
                        }

                    }
                });
        builder.create();
        builder.show();
    }
    */

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
     * Dient zum refreshen der Liste der aktuellen UserGroups.
     */
    public void refresh(){
        new QueryUserGroupsTask().execute(this);
    }

}
