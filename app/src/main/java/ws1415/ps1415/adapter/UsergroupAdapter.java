package ws1415.ps1415.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.util.GroupImageLoader;


/**
 * Dieser Adapter wird für das Anzeigen der Usergruppen in der Liste genutzt.
 *
 * @author Martin Wrodarczyk
 */
public class UsergroupAdapter extends BaseAdapter {
    private List<UserGroupMetaData> groupList;
    private List<Bitmap> groupPictures;
    private Context context;
    private LayoutInflater inflater;
    private int maximum;

    /**
     * Konstruktor, der den Inhalt der Liste festlegt;
     *
     * @param context   Context, von dem aus der Adapter aufgerufen wird.
     * @param groupList Liste von den Nutzergruppen
     * @param maximum Maximale Anzahl der Einträge, oder -1 für unbegrenzt.
     */
    public UsergroupAdapter(Context context, List<UserGroupMetaData> groupList, int maximum) {
        if(maximum > -1 && maximum < groupList.size()){
            throw new IllegalArgumentException("Liste zu groß");
        }
        this.maximum = maximum;
        this.context = context;
        this.groupList = groupList;
        if (context != null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
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
    public UserGroupMetaData getItem(int i) {
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
     * Klasse zum Halten der GUI Elemente, damit convertView die alten Objekte übernehmen kann.
     */
    private class Holder {
        private ImageView groupImage;
        private TextView groupName;
        private TextView groupCount;
    }

    /**
     * Methode zum Füllen der ListView mit Items.
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Holder holder;

        if (convertView == null) {
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_view_item_usergroup, viewGroup, false);
            holder.groupImage = (ImageView) convertView.findViewById(R.id.user_group_list_view_item_image);
            holder.groupName = (TextView) convertView.findViewById(R.id.user_group_list_view_item_title);
            holder.groupCount = (TextView) convertView.findViewById(R.id.user_group_list_view_item_count);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        GroupImageLoader.getInstance().setGroupImageToImageView(context, getItem(position).getBlobKeyValue(), holder.groupImage);
        holder.groupName.setText(getItem(position).getName());
        holder.groupCount.setText(context.getString(R.string.usergroup_member_count)+Integer.toString(getItem(position).getMemberCount()));

        return convertView;
    }

    /**
     * Entfernt die UserGroup.
     *
     * @param userGroup UserGroup
     */
    public void removeListItem(UserGroup userGroup) {
        groupList.remove(userGroup);
        notifyDataSetChanged();
    }

    /**
     * Fügt die übergebene UserGroup der Liste von UserGroups hinzu.
     *
     * @param userGroup
     */
    public boolean addListItem(UserGroupMetaData userGroup){
        if(maximum > -1 && groupList.size() >= maximum){
            Toast.makeText(context, R.string.usergroup_adapter_maximum_reached, Toast.LENGTH_LONG).show();
            return false;
        }
        groupList.add(userGroup);
        notifyDataSetChanged();
        return true;
    }
}
