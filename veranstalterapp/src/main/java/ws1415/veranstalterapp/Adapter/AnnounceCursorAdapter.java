package ws1415.veranstalterapp.Adapter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;
import com.skatenight.skatenightAPI.model.Route;

import ws1415.veranstalterapp.activity.ChooseRouteActivity;
import ws1415.veranstalterapp.util.EventUtils;
import ws1415.veranstalterapp.util.EventUtils.TYPE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ws1415.veranstalterapp.R;

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
    private ListView list;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private Route route;
    private Button routePickerButton;

    /**
     * Konstruktor, der den Inhalt der Liste festlegt.
     *
     * @param context   Context, von dem aus der Adapter aufgerufen wird
     * @param fieldList Liste von den Routen
     */
    public AnnounceCursorAdapter(Context context, List<Field> fieldList, Event event, ListView list) {
        this.context = context;
        this.fieldList = fieldList;
        this.event = event;
        this.list = list;
        setStandardTime();
        setCurrentDate();
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
                return fieldList.size() * 2 + 1;
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
    private class HolderUniqueTextField {
        TextView title;
        EditText content;
    }

    private class HolderSimpleTextField {
        TextView title;
        EditText content;
        Button deleteButton;
    }

    private class HolderPictureField {
        TextView title;
        Button button;
        Button deleteButton;
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

        if (position % 2 == 1 || edit_mode == false) {
            if (edit_mode) position = position / 2;
            final int focusPos = position;

            // FocusChangeListener um die Edittexts in der ListView abzuspeichern
            View.OnFocusChangeListener FocusChangeListener = new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        EditText editText = (EditText) v;
                        fieldList.get(focusPos).setValue(editText.getText().toString());
                    }
                }
            };

            if (getItem(position).getType().equals(TYPE.TITLE.name()) ||
                    getItem(position).getType().equals(TYPE.LOCATION.name()) ||
                    getItem(position).getType().equals(TYPE.DESCRIPTION.name())) {
                HolderUniqueTextField holder = new HolderUniqueTextField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_unique_text, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_uniquetext_textView);
                holder.content = (EditText) view.findViewById(R.id.list_view_item_announce_information_uniquetext_editText);

                holder.content.setOnFocusChangeListener(FocusChangeListener);
                if(getItem(position).getValue() != null) holder.content.setText(getItem(position).getValue().toString());
                holder.title.setText(getItem(position).getTitle());
            } else if (getItem(position).getType().equals(TYPE.FEE.name())) {
                HolderUniqueTextField holder = new HolderUniqueTextField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_fee, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_fee_textView);
                holder.content = (EditText) view.findViewById(R.id.list_view_item_announce_information_fee_editText);

                holder.content.setOnFocusChangeListener(FocusChangeListener);
                if(getItem(position).getValue() != null) holder.content.setText(getItem(position).getValue().toString());
                holder.title.setText(getItem(position).getTitle());
            } else if (getItem(position).getType().equals(TYPE.DATE.name())) {
                HolderButtonField holder = new HolderButtonField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);

                holder.title.setText(getItem(position).getTitle());
                setDateListener(holder.button);
                holder.button.setText(day + "." + (month + 1) + "." + year);
            } else if (getItem(position).getType().equals(TYPE.TIME.name())) {
                HolderButtonField holder = new HolderButtonField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);

                holder.title.setText(getItem(position).getTitle());
                setTimeListener(holder.button);
                if (minute < 10) holder.button.setText(hour + ":0" + minute + " Uhr");
                else holder.button.setText(hour + ":" + minute + " Uhr");
            } else if (getItem(position).getType().equals(TYPE.ROUTE.name())) {
                HolderButtonField holder = new HolderButtonField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
                routePickerButton = holder.button;

                holder.title.setText(getItem(position).getTitle());
                setRouteListener(holder.button);
                if(route != null) holder.button.setText(route.getName());
                else holder.button.setText(context.getResources().getString(R.string.announce_info_choose_map));
            } else if (getItem(position).getType().equals(TYPE.PICTURE.name())) {
                HolderPictureField holder = new HolderPictureField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_picture, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_picture_textView);
                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_picture_button);
                holder.deleteButton = (Button) view.findViewById(R.id.list_view_item_announce_information_picture_deleteButton);

                if(!edit_mode) holder.deleteButton.setVisibility(View.GONE);
                holder.title.setText(getItem(position).getTitle());
                final int pos = position;
                holder.deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EventUtils.getInstance(context).deleteDynamicField(event, pos);
                        notifyDataSetChanged();
                    }
                });
            } else if(getItem(position).getType().equals(TYPE.SIMPLETEXT.name()) ||
                    getItem(position).getType().equals(TYPE.LINK.name())) {
                HolderSimpleTextField holder = new HolderSimpleTextField();
                view = inflater.inflate(R.layout.list_view_item_announce_information_simpletext, viewGroup, false);
                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_simpletext_textView);
                holder.content = (EditText) view.findViewById(R.id.list_view_item_announce_information_simpletext_editText);
                holder.deleteButton = (Button) view.findViewById(R.id.list_view_item_announce_information_simpletext_deleteButton);

                holder.content.setOnFocusChangeListener(FocusChangeListener);
                holder.title.setText(getItem(position).getTitle());
                if(getItem(position).getValue() != null) holder.content.setText(getItem(position).getValue().toString());
                if(!edit_mode) holder.deleteButton.setVisibility(View.GONE);
                final int pos = position;
                holder.deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EventUtils.getInstance(context).deleteDynamicField(event, pos);
                        notifyDataSetChanged();
                    }
                });
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

    /**
     * Erstellt einen Dialog um ein Editierfeld auszuwählen.
     * @param position
     */
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

    /**
     * Erstellt je nach Auswahl, ein Editierfeld mit dem angegebenen Titel.
     * @param which Auswahl des Editierfelds
     * @param position Position in der ListView
     */
    public void createTitleInput(final int which, final int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        if (which == 0) alert.setTitle(R.string.field_picker_text);
        else if (which == 1) alert.setTitle(R.string.field_picker_picture);
        else if (which == 2) alert.setTitle(R.string.field_picker_link);

        final EditText input = new EditText(context);
        input.setSingleLine(true);

        // Setzt die Maximal-Länge
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(15); //Filter to 10 characters
        input.setFilters(filters);

        alert.setView(input);

        alert.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString() + ":";

                if (which == 0)
                    EventUtils.getInstance(context).addDynamicField(value, TYPE.SIMPLETEXT, event, position / 2);
                else if (which == 1)
                    EventUtils.getInstance(context).addDynamicField(value, TYPE.PICTURE, event, position / 2);
                else if (which == 2)
                    EventUtils.getInstance(context).addDynamicField(value, TYPE.LINK, event, position / 2);
                notifyDataSetChanged();
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    /**
     * Setzt den DateListener auf den DateButton.
     *
     * @param button Button des Editierfeldes Datum
     */
    private void setDateListener(final Button button) {
        final OnDateSetListener datePickerListener = new OnDateSetListener() {

            // when dialog box is closed, below method will be called.
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                year = selectedYear;
                month = selectedMonth;
                day = selectedDay;

                button.setText(day + "." + (month + 1) + "." + year);
            }
        };

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(context, datePickerListener, year, month, day).show();
            }
        });
    }

    /**
     * Setzt den TimeListener auf den Uhrzeit Button.
     *
     * @param button Button für die Uhrzeit
     */
    private void setTimeListener(final Button button){
        final OnTimeSetListener timePickerListener = new OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;
                if (minute < 10) {
                    button.setText(hour + ":0" + minute + " Uhr");
                } else {
                    button.setText(hour + ":" + minute + " Uhr");
                }
            }
        };

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(context, timePickerListener, hour, minute, true).show();
            }
        });
    }

    /**
     * Setzt die Standardzeit(20 Uhr).
     */
    private void setStandardTime(){
        hour = 20;
        minute = 0;
    }

    /**
     * Setzt das aktuelle Datum als Text auf den datePickerButton.
     */
    public void setCurrentDate() {
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Setzt den Click-Listener auf den Routeauswahl-Button um die Route auszuwählen.
     *
     * @param button Button um die Route zu setzen
     */
    private void setRouteListener(final Button button){
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseRouteIntent = new Intent(context, ChooseRouteActivity.class);
                context.startActivity(chooseRouteIntent);
            }
        });
    }

    /**
     * Dies Methode setzt die Route für das Event
     *
     * @param selectedRoute Die Route für das Event
     */
    public void setRoute(Route selectedRoute) {
        route = selectedRoute;
        routePickerButton.setText(selectedRoute.getName());
    }

    /**
     * Aktiviert den Editiermodus und updated die ListView.
     */
    public void startEditMode() {
        edit_mode = true;
        notifyDataSetChanged();
    }

    /**
     * Deaktiviert den Editiermodus und updated die ListView.
     */
    public void exitEditMode() {
        edit_mode = false;
        notifyDataSetChanged();
    }

    public boolean getEditMode() {
        return edit_mode;
    }

    public List<Field> getFieldlist(){
        return fieldList;
    }

    public Date getDate(){
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hour, minute);
        return cal.getTime();
    }

    public Route getRoute(){
        return route;
    }


}
