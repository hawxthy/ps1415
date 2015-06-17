package ws1415.ps1415.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import ws1415.ps1415.adapter.RouteAdapter;

/**
 * Zeigt eine Liste der auf dem Server existierenden Routen an.
 */
public class RouteListFragment extends ListFragment implements AdapterView.OnItemLongClickListener {
    private OnRouteClickListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnRouteClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnRouteClickListener");
        }
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (!(adapter instanceof RouteAdapter)) {
            throw new IllegalArgumentException("adapter has to be a route adapter");
        }
        super.setListAdapter(adapter);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mListener.onRouteClick(l, v, position, id);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return mListener.onRouteLongClick(parent, view, position, id);
    }

    /**
     * Interface für Callback-Methoden in verwendenden Activities.
     */
    public interface OnRouteClickListener {
        void onRouteClick(ListView l, View v, int position, long id);
        boolean onRouteLongClick(AdapterView<?> parent, View v, int position, long id);
    }

}
