package ws1415.ps1415.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import ws1415.ps1415.adapter.EventAdapter;

/**
 * Ein Fragment zur Anzeige einer Liste von Events.
 */
public class EventListFragment extends ListFragment {
    private OnEventClickListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Prüft, ob die Parent-Activity das Callback-Interface implementiert und setzt sie als Listener
        try {
            mListener = (OnEventClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnEventClickListener");
        }
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (!(adapter instanceof EventAdapter)) {
            throw new IllegalArgumentException("adapter has to be an event adapter");
        }
        super.setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mListener.onEventClick(l, v, position, id);
    }

    public interface OnEventClickListener {
        void onEventClick(ListView l, View v, int position, long id);
    }

}
