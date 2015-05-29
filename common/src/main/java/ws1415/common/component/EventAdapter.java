package ws1415.common.component;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.EventFilter;
import com.skatenight.skatenightAPI.model.EventMetaData;
import com.skatenight.skatenightAPI.model.PictureMetaData;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ws1415.common.R;
import ws1415.common.controller.EventController;
import ws1415.common.controller.GalleryController;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

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
        fetchDistance = (int) (filter.getLimit() * 0.4);
        fetchData();
    }

    @Override
    public int getCount() {
        return events.size() + (fetching ? 1 : 0);
    }

    @Override
    public Object getItem(int position) {
        if (fetching && position == events.size()) {
            return null;
        }
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO
        return 0;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (position == events.size()) {
            // Ladeanzeige
            if (convertView != null && getItemViewType(position) == LOAD_VIEW_TYPE) {
                view = convertView;
            } else {
                view = parent.inflate(parent.getContext(), R.layout.listitem_fetching, null);
            }
        } else {
            EventMetaData event = (EventMetaData) getItem(position);
            if (convertView != null && getItemViewType(position) == EVENT_VIEW_TYPE) {
                view = convertView;
            } else {
                view = parent.inflate(parent.getContext(), R.layout.listitem_event_meta_data, null);
            }
            ImageView iconView = (ImageView) view.findViewById(R.id.eventIcon);
            TextView titleView = (TextView) view.findViewById(R.id.eventTitle);
            TextView dateView = (TextView) view.findViewById(R.id.eventDate);
            titleView.setText(event.getTitle());
            dateView.setText(DateFormat.getMediumDateFormat(parent.getContext()).format(new Date(event.getDate().getValue())));
        }

        if (events.size() - position < fetchDistance) {
            fetchData();
        }

        return view;
    }

    /**
     * Ruft weitere Events vom Server ab.
     */
    private void fetchData() {
        // TODO Geht der synchronized-Block besser?
        if (fetching) {
            return;
        }
        synchronized (fetching) {
            if (fetching) {
                return;
            } else {
                fetching = true;
            }
        }

        Log.d(EventAdapter.class.getName(), "fetching data");

        // Lade-Icon anzeigen lassen
        notifyDataSetChanged();

        EventController.listEvents(new ExtendedTaskDelegateAdapter<Void, List<EventMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<EventMetaData> newEvents) {
                if (newEvents != null) {
                    events.addAll(newEvents);
                } else {
                    fetchDistance = 0;
                }
                finish();
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                // TODO Ggf. Fehlermeldung (Toast) anzeigen
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

    public int getFetchDistance() {
        return fetchDistance;
    }

    public void setFetchDistance(int fetchDistance) {
        this.fetchDistance = fetchDistance;
    }
}
