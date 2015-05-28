package ws1415.common.component;

import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.EventFilter;
import com.skatenight.skatenightAPI.model.EventMetaData;

import java.util.LinkedList;
import java.util.List;

import ws1415.common.R;

/**
 * Adapter für Events. Erwartet einen Event-Filter als Parameter und fragt anschließend die
 * entsprechenden Events beim Server an. Es kann außerdem angegeben werden, wieviele Events pro
 * Serveraufruf abgerufen werden sollen. Wenn der Benutzer an das Ende der Liste scrollt, werden
 * automatisch weitere Events abgerufen.
 * @author Richard Schulze
 */
public class EventAdapter extends BaseAdapter {
    private EventFilter filter;
    private List<EventMetaData> events = new LinkedList<>();

    public EventAdapter(EventFilter filter) {
        if (filter == null) {
            throw new NullPointerException("no filter submitted");
        }
        this.filter = filter;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EventMetaData event = (EventMetaData) getItem(position);
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = parent.inflate(parent.getContext(), R.layout.listitem_event_meta_data, parent);
        }
        ImageView iconView = (ImageView) view.findViewById(R.id.eventIcon);
        TextView titleView = (TextView) view.findViewById(R.id.eventTitle);
        TextView dateView = (TextView) view.findViewById(R.id.eventDate);
        titleView.setText(event.getTitle());
        dateView.setText(DateFormat.getMediumDateFormat(parent.getContext()).format(event.getDate()));
        return null;
    }

    /**
     * Ruft weitere Events vom Server ab.
     */
    private void queryData() {

    }
}
