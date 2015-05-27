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

import com.skatenight.skatenightAPI.model.BlobKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ws1415.common.util.ImageUtil;
import ws1415.ps1415.R;
import ws1415.common.model.Conversation;
import ws1415.common.model.Message;
import ws1415.ps1415.util.UserImageLoader;

/**
 * Dieser Adapter dient dazu, die Liste von Konversationen eines Benutzers zu füllen.
 *
 * @author Martin Wrodarczyk
 */
public class MessagingAdapter extends BaseAdapter {
    private List<Conversation> mData;
    private LayoutInflater mInflater;
    private Context mContext;
    private Bitmap defaultBitmap;

    /**
     * @param data    Liste von Konversationen
     * @param context Context
     */
    public MessagingAdapter(List<Conversation> data, Context context) {
        mData = data;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        defaultBitmap = ImageUtil.getRoundedBitmap(BitmapFactory.
                decodeResource(context.getResources(), R.drawable.default_picture));
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Conversation getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class Holder {
        private ImageView userPicture;
        private TextView userName;
        private TextView lastMessage;
        private TextView lastMessageTime;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Holder holder;

        if (convertView == null) {
            holder = new Holder();
            convertView = mInflater.inflate(R.layout.list_view_item_conversation, viewGroup, false);
            holder.userPicture = (ImageView) convertView.findViewById(R.id.list_item_conversation_picture);
            holder.userName = (TextView) convertView.findViewById(R.id.list_item_conversation_name);
            holder.lastMessage = (TextView) convertView.findViewById(R.id.list_item_conversation_last_message);
            holder.lastMessageTime = (TextView) convertView.findViewById(R.id.list_item_conversation_last_message_time);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        Conversation item = getItem(i);
        Message lastMessage = item.getLastMessage();
        String blobKey = item.getPictureKey();
        String userName = item.getFirstName() + item.getLastName();
        String lastMessageContent = (lastMessage == null) ? "" : lastMessage.getContent();
        String lastMessageTime = (lastMessage == null) ? "" : convertToMessageTime(lastMessage.getSendDate());

        UserImageLoader.getInstance(mContext).displayImage(new BlobKey().setKeyString(blobKey), holder.userPicture);
        holder.userName.setText(userName);
        holder.lastMessage.setText(lastMessageContent);
        holder.lastMessageTime.setText(lastMessageTime);

        return convertView;
    }

    /**
     * Löscht die Konversation aus den Daten.
     *
     * @param conversation Konversation
     */
    public void removeItem(Conversation conversation) {
        mData.remove(conversation);
        notifyDataSetChanged();
    }

    private String convertToMessageTime(Date date) {
        SimpleDateFormat sdf;
        if (checkSameDay(new Date(), date)) {
            sdf = new SimpleDateFormat("HH:mm");
            return sdf.format(date);
        } else if (checkYesterday(new Date(), date)) {
            return "Yesterday";
        } else {
            sdf = new SimpleDateFormat("dd MMM yyyy");
            return sdf.format(date);
        }
    }

    private boolean checkSameDay(Date today, Date dateToCheck) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(dateToCheck);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean checkYesterday(Date today, Date dateToCheck) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(dateToCheck);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) - cal2.get(Calendar.DAY_OF_YEAR) == 1;
    }
}
