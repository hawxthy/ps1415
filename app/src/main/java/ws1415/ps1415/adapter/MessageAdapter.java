package ws1415.ps1415.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.model.LocalMessageType;
import ws1415.ps1415.model.Message;

/**
 * Dieser Adapter dient dazu, die Liste von Nachrichten in einer Konversation zu füllen.
 *
 * @author Martin Wrodarczyk
 */
public class MessageAdapter extends BaseAdapter {
    private List<Message> mData;
    private LayoutInflater mInflater;
    private Context mContext;

    /**
     * @param data Liste von Nachrichten
     * @param context Context
     */
    public MessageAdapter(List<Message> data, Context context){
        mData = data;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Übergibt die neuesten Daten dem Adapter.
     *
     * @param data Daten
     */
    public void setUpData(List<Message> data){
        mData.clear();
        mData.addAll(data);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().getId();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Message getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class Holder {
        private TextView content;
        private TextView sendDate;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Holder holder;

        if(convertView == null){
            holder = new Holder();
            if(getItemViewType(i) == LocalMessageType.INCOMING.getId()) {
                convertView = mInflater.inflate(R.layout.list_view_item_message_incoming, viewGroup, false);
            } else if(getItemViewType(i) == LocalMessageType.OUTGOING_NOT_RECEIVED.getId()) {
                convertView = mInflater.inflate(R.layout.list_view_item_message_outgoing_not_received, viewGroup, false);
            } else {
                convertView = mInflater.inflate(R.layout.list_view_item_message_outgoing_received, viewGroup, false);
            }
            holder.content = (TextView) convertView.findViewById(R.id.item_message_content);
            holder.sendDate = (TextView) convertView.findViewById(R.id.item_message_date);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        Message item = getItem(i);
        holder.content.setText(item.getContent());
        holder.sendDate.setText(convertDate(item.getSendDate()));

        return convertView;
    }

    /**
     * Fügt eine Nachricht hinzu und updated die Liste.
     *
     * @param message Nachricht
     */
    public void addMessage(Message message){
        mData.add(message);
        notifyDataSetChanged();
    }

    private String convertDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, HH:mm");
        return sdf.format(date);
    }
}
