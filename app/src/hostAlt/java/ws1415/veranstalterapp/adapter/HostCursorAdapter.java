package ws1415.veranstalterapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.Host;

import java.util.ArrayList;
import java.util.List;

import ws1415.veranstalterapp.R;

/**
 * Klasse zum Füllen der ListView in PermissionManagementActivity
 *
 * Created by Martin on 19.12.2014.
 */
public class HostCursorAdapter extends BaseAdapter {
    private List<Host> hostList = new ArrayList<Host>();
    private Context context;
    private LayoutInflater inflater;

    /**
     * Konstruktor, der den Inhalt der Liste festlegt.
     *
     * @param context Context, von dem aus der Adapter aufgerufen wird
     * @param hostList Liste von den Veranstaltern
     */
    public HostCursorAdapter(Context context, List<Host> hostList){
        this.context = context;
        this.hostList = hostList;
    }

    /**
     * Gibt die Anzahl der Veranstaltern zurück.
     *
     * @return Anzahl der Veranstaltern
     */
    @Override
    public int getCount(){
        if(hostList == null) {
            return 0;
        } else {
            return hostList.size();
        }
    }

    /**
     * Gibt den Veranstalter an der Stelle i in der Liste zurück.
     *
     * @param i Stelle
     * @return
     */
    @Override
    public Host getItem(int i){
        return hostList.get(i);
    }

    /**
     * Gibt die Id des Veranstalters in der Liste zurück.
     *
     * @param i Stelle des Veranstalters
     * @return Id des Veranstalters
     */
    public long getItemId(int i){
        return i;
    }

    /**
     * Klasse zum Halten der GUI Elemente, damit keine Kopien erstellt werden.
     */
    private class Holder{
        private TextView hostMail;
    }

    /**
     * Setzt das Layout der Items in der ListView
     *
     * @param position Position in der ListView un in der ArrayList
     * @param convertView Position in der ListView mit den Daten aus der ArrayList
     * @param viewGroup Die Liste welche die Veranstalter hält
     * @return Das Item in der Liste von den Veranstaltern
     */
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup){
        Holder holder;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_view_item_permission_management, viewGroup, false);
            holder.hostMail = (TextView) convertView.findViewById(R.id.list_view_item_permission_management_textView);
            convertView.setTag(holder);
        }else{
            holder = (Holder)convertView.getTag();
        }

        holder.hostMail.setText(getItem(position).getEmail());

        return convertView;
    }

    /**
     * Entfernt Veranstalter mit der angegebenen ID
     *
     * @param i ID
     */
    public void removeListItem(int i){
        hostList.remove(i);
        notifyDataSetChanged();
    }
}
