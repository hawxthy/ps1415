package ws1415.ps1415.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;
import com.skatenight.skatenightAPI.model.Text;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.ShowRouteActivity;
import ws1415.ps1415.util.FieldType;
import ws1415.ps1415.util.ImageUtil;

/**
 * Adapter zum füllen der ListView in der ShowInformationActivity.
 *
 * @author Bernd Eissing, Martin Wrodarczyk, Richard Schulze
 */
public class ShowCursorAdapter extends BaseAdapter {
    private List<Field> fieldList = new ArrayList<Field>();
    private Context context;
    private LayoutInflater inflater;
    private Event event;


    /**
     * Cache für die Bilder des Eevnts, damit diese nicht bei jedem Scrollen neu skaliert werden müssen.
     */
    private HashMap<Field, Bitmap> bitmapCache = new HashMap<Field, Bitmap>();

    /**
     * Konstruktor, der den Inhalt der Liste festlegt.
     *
     * @param context   Context, von dem aus der Adapter aufgerufen wird
     * @param fieldList List von den Routen
     */
    public ShowCursorAdapter(Context context, List<Field> fieldList, Event event) {
        this.context = context;
        this.fieldList = fieldList;
        this.event = event;
    }

    /**
     * Gibt die Anzhl der bearbeitenden EventFelder in der Liste zurück.
     *
     * @return Anzahl der EventFelder
     */
    @Override
    public int getCount() {
        if (fieldList == null) {
            return 0;
        } else {
            return fieldList.size();
        }
    }

    /**
     * Gibt das bearbeitende Feld an der Stelle i in der Liste zurück.
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
     * Klasse zum Halten der GUI Elemente für ein TextField.
     */
    private class HolderTextField {
        TextView title;
        TextView content;
    }

    /**
     * Klasse zum Halten der GUI Elemente für ein ImageField.
     */
    private class HolderImageField {
        TextView title;
        ImageView image;
    }

    /**
     * Fügt die Items der ListView hinzu. Hier wird nach Typen unterschieden und
     * je Typ ein anderes Item hinzugefügt.
     *
     * @param position Position der ListView
     * @param convertView
     * @param viewGroup
     * @return
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        View view = null;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (getItem(position).getType() == FieldType.TITLE.getId() ||
                getItem(position).getType() == FieldType.LOCATION.getId() ||
                getItem(position).getType() == FieldType.DESCRIPTION.getId() ||
                getItem(position).getType() == FieldType.SIMPLETEXT.getId()) {
            HolderTextField holder = new HolderTextField();
            view = inflater.inflate(R.layout.list_view_item_show_information_text_field, viewGroup, false);
            view.setEnabled(false);
            view.setOnClickListener(null);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_show_information_text_field_textView_title);
            holder.content = (TextView) view.findViewById(R.id.list_view_item_show_information_text_field_textView_content);
            String contentValue = fieldList.get(position).getValue();
            holder.title.setText(fieldList.get(position).getTitle());
            if(contentValue != null) holder.content.setText(contentValue);
            else holder.content.setText("n/a");

        } else if (getItem(position).getType() == FieldType.PICTURE.getId()) {
            HolderImageField holder = new HolderImageField();
            view = inflater.inflate(R.layout.list_view_item_show_information_image_field, viewGroup, false);
            view.setEnabled(false);
            view.setOnClickListener(null);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_show_information_image_field_textView_title);
            holder.image = (ImageView) view.findViewById(R.id.list_view_item_show_information_image_field_imageView);
            holder.title.setText(fieldList.get(position).getTitle());
            Bitmap bm = bitmapCache.get(getItem(position));
            if (bm == null) {
                Text encodedBytes = getItem(position).getData();
                if (encodedBytes != null) {
                    byte[] bytes = Base64.decodeBase64(encodedBytes.getValue());
                    // Zunächst nur Auflösung des Bilds abrufen und passende SampleSize berechnen
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    options.inSampleSize = ImageUtil.calculateInSampleSize(options, 720);
                    // Skalierte Version des Bilds abrufen
                    options.inJustDecodeBounds = false;
                    bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    bitmapCache.put(getItem(position), bm);
                }
            }
            // Bild anzeigen
            holder.image.setImageBitmap(bm);
        } else if (getItem(position).getType() == FieldType.ROUTE.getId()) {
            HolderButtonField holder = new HolderButtonField();
            view = inflater.inflate(R.layout.list_view_item_show_information_button_field, viewGroup, false);
            view.setEnabled(false);
            view.setOnClickListener(null);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_show_information_button_field_textView_title);
            holder.button = (Button) view.findViewById(R.id.list_view_item_show_information_button_field_button);
            holder.title.setText(fieldList.get(position).getTitle());
            holder.button.setText(event.getRoute().getName());
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (event.getRoute() != null && event.getRoute().getRouteData() != null) {
                        showMap();
                    }
                }
            });

        } else if (getItem(position).getType() == FieldType.FEE.getId()) {
            HolderTextField holder = new HolderTextField();
            view = inflater.inflate(R.layout.list_view_item_show_information_text_field, viewGroup, false);
            view.setEnabled(false);
            view.setOnClickListener(null);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_show_information_text_field_textView_title);
            holder.content = (TextView) view.findViewById(R.id.list_view_item_show_information_text_field_textView_content);
            String contentValue = fieldList.get(position).getValue();
            holder.title.setText(fieldList.get(position).getTitle());
            if(contentValue != null) holder.content.setText(contentValue.toString() + " €");
            else holder.content.setText("n/a");

        } else if (getItem(position).getType() == FieldType.TIME.getId()) {
            HolderTextField holder = new HolderTextField();
            view = inflater.inflate(R.layout.list_view_item_show_information_text_field, viewGroup, false);
            view.setEnabled(false);
            view.setOnClickListener(null);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_show_information_text_field_textView_title);
            holder.content = (TextView) view.findViewById(R.id.list_view_item_show_information_text_field_textView_content);
            holder.title.setText(fieldList.get(position).getTitle());
            // muss noch richtig gemacht werden
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            holder.content.setText(dateFormat.format(new Date(Long.parseLong(fieldList.get(position).getValue()))));

        } else if (getItem(position).getType() == FieldType.LINK.getId()) {
            HolderTextField holder = new HolderTextField();
            view = inflater.inflate(R.layout.list_view_item_show_information_text_field, viewGroup, false);
            holder.title = (TextView) view.findViewById(R.id.list_view_item_show_information_text_field_textView_title);
            holder.content = (TextView) view.findViewById(R.id.list_view_item_show_information_text_field_textView_content);
            String contentValue = fieldList.get(position).getValue();
            holder.title.setText(fieldList.get(position).getTitle());
            if(contentValue != null){
                holder.content.setText(contentValue);
                holder.content.setTextColor(Color.BLUE);
            } else {
                holder.content.setText("n/a");
            }
            final String link = contentValue;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(link != null) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("http://" + link));
                        context.startActivity(i);
                    }
                }
            });
        } else if(getItem(position).getType() == FieldType.DATE.getId()){
            view = new View(context);
        }
        return view;
    }

    /**
     * Öffnet die Map mit der Route für das Event.
     */
    private void showMap() {
        // Leite wieter auf die Karte
        Intent intent = new Intent(context, ShowRouteActivity.class);
        intent.putExtra(ShowRouteActivity.EXTRA_ROUTE, event.getRoute().getRouteData().getValue());
        intent.putExtra(ShowRouteActivity.EXTRA_ROUTE_FIELD_FIRST, event.getRouteFieldFirst());
        intent.putExtra(ShowRouteActivity.EXTRA_ROUTE_FIELD_LAST, event.getRouteFieldLast());
        intent.putExtra(ShowRouteActivity.EXTRA_WAYPOINTS, (Serializable) event.getRoute().getWaypoints());
        context.startActivity(intent);
    }
}