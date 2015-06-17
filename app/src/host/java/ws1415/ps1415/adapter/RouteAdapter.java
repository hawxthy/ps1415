package ws1415.ps1415.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.Route;

import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.RouteController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Adapter zur Verwaltung von Routen. Der Adapter ruft die Routen vom Server ab.
 * @author Richard Schulze
 */
public class RouteAdapter extends BaseAdapter {
    private List<Route> routes = new LinkedList<>();

    /**
     * Erstellt den Adapter und ruft dabei die Liste der Routen vom Server ab.
     * @param handler    Optionaler Handler, der nach dem Abrufen der Routen benachrichtigt wird.
     */
    public RouteAdapter(ExtendedTaskDelegate<Void, List<Route>> handler) {
        refresh(handler);
    }

    @Override
    public int getCount() {
        return routes.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < routes.size()) {
            return routes.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position < routes.size()) {
            return routes.get(position).getId();
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Route route = (Route) getItem(position);
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = parent.inflate(parent.getContext(), R.layout.listitem_route, null);
        }

        TextView routeName = (TextView) view.findViewById(R.id.routeName);
        routeName.setText(route.getName());
        TextView routeLength = (TextView) view.findViewById(R.id.routeLength);
        routeLength.setText(route.getLength());

        return view;
    }

    /**
     * Ruft die Liste der Routen vom Server ab und fügt sie dem Adapter hinzu. Bereits abgerufene
     * Routen werden ersetzt.
     * @param handler    Optionaler Handler, der nach dem Abrufen der Routen benachrichtigt wird.
     */
    public void refresh(final ExtendedTaskDelegate<Void, List<Route>> handler) {
        routes.clear();
        notifyDataSetChanged();
        RouteController.getRoutes(new ExtendedTaskDelegateAdapter<Void, List<Route>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<Route> newRoutes) {
                if (newRoutes != null) {
                    routes.clear();
                    routes.addAll(newRoutes);
                    notifyDataSetChanged();
                }
                if (handler != null) {
                    handler.taskDidFinish(task, newRoutes);
                }
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                if (handler != null) {
                    handler.taskFailed(task, message);
                }
            }
            @Override
            public void taskDidProgress(ExtendedTask task, Void... progress) {
                if (handler != null) {
                    taskDidProgress(task, progress);
                }
            }
        });
    }

    /**
     * Gibt die Route mit dem angegebenen Index zurück.
     * @param position    Der Index der abzurufenden Route.
     * @return Die Route mit dem angegebenen Index.
     */
    public Route getRoute(int position) {
        if (position < routes.size()) {
            return routes.get(position);
        }
        return null;
    }

    /**
     * Entfernt die Route an der angegeben Position aus dem Adapter.
     * @param position    Die Position der zu entfernenden Route.
     */
    public void removeRoute(int position) {
        if (position < routes.size()) {
            routes.remove(position);
            notifyDataSetChanged();
        }
    }
}
