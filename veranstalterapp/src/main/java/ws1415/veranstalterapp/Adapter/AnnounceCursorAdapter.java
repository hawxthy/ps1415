package ws1415.veranstalterapp.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;

import ws1415.veranstalterapp.util.EventUtils;
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
    private Event event;
    private boolean edit_mode;

    /**
     * Konstruktor, der den Inhalt der Liste festlegt.
     *
     * @param context   Context, von dem aus der Adapter aufgerufen wird
     * @param fieldList Liste von den Routen
     */
    public AnnounceCursorAdapter(Context context, List<Field> fieldList, Event event) {
        this.context = context;
        this.fieldList = fieldList;
        this.event = event;
    }

    /**
     * Gibt die Anzahl der zu bearbeitenden EventFelder in der Liste zurück.
     *
     * @return Anzahl der EventFelder
     */
    @Override
    public int getCount() {
        if (fieldList == null) {
            return 0;
        } else {
            if (edit_mode) {
                return fieldList.size() * 2;
            } else {
                return fieldList.size();
            }
        }
    }

    /**
     * Gibt das zu bearbeitende Feld an der Stelle i in der Liste zurück.
     *
     * @param i Stelle
     * @return Feld
     */
    @Override
    public Field getItem(int i) {
        return fieldList.get(i);
    }

    /**
     * Gibt die Id der Route in der Liste zurück.
     *
     * @param i
     * @return
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Klasse zum Halten der GUI Elemente für ein ButtonField.
     */
    private class HolderButtonField {
        TextView title;
        Button button;
    }

    /**
     * Klasse zum Halten der GUI Elemente um Felder zu ergänzen.
     */
    private class HolderAddField {
        Button addFieldButton;
    }

    /**
     * Klasse zum Halten der GUI um Felder für ein TextField.
     */
    private class HolderTextField {
        TextView title;
        EditText content;
    }

    /**
     * Setzt das Layout der Items in der ListView.
     *
     * @param position    Position in der ListView
     * @param convertView
     * @param viewGroup
     * @return die View des Items an der Stelle position in der Liste
     */
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = null;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (position % 2 == 0 || edit_mode == false) {
            if (edit_mode) position = position / 2;
            if (getItem(position).getType().equals(TYPE.TITLE.name()) ||
                    getItem(position).getType().equals(TYPE.LOCATION.name()) ||
                    getItem(position).getType().equals(TYPE.DESCRIPTION.name()) ||
                    getItem(position).getType().equals(TYPE.SIMPLETEXT.name()) ||
                    getItem(position).getType().equals(TYPE.LINK.name())) {
                HolderTextField holder = new HolderTextField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_simpletext, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_simpletext_textView);
                holder.content = (EditText) view.findViewById(R.id.list_view_item_announce_information_simpletext_editText);
                holder.title.setText(getItem(position).getTitle());
            } else if (getItem(position).getType().equals(TYPE.FEE.name())) {
                HolderTextField holder = new HolderTextField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_fee, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_fee_textView);
                holder.content = (EditText) view.findViewById(R.id.list_view_item_announce_information_fee_editText);
                holder.title.setText(getItem(position).getTitle());
            } else if (getItem(position).getType().equals(TYPE.DATE.name())) {
                HolderButtonField holder = new HolderButtonField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
                holder.title.setText(getItem(position).getTitle());
                // @TODO SET BUTTON LISTENER AND BUTTONTEXT FÜR SET DATE
            } else if (getItem(position).getType().equals(TYPE.TIME.name())) {
                HolderButtonField holder = new HolderButtonField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
                holder.title.setText(getItem(position).getTitle());
                // @TODO SET BUTTON LISTENER AND BUTTONTEXT FÜR SET TIME
            } else if (getItem(position).getType().equals(TYPE.ROUTE.name())) {
                HolderButtonField holder = new HolderButtonField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
                holder.title.setText(getItem(position).getTitle());
                // @TODO SET BUTTON LISTENER AND BUTTONTEXT FÜR SET ROUTE
            } else if(getItem(position).getType().equals(TYPE.PICTURE.name())) {
                HolderButtonField holder = new HolderButtonField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
                holder.title.setText(getItem(position).getTitle());
            }

        } else {
            HolderAddField holder = new HolderAddField();
            final int pos = position;
            view = inflater.inflate(R.layout.list_view_item_announce_information_plus_item, viewGroup, false);
            holder.addFieldButton = (Button) view.findViewById(R.id.list_view_item_announce_information_plus_item_button);
            holder.addFieldButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createFieldChooser(pos);
                }
            });
        }
        return view;
    }

    public void createFieldChooser(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.field_picker_title)
                .setItems(R.array.field_picker, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        createTitleInput(which, position);
                    }
                });
        builder.show();
    }

    public void createTitleInput(final int which, final int position){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        if(which == 0) alert.setTitle(R.string.field_picker_text);
        else if(which == 1) alert.setTitle(R.string.field_picker_picture);
        else if(which == 2) alert.setTitle(R.string.field_picker_link);

        final EditText input = new EditText(context);
        alert.setView(input);

        alert.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                if(which == 0) EventUtils.getInstance(context).addDynamicField(value, TYPE.SIMPLETEXT, event, position/2 + 1);
                else if(which == 1) EventUtils.getInstance(context).addDynamicField(value, TYPE.PICTURE, event, position/2 + 1);
                else if(which == 2) EventUtils.getInstance(context).addDynamicField(value, TYPE.LINK, event, position/2 + 1);

                notifyDataSetChanged();
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    public void startEditMode() {
        edit_mode = true;
        notifyDataSetChanged();
    }

    public void exitEditMode() {
        edit_mode = false;
        notifyDataSetChanged();
    }

    public boolean getEditMode(){
        return edit_mode;
    }
}
