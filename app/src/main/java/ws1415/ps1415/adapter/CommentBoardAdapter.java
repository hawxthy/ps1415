package ws1415.ps1415.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.Comment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;

/**
 * @author Bernd Eissing on 07.06.2015.
 */
public class CommentBoardAdapter  extends BaseAdapter{
    private Long id;
    private List<Comment> comments;
    private Context context;
    private LayoutInflater inflater;
    private List<String> rights;

    public CommentBoardAdapter(Context context, Long id, List<Comment> comments){
        this.id = id;
        this.context = context;
        this.comments = comments;
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
        if(comments == null) return 0;
        else return comments.size();
    }

    /**
     * Gibt das BoardEntry and der angegebenen Stelle zurück.
     *
     * @param i Die Stelle
     * @return
     */
    @Override
    public Comment getItem(int i){
        if(comments != null){
            return comments.get(i);
        }
        return  null;
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
        private TextView contentView;
        private TextView writerView;
        private TextView dateView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup){
        final Holder holder;

        if(convertView == null){
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_view_item_comment_board, viewGroup, false);
            holder.dateView = (TextView) convertView.findViewById(R.id.date_text_view);
            holder.contentView = (TextView) convertView.findViewById(R.id.content_text_view);
            holder.writerView = (TextView) convertView.findViewById(R.id.writer_text_view);
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
        holder.writerView.setText(ServiceProvider.getEmail());
        holder.contentView.setText(getItem(position).getMessage());

        return convertView;
    }

}
