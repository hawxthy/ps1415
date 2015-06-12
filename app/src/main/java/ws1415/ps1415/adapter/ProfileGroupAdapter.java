package ws1415.ps1415.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.UserGroupMetaData;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.util.GroupImageLoader;

/**
 * Dieser Adapter wird genutzt, um eine Liste von Gruppen mit Inhalt zu füllen.
 *
 * @author Martin Wrodarczyk
 */
public class ProfileGroupAdapter extends BaseAdapter {
    private List<UserGroupMetaData> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context mContext;

    public ProfileGroupAdapter(List<UserGroupMetaData> data, Context context){
        mData = data;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public UserGroupMetaData getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class Holder {
        private ImageView groupImage;
        private TextView primaryText;
        private TextView secondaryText;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Holder holder;

        if(convertView == null){
            holder = new Holder();
            convertView = mInflater.inflate(R.layout.list_view_item_profile_group, viewGroup, false);
            holder.groupImage = (ImageView) convertView.findViewById(R.id.list_item_profile_group_icon);
            holder.primaryText = (TextView) convertView.findViewById(R.id.list_item_profile_group_primary);
            holder.secondaryText = (TextView) convertView.findViewById(R.id.list_item_profile_group_secondary);
            convertView.setTag(holder);
        } else {
            holder = (Holder)convertView.getTag();
        }

        UserGroupMetaData item = getItem(i);
        String secondaryText = mContext.getString(R.string.members) + ": " + item.getMemberCount();

        GroupImageLoader.getInstance().setGroupImageToImageView(mContext, item.getBlobKey(), holder.groupImage);

        holder.primaryText.setText(item.getName());
        holder.secondaryText.setText(secondaryText);

        return convertView;
    }
}
