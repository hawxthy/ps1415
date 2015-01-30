package ws1415.ps1415.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;


/**
 * Dieser Adapter wird für das Anzeigen der Usergruppen in der Liste genutzt.
 *
 * Created by Martin on 30.01.2015.
 */
public class UsergroupAdapter extends BaseAdapter {
    private List<UserGroup> groupList = new ArrayList<UserGroup>();
    private Context context;
    private LayoutInflater inflater;

    /**
     * Konstruktor, der den Inhalt der Liste festlegt;
     *
     * @param context   Context, von dem aus der Adapter aufgerufen wird.
     * @param groupList Liste von den Nutzergruppen
     */
    public UsergroupAdapter(Context context, List<UserGroup> groupList) {
        this.context = context;
        this.groupList = groupList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Gibt die Anzahl der Nutzergruppen zurück.
     *
     * @return Anzahl der Nutzergruppen
     */
    @Override
    public int getCount() {
        if (groupList == null) return 0;
        else return groupList.size();
    }

    /**
     * Gibt die Nutzergruppe an der Stelle i in der Liste zurück.
     *
     * @param i Stelle
     * @return Nutzergruppe an Stelle i
     */
    @Override
    public UserGroup getItem(int i) {
        return groupList.get(i);
    }

    /**
     * Gibt die ID der Nutzergruppe in der Liste zurück.
     *
     * @param i Stelle der Nutzergruppe
     * @return ID der Nutzergruppe
     */
    public long getItemId(int i) {
        return i;
    }

    /**
     * Klasse zum Halten der GUI Element, damit keine Kopien erstellt werden.
     */
    private class Holder {
        private TextView groupName;
        private TextView groupCreator;
        private TextView groupCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Holder holder;

        if (convertView == null) {
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_view_item_usergroup, viewGroup, false);
            holder.groupName = (TextView) convertView.findViewById(R.id.list_view_item_usergroup_name);
            holder.groupCreator = (TextView) convertView.findViewById(R.id.list_view_item_usergroup_creator);
            holder.groupCount = (TextView) convertView.findViewById(R.id.list_view_item_usergroup_member);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.groupName.setText(getItem(position).getName());
        holder.groupCreator.setText(getItem(position).getCreator().getName());
        holder.groupCount.setText(getItem(position).getMembers().size());

        return convertView;
    }

    /**
     * Entfernt UserGroup mit der angegebenen ID.
     *
     * @param i ID
     */
    public void removeListItem(int i) {
        groupList.remove(i);
        notifyDataSetChanged();
    }
}
