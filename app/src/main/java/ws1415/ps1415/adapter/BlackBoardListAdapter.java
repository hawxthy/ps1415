package ws1415.ps1415.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.skatenight.skatenightAPI.model.BoardEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.GroupProfileActivity;
import ws1415.ps1415.model.Right;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.DateUtil;
import ws1415.ps1415.util.GroupImageLoader;
import ws1415.ps1415.util.ImageUtil;

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
        private ImageView messageImage;
        private ImageView deleteEntryButton;
        private ImageView editEntryButton;
        private TextView dateView;
        private TextView writerView;
        private TextView contentView;
        private ButtonFlat commentButton;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup){
        final Holder holder;

        if(convertView == null){
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_view_item_black_board, viewGroup, false);
            holder.dateView = (TextView) convertView.findViewById(R.id.list_view_item_black_board_date_text_view);
            holder.writerView = (TextView) convertView.findViewById(R.id.list_view_item_black_board_creator_text_view);
            holder.contentView = (TextView) convertView.findViewById(R.id.list_view_item_black_board_content_edit_text);
            holder.deleteEntryButton = (ImageView) convertView.findViewById(R.id.delete_board_message_button);
            holder.editEntryButton = (ImageView) convertView.findViewById(R.id.edit_board_message_button);
            holder.commentButton = (ButtonFlat) convertView.findViewById(R.id.list_view_item_black_board_comments_button);
            holder.messageImage = (ImageView) convertView.findViewById(R.id.black_board_message_image);
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }

        holder.dateView.setText(DateUtil.getInstance().formatMyDate(getItem(position).getDate().getValue()));
        holder.writerView.setText(getItem(position).getWriter());
        //TODO wenn Zeit kann man hier versucht den Profil zu öffnen über einen click
        holder.contentView.setText(getItem(position).getMessage());
        // Prüft ob ein Bild existiert
        if(getItem(position).getBlobKey()!= null){
            GroupImageLoader.getInstance().setBoardImageToImageView(context, getItem(position).getBlobKey(),holder.messageImage);

            holder.messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder altertadd = new AlertDialog.Builder(context);
                    LayoutInflater factory = LayoutInflater.from(context);
                    View previewImageView = factory.inflate(R.layout.preview_board_image, null);
                    final ImageView previewImage = (ImageView) previewImageView.findViewById(R.id.board_image);
                    GroupImageLoader.getInstance().setBoardImageToImageView(context, getItem(position).getBlobKey(), previewImage);
                    altertadd.setView(previewImageView);
                    altertadd.setCancelable(true);
                    altertadd.show();
                }
            });

            holder.messageImage.setVisibility(View.VISIBLE);
        }else{
            holder.messageImage.setVisibility(View.GONE);
        }



        if(context instanceof GroupProfileActivity){
            final GroupProfileActivity activity = (GroupProfileActivity)context;
            holder.deleteEntryButton.setOnClickListener(new View.OnClickListener() {
                Long entryId = getItem(position).getId();
                @Override
                public void onClick(View view) {
                    activity.startDeleteBoardEntry(entryId);
                }
            });
            if(rights != null){
                if(!rights.contains(Right.EDITBLACKBOARD.name()) && !rights.contains(Right.FULLRIGHTS.name())){
                    holder.deleteEntryButton.setVisibility(View.GONE);
                    holder.editEntryButton.setVisibility(View.GONE);
                }
            }else{
                holder.deleteEntryButton.setVisibility(View.GONE);
                holder.editEntryButton.setVisibility(View.GONE);
            }
            holder.commentButton.setOnClickListener(new View.OnClickListener() {
                Long entryId = getItem(position).getId();
                @Override
                public void onClick(View view) {
                    activity.startCommentMessage(entryId);
                }
            });
            holder.editEntryButton.setOnClickListener(new View.OnClickListener() {
                Long entryId = getItem(position).getId();
                @Override
                public void onClick(View view) {
                    activity.startEditMessage(entryId);
                }
            });
        }
        return convertView;
    }
}
