package ws1415.ps1415.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.BoardEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.GroupProfileActivity;
import ws1415.ps1415.model.Right;

/**
 * @author Bernd Eissing on 05.06.2015.
 */
public class BlackBoardListAdapter extends BaseAdapter{
    private List<BoardEntry> boardEntries;
    private Context context;
    private LayoutInflater inflater;
    private List<String> rights;

    public BlackBoardListAdapter(Context context, List<BoardEntry> boardEntries, List<String> rights){
        this.context = context;
        this.boardEntries = boardEntries;
        this.rights = rights;
        if(context != null){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    /**
     * Gibt die Anz. der Boardentries zurück.
     *
     * @return
     */
    @Override
    public int getCount(){
        if(boardEntries == null) return 0;
        else return boardEntries.size();
    }

    /**
     * Gibt das BoardEntry and der angegebenen Stelle zurück.
     *
     * @param i Die Stelle
     * @return
     */
    @Override
    public BoardEntry getItem(int i){
        return boardEntries.get(i);
    }

    /**
     * Git die ID des BoardEntries zurück.
     *
     * @param i Stelle
     * @return
     */
    public long getItemId(int i) {
        return i;
    }

    /**
     * Klasse zum halten der GUI Elemente, damit die convertView die alten Objekte übernehmen kann.
     */
    private class Holder{
        private EditText hiddenEntryId;
        private ImageView deleteEntryButton;
        private TextView dateView;
        private TextView writerView;
        private TextView contentView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup){
        final Holder holder;

        if(convertView == null){
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_view_item_black_board, viewGroup, false);
            holder.dateView = (TextView) convertView.findViewById(R.id.list_view_item_black_board_date_text_view);
            holder.writerView = (TextView) convertView.findViewById(R.id.list_view_item_black_board_creator_text_view);
            holder.contentView = (TextView) convertView.findViewById(R.id.list_view_item_black_board_content_edit_text);
            holder.hiddenEntryId = (EditText) convertView.findViewById(R.id.hidden_board_id);
            holder.deleteEntryButton = (ImageView) convertView.findViewById(R.id.delete_board_message_button);
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }

        Date date = new Date(getItem(position).getDate().getValue());
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);


        holder.dateView.setText("Am "+day+"."+month+" "+year+" um "+hours+":"+minutes+" Uhr");
        holder.writerView.setText(getItem(position).getWriter());
        holder.contentView.setText(getItem(position).getMessage());
        holder.hiddenEntryId.setText(getItem(position).getId().toString());

        if(context instanceof GroupProfileActivity){
            final GroupProfileActivity activity = (GroupProfileActivity)context;
            holder.deleteEntryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.startDeleteBoardEntry(Long.parseLong(holder.hiddenEntryId.getText().toString()));
                }
            });
            if(rights != null){
                if(!rights.contains(Right.EDITBLACKBOARD.name()) && rights.contains(Right.FULLRIGHTS.name())){
                    holder.deleteEntryButton.setVisibility(View.GONE);
                }
            }else{
                holder.deleteEntryButton.setVisibility(View.GONE);
            }
        }
        return convertView;
    }
}
