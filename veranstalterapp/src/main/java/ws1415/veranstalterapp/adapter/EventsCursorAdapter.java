package ws1415.veranstalterapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.util.EventUtils;
import ws1415.veranstalterapp.util.FieldType;

/**
 * Klasse zum Füllen der ListView in ShowEventsFragment.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 18.11.2014.
 */
public class EventsCursorAdapter extends BaseAdapter{
    private List<Event> eventList = new ArrayList<Event>();
    private Context mContext;
    private LayoutInflater inflater;

    private SimpleDateFormat eventDateFormat;

    /**
     * Konstruktor, der den Inhalt der Liste festlegt.
     *
     * @param context Context, von dem aus der Adapter aufgerufen wird
     * @param eventList Liste von den Events
     */
    public EventsCursorAdapter(Context context, List<Event> eventList){
        mContext = context;
        this.eventList = eventList;

        eventDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    }

    /**
     * Gibt die Anzahl der Events in der Liste zurück.
     *
     * @return Anz. der Events
     */
    @Override
    public int getCount() {
        if(eventList == null){
            return 0;
        }else{
            return eventList.size();
        }
    }

    /**
     * Gibt das event and der Stelle i in der Liste zurück.
     *
     * @param i Stelle
     * @return das Event
     */
    @Override
    public Event getItem(int i) {
        return eventList.get(i);
    }

    /**
     * Gibt die Id des Events in der Liste zurück
     *
     * @param i Stelle des Events
     * @return Id des EVents
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Klasse zum Halten der GUI Elemente, damit keine Kopien erstellt werden.
     */
    private class Holder{
        private TextView eventName;
        private TextView eventDate;
        private TextView eventFee;
        private TextView eventLocation;
    }

    /**
     * Setzt das Layout der Items in der ListView
     *
     * @param position Position in der ListView un in der ArrayList
     * @param convertView Position in der ListView mit den Daten aus der ArrayList
     * @param viewGroup Die Liste welche die Maps hält
     * @return Das Item in der Liste von den Maps
     */
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Holder holder;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_view_item_event_layout, viewGroup, false);
            holder.eventName = (TextView) convertView.findViewById(R.id.list_view_item_event_layout_name);
            holder.eventLocation = (TextView) convertView.findViewById(R.id.list_view_item_event_layout_location);
            holder.eventDate = (TextView) convertView.findViewById(R.id.list_view_item_event_layout_date);
            holder.eventFee = (TextView) convertView.findViewById(R.id.list_view_item_event_layout_fee);
            convertView.setTag(holder);
        }else{
            holder = (Holder)convertView.getTag();
        }
        String eventLocationText = EventUtils.getInstance(mContext).getUniqueField(FieldType.LOCATION.getId(), getItem(position)).getValue();
        String eventFeeText = EventUtils.getInstance(mContext).getUniqueField(FieldType.FEE.getId(), getItem(position)).getValue();
        holder.eventName.setText(EventUtils.getInstance(mContext).getUniqueField(FieldType.TITLE.getId(), getItem(position)).getValue());
        holder.eventDate.setText(eventDateFormat.format(EventUtils.getInstance(mContext).getFusedDate(getItem(position))));


        if(eventLocationText != null) holder.eventLocation.setText(eventLocationText);
        else holder.eventLocation.setText("n/a");

        if(eventFeeText != null) holder.eventFee.setText(eventFeeText + " €");
        else holder.eventFee.setText("n/a");


        return convertView;
    }

    /**
     * Entfernt Event mit der angegebenen ID
     *
     * @param i ID
     */
    public void removeListItem(int i){
        eventList.remove(i);
        notifyDataSetChanged();
    }
}
