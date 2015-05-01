package ws1415.veranstalterapp.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.Event;

import java.util.List;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.task.QueryEventsTask;
import ws1415.veranstalterapp.adapter.EventsCursorAdapter;
import ws1415.veranstalterapp.activity.EditEventActivity;
import ws1415.veranstalterapp.R;
import ws1415.common.task.DeleteEventTask;
import ws1415.veranstalterapp.util.EventUtils;

/**
 * Dieses Fragment wird dazu genutzt, eine Liste von Veranstaltungen anzuzeigen.
 *
 * @author Bernd Eissing, Martin Wrodarczyk
 */
public class ShowEventsFragment extends Fragment {
    private ListView eventListView;
    private List<Event> eventList;

    private EventsCursorAdapter mAdapter;

    /**
     * Fragt alle Events vom Server ab und fügt diese in die Liste ein
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        new QueryEventsTask(new ExtendedTaskDelegateAdapter<Void, List<Event>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<Event> events) {
                setEventsToListView(events);
            }
        }).execute();
    }

    /**
     * Setzt die Events in die Liste
     */
    @Override
    public void onResume(){
        super.onResume();
        eventListView.setAdapter(mAdapter);
    }

    /**
     * Initialisiert die ListView und setzt die Listener auf die ListView.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_events, container, false);

        eventListView = (ListView) view.findViewById(R.id.fragment_show_events_list_view);
        eventListView.setAdapter(mAdapter);

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Ruft die showRouteActivity auf, die die ausgewählte Route anzeigt.
             *
             * @param adapterView
             * @param view
             * @param i Index der ausgewählten Route in der ListView
             * @param l
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ShowInformationActivity.class);
                intent.putExtra("event", eventList.get(i).getKey().getId());
                startActivity(intent);
            }
        });

        eventListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
                createSelectionsMenu(i);
                return true;
            }
        });

        return view;
    }

    /**
     * Füllt die ListView mit den Events vom Server.
     *
     * @param results ArrayList von Events
     */
    public void setEventsToListView(List<Event> results) {
        eventList = results;
        mAdapter = new EventsCursorAdapter(getActivity(), results);
        eventListView.setAdapter(mAdapter);
    }

    /**
     * Erstellt einen Dialog, welcher aufgerufen wird, wenn ein Item in der ListView lange
     * ausgewählt wird. In diesem Dialog kann man dann auswählen, ob man die ausgewählte
     * Veranstaltung löschen möchte.
     *
     * @param position
     */
    private void createSelectionsMenu(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(EventUtils.getInstance(getActivity()).getUniqueField(8, eventList.get(position)).getValue())
                .setItems(R.array.selections_menu, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int index) {
                        if (index == 0) {
                            editEvent(eventList.get(position));
                        } else if (index == 1) {
                            deleteEvent(eventList.get(position));
                        }

                    }
                });
        builder.create();
        builder.show();
    }

    /**
     * Löscht das Event vom Server
     *
     * @param event das zu löschende Event
     */
    public void deleteEvent(final Event event){
        new DeleteEventTask(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                if (aBoolean != null && aBoolean == true) {
                    mAdapter.removeListItem(eventList.indexOf(event));
                    eventList.remove(event);
                }
            }
        }).execute(event);
    }


    public void editEvent(Event event){
        Intent intent = new Intent(getActivity(), EditEventActivity.class);
        intent.putExtra("event", event.getKey().getId());
        startActivity(intent);
    }

    public EventsCursorAdapter getmAdapter() {
        return mAdapter;
    }
}
