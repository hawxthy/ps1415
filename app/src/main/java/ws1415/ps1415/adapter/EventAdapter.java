package ws1415.ps1415.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.EventFilter;
import com.skatenight.skatenightAPI.model.EventMetaData;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.DiskCacheImageLoader;

/**
 * Adapter für Events. Erwartet einen Event-Filter als Parameter und fragt anschließend die
 * entsprechenden Events beim Server an. Es kann außerdem angegeben werden, wieviele Events pro
 * Serveraufruf abgerufen werden sollen. Wenn der Benutzer an das Ende der Liste scrollt, werden
 * automatisch weitere Events abgerufen.
 * @author Richard Schulze
 */
public class EventAdapter extends BaseAdapter {
    /**
     * View-Type für Event-Views.
     */
    private static final int EVENT_VIEW_TYPE = 0;
    /**
     * View-Type für die View zum Anzeigen des Lade-Icons.
     */
    private static final int LOAD_VIEW_TYPE = 1;

    private Context context;
    private EventFilter filter;
    private List<EventMetaData> events = new LinkedList<>();
    private int fetchDistance;
    private boolean keepFetching = true;

    /**
     * Speichert, ob der Adapter gerade Daten abruft.
     */
    private Boolean fetching = false;

    public EventAdapter(Context context, EventFilter filter) {
        if (filter == null) {
            throw new NullPointerException("no filter submitted");
        }
        if (context == null) {
            throw new NullPointerException("no context submitted");
        }
        this.context = context;
        this.filter = filter;
        fetchDistance = (int) Math.max(filter.getLimit() * 0.4, 1);
        fetchData(true);
    }

    @Override
    public int getCount() {
        return events.size() + (fetching ? 1 : 0);
    }

    @Override
    public EventMetaData getItem(int position) {
        if (fetching && position == events.size()) {
            return null;
        }
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position < events.size()) {
            return events.get(position).getId();
        } else {
            return -1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (position == events.size()) {
            // Ladeanzeige
            if (convertView != null && getItemViewType(position) == LOAD_VIEW_TYPE) {
                view = convertView;
            } else {
                view = View.inflate(parent.getContext(), R.layout.listitem_fetching, null);
            }
        } else {
            EventMetaData event = getItem(position);
            if (convertView != null && getItemViewType(position) == EVENT_VIEW_TYPE) {
                view = convertView;
            } else {
                view = View.inflate(parent.getContext(), R.layout.listitem_event_meta_data, null);
            }
            ImageView iconView = (ImageView) view.findViewById(R.id.eventIcon);
            TextView titleView = (TextView) view.findViewById(R.id.eventTitle);
            TextView dateView = (TextView) view.findViewById(R.id.eventDate);

            DiskCacheImageLoader.getInstance().loadCroppedImage(iconView, event.getIcon(), iconView.getWidth());
            titleView.setText(event.getTitle());
            Date date = new Date(event.getDate().getValue());
            dateView.setText(DateFormat.getMediumDateFormat(context).format(date) + " " + DateFormat.getTimeFormat(context).format(date));
        }

        if (events.size() - position - 1 < fetchDistance) {
            fetchData(false);
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < events.size()) {
            return EVENT_VIEW_TYPE;
        } else {
            return LOAD_VIEW_TYPE;
        }
    }

    /**
     * Ruft weitere Events vom Server ab.
     * @param refresh    Falls true, so wird die Liste der Events aktualisiert, d.h. die Liste wird
     *                   neu vom Server abgerufen und nicht erweitert.
     */
    private void fetchData(boolean refresh) {
        if (!keepFetching || fetching) {
            return;
        }
        synchronized (fetching) {
            if (fetching) {
                return;
            } else {
                fetching = true;
            }
        }

        // Lade-Icon anzeigen lassen
        notifyDataSetChanged();

        if (refresh) {
            filter.setCursorString(null);
        }

        EventController.listEvents(new ExtendedTaskDelegateAdapter<Void, List<EventMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<EventMetaData> newEvents) {
                if (newEvents != null) {
                    events.addAll(newEvents);
                } else {
                    keepFetching = false;
                }
                finish();
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                finish();
            }
            /**
             * Beendet das Abrufen der Daten unabhängig davon, ob das Abrufen erfolgreich war oder
             * fehlgeschlagen ist.
             */
            private void finish() {
                fetching = false;
                EventAdapter.this.notifyDataSetChanged();
            }
        }, filter);
    }

    /**
     * Veranlasst den Adapter die Eventliste neu herunterzuladen. Es wird dabei an den Anfang der
     * Liste gescrollt.
     */
    public void refresh() {
        events.clear();
        keepFetching = true;
        fetchData(true);
    }

    /**
     * Entfernt das Event mit der angegebenen Position aus dem Adapter.
     * @param position    Die Position des zu entfernenden Events.
     */
    public void removeItem(int position) {
        if (position < events.size()) {
            events.remove(position);
            notifyDataSetChanged();
        }
    }
}
