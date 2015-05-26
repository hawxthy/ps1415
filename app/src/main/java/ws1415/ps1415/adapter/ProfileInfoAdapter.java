package ws1415.ps1415.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import ws1415.ps1415.R;

/**
 * Dieser Adapter wird genutzt, um die Liste der allgemeinen Informationen mit Inhalt zu füllen.
 *
 * @author Martin Wrodarczyk
 */
public class ProfileInfoAdapter extends BaseAdapter {
    private List<Entry<String, String>> mData = new ArrayList<>();
    private LayoutInflater mInflater;

    public ProfileInfoAdapter(List<Entry<String, String>> data, Context context){
        mData = data;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Entry<String, String> getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean isEnabled(int i){
        return false;
    }

    private class Holder{
        private TextView primaryText;
        private TextView secondaryText;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Holder holder;

        if(convertView == null){
            holder = new Holder();
            convertView = mInflater.inflate(R.layout.list_view_item_profile_info, viewGroup, false);
            holder.primaryText = (TextView) convertView.findViewById(R.id.list_item_profile_info_primary);
            holder.secondaryText = (TextView) convertView.findViewById(R.id.list_item_profile_info_secondary);
            convertView.setTag(holder);
        } else {
            holder = (Holder)convertView.getTag();
        }

        Entry<String, String> item = getItem(i);
        holder.primaryText.setText(item.getKey());
        holder.secondaryText.setText(item.getValue());

        return convertView;
    }
}
