package ws1415.ps1415.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.GroupMetaData;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;

/**
 * Dieser Adapter wird genutzt, um eine Liste von Gruppen mit Inhalt zu füllen.
 *
 * @author Martin Wrodarczyk
 */
public class ProfileGroupAdapter extends BaseAdapter {
    private List<GroupMetaData> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context mContext;

    public ProfileGroupAdapter(List<GroupMetaData> data, Context context){
        mData = data;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public GroupMetaData getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class Holder {
        private TextView primaryText;
        private TextView secondaryText;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Holder holder;

        if(convertView == null){
            holder = new Holder();
            convertView = mInflater.inflate(R.layout.list_view_profile_group, viewGroup, false);
            holder.primaryText = (TextView) convertView.findViewById(R.id.list_item_profile_group_primary);
            holder.secondaryText = (TextView) convertView.findViewById(R.id.list_item_profile_group_secondary);
        } else {
            holder = (Holder)convertView.getTag();
        }

        GroupMetaData item = getItem(i);
        if(item.getMembers() == null) item.setMembers(new ArrayList<String>());
        String secondaryText = mContext.getString(R.string.members) + ": " + item.getMembers().size();

        holder.primaryText.setText(item.getName());
        holder.secondaryText.setText(secondaryText);

        return convertView;
    }
}
