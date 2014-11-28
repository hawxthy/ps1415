package ws1415.veranstalterapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.Field;

import ws1415.veranstalterapp.util.EventUtils.TYPE;
import java.util.ArrayList;
import java.util.List;

import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.fragment.AnnounceInformationFragment;

/**
 * Klasse zum Füllen der ListView in AnnounceInformationFragment.
 *
 * Created by Martin Wrodarczyk on 28.11.2014.
 */
public class AnnounceCursorAdapter extends BaseAdapter {
    private List<Field> fieldList = new ArrayList<Field>();
    private Context context;
    private LayoutInflater inflater;

    /**
     * Konstruktor, der den Inhalt der Liste festlegt.
     *
     * @param context Context, von dem aus der Adapter aufgerufen wird
     * @param fieldList Liste von den Routen
     */
    public AnnounceCursorAdapter(Context context, List<Field> fieldList){
        this.context = context;
        this.fieldList = fieldList;
    }

    /**
     * Gibt die Anzahl der zu bearbeitenden EventFelder in der Liste zurück.
     *
     * @return Anzahl der EventFelder
     */
    @Override
    public int getCount(){
        if(fieldList == null){
            return 0;
        } else{
            return fieldList.size();
        }
    }

    /**
     * Gibt das zu bearbeitende Feld an der Stelle i in der Liste zurück.
     *
     * @param i Stelle
     * @return Feld
     */
    @Override
    public Field getItem(int i){
        return fieldList.get(i);
    }

    /**
     * Gibt die Id der Route in der Liste zurück.
     *
     * @param i
     * @return
     */
    @Override
    public long getItemId(int i){
        return i;
    }

    /**
     * Klasse zum Halten der GUI Elemente für ein ButtonField.
     */
    private class HolderButtonField{
        TextView title;
        Button button;
    }

    /**
     * Klasse zum Halten der GUI Elemente um Felder zu ergänzen.
     */
    private class HolderAddField{
        Button addFieldButton;
    }

    /**
     * Klasse zum Halten der GUI um Felder für ein TextField.
     */
    private class HolderTextField{
        TextView title;
        EditText content;
    }

    /**
     * Setzt das Layout der Items in der ListView.
     *
     * @param position Position in der ListView
     * @param convertView
     * @param viewGroup
     * @return die View des Items an der Stelle position in der Liste
     */
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup){
        View view = null;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(getItem(position).getType().equals(TYPE.TITLE.name()) ||
                getItem(position).getType().equals(TYPE.LOCATION.name()) ||
                getItem(position).getType().equals(TYPE.DESCRIPTION.name()) ||
                getItem(position).getType().equals(TYPE.SIMPLETEXT.name())){
            HolderTextField holder = new HolderTextField();
            view = inflater.inflate(R.layout.list_view_item_announce_information_simpletext, viewGroup, false);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_simpletext_textView);
            holder.content = (EditText) view.findViewById(R.id.list_view_item_announce_information_simpletext_editText);
            holder.title.setText(getItem(position).getTitle());
        } else if(getItem(position).getType().equals(TYPE.FEE.name())){
            HolderTextField holder = new HolderTextField();
            view = inflater.inflate(R.layout.list_view_item_announce_information_fee, viewGroup, false);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_fee_textView);
            holder.content = (EditText) view.findViewById(R.id.list_view_item_announce_information_fee_editText);
            holder.title.setText(getItem(position).getTitle());
        } else if(getItem(position).getType().equals(TYPE.DATE.name())){
            HolderButtonField holder = new HolderButtonField();
            view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
            holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
            holder.title.setText(getItem(position).getTitle());
            // @TODO SET BUTTON LISTENER AND BUTTONTEXT FÜR SET DATE
        } else if(getItem(position).getType().equals(TYPE.TIME)){
            HolderButtonField holder = new HolderButtonField();
            view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
            holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
            holder.title.setText(getItem(position).getTitle());
            // @TODO SET BUTTON LISTENER AND BUTTONTEXT FÜR SET TIME
        } else if(getItem(position).getType().equals(TYPE.ROUTE)){
            HolderButtonField holder = new HolderButtonField();
            view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
            holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
            holder.title.setText(getItem(position).getTitle());
            // @TODO SET BUTTON LISTENER AND BUTTONTEXT FÜR SET ROUTE
        }
        return view;
    }

    public void startEditMode(){

    }

    public void exitEditMode(){

    }
}
