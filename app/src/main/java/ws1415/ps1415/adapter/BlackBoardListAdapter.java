package ws1415.ps1415.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.BoardEntry;

import java.util.List;

import ws1415.ps1415.R;

/**
 * @author Bernd Eissing on 05.06.2015.
 */
public class BlackBoardListAdapter extends BaseAdapter{
    private List<BoardEntry> boardEntries;
    private Context context;
    private LayoutInflater inflater;

    public BlackBoardListAdapter(Context context, List<BoardEntry> boardEntries){
        this.context = context;
        this.boardEntries = boardEntries;
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
        private TextView dateView;
        private TextView writerView;
        private TextView contentView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup){
        Holder holder;

        if(convertView == null){
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_view_item_black_board, viewGroup, false);
            holder.dateView = (TextView) convertView.findViewById(R.id.list_view_item_black_board_date_text_view);
            holder.writerView = (TextView) convertView.findViewById(R.id.list_view_item_black_board_creator_text_view);
            holder.contentView = (TextView) convertView.findViewById(R.id.list_view_item_black_board_content_edit_text);
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }

        holder.writerView.setText(getItem(position).getWriter());
        holder.contentView.setText(getItem(position).getMessage());

        return convertView;
    }
}
