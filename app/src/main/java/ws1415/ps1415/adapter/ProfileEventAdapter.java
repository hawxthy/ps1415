package ws1415.ps1415.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.EventMetaData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ws1415.common.util.ImageUtil;
import ws1415.ps1415.R;

/**
 * Dieser Adapter wird genutzt, um eine Liste von Veranstaltungen mit Inhalt zu füllen.
 *
 * @author Martin Wrodarczyk
 */
public class ProfileEventAdapter extends BaseAdapter {
    private List<EventMetaData> mData = new LinkedList<>();
    private LayoutInflater mInflater;
    private Context mContext;

    public ProfileEventAdapter(List<EventMetaData> data, Context context){
        mData = data;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public EventMetaData getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class Holder {
        private ImageView icon;
        private TextView primaryText;
        private TextView secondaryText;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Holder holder;

        if(convertView == null){
            holder = new Holder();
            convertView = mInflater.inflate(R.layout.list_view_profile_event, viewGroup, false);
            holder.icon = (ImageView) convertView.findViewById(R.id.list_item_profile_event_icon);
            holder.primaryText = (TextView) convertView.findViewById(R.id.list_item_profile_event_primary);
            holder.secondaryText = (TextView) convertView.findViewById(R.id.list_item_profile_event_secondary);
        } else {
            holder = (Holder)convertView.getTag();
        }

        EventMetaData item = getItem(i);
        Date startDate = new Date(item.getDate().getValue());
        SimpleDateFormat sdfFirst = new SimpleDateFormat("EEEE, dd. MMM yyyy");
        SimpleDateFormat sdfSecond = new SimpleDateFormat("HH:mm");
        String at = mContext.getString(R.string.at_time);
        String startDateFormatted = sdfFirst.format(startDate) + " " + at + " " + sdfSecond.format(startDate);

        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_picture);
        holder.icon.setImageBitmap(ImageUtil.getRoundedBitmap(bm));
        holder.primaryText.setText(item.getTitle());
        holder.secondaryText.setText(startDateFormatted);

        return convertView;
    }
}
