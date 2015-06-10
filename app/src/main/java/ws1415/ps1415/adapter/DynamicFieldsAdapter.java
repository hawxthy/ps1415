package ws1415.ps1415.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.DynamicField;

import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.R;

/**
 * Adapter zur Anzeige der dynamischen Felder eines Events.
 * @author Richard Schulze
 */
public class DynamicFieldsAdapter extends BaseAdapter {
    private boolean edited;
    private int itemLayoutId;
    private List<DynamicField> dynamicFields;

    public DynamicFieldsAdapter(List<DynamicField> dynamicFields, boolean editable) {
        if (editable) {
            itemLayoutId = R.layout.listitem_editable_dynamic_field;
        } else {
            itemLayoutId = R.layout.listitem_dynamic_field;
        }
        this.dynamicFields = dynamicFields;
        if (this.dynamicFields == null) {
            this.dynamicFields = new LinkedList<>();
        }
    }

    @Override
    public int getCount() {
        return dynamicFields.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < dynamicFields.size()) {
            return dynamicFields.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        final DynamicField field = (DynamicField) getItem(position);
        if (field != null) {
            if (convertView != null) {
                view = convertView;
            } else {
                view = View.inflate(parent.getContext(), itemLayoutId, null);
            }

            if (itemLayoutId == R.layout.listitem_editable_dynamic_field) {
                // Wenn editierbar, dann Listener für Änderungen an den Daten hinzufügen
                final EditText label = (EditText) view.findViewById(R.id.fieldLabel);
                label.setText(field.getName());
                label.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            if (!edited) {
                                edited = !label.getText().toString().equals(field.getName());
                            }
                            field.setName(label.getText().toString());
                        }
                    }
                });
                final EditText content = (EditText) view.findViewById(R.id.fieldContent);
                content.setText(field.getContent());
                content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            if (!edited) {
                                edited = !content.getText().toString().equals(field.getContent());
                            }
                            field.setContent(content.getText().toString());
                        }
                    }
                });
            } else {
                TextView label = (TextView) view.findViewById(R.id.fieldLabel);
                label.setText(field.getName());
                TextView content = (TextView) view.findViewById(R.id.fieldContent);
                content.setText(field.getContent());
            }
        }
        return view;
    }

    public List<DynamicField> getList() {
        return dynamicFields;
    }

    /**
     * Fügt ein neues leeres Feld zu diesem Adapter hinzu.
     */
    public void addField() {
        dynamicFields.add(new DynamicField());
        edited = true;
        notifyDataSetChanged();
    }

    /**
     * @return true, wenn die Daten im Adapter editiert wurden.
     */
    public boolean isEdited() {
        return edited;
    }
}
