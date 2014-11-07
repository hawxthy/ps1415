package ws1415.veranstalterapp.Adaper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.appspot.skatenight_ms.skatenightAPI.model.Route;

import java.util.ArrayList;
import java.util.List;

import ws1415.veranstalterapp.R;

/**
 * Created by Bernd Eissing on 31.10.2014.
 */
public class MapsCursorAdapter extends BaseAdapter {
    private List<Route> routeList = new ArrayList<Route>();
    private Context mContext;
    private LayoutInflater inflater;

    public MapsCursorAdapter(Context context, ArrayList<Route> routeList){
        mContext = context;
        this.routeList = routeList;
    }

    @Override
    public int getCount() {
        if(routeList == null){
            return 0;
        }else{
            return routeList.size();
        }
    }

    @Override
    public Object getItem(int i) {
        return routeList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    /**
     * Klasse zum halten der GUI Elemente, damit keine kopien erstellt werden.
     */
    private class Holder{
        private TextView routeName;
        private TextView routeLength;
    }

    /**
     * Setzt das Layout der Items in der ListView
     * @param position Position in der ListView und in der ArrayList
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
            convertView = inflater.inflate(R.layout.list_view_item_layout, viewGroup, false);
            holder.routeName = (TextView) convertView.findViewById(R.id.list_view_item_layout_map_name_TextView);
            holder.routeLength = (TextView) convertView.findViewById(R.id.list_view_item_layout_map_length_TextView);
            convertView.setTag(holder);
        } else{
            holder = (Holder)convertView.getTag();
        }
        holder.routeName.setText(routeList.get(position).getName());
        holder.routeLength.setText(routeList.get(position).getLength());

        return convertView;
    }

    public void removeListItem(int i){
        routeList.remove(i);
        notifyDataSetChanged();
    }
}
