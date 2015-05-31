package ws1415.ps1415.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
    private List<DynamicField> dynamicFields;

    public DynamicFieldsAdapter(List<DynamicField> dynamicFields) {
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
        DynamicField field = (DynamicField) getItem(position);
        if (field != null) {
            if (convertView != null) {
                view = convertView;
            } else {
                view = View.inflate(parent.getContext(), R.layout.listitem_dynamic_field, null);
            }
            TextView label = (TextView) view.findViewById(R.id.fieldLabel);
            label.setText(field.getName());
            TextView content = (TextView) view.findViewById(R.id.fieldContent);
            content.setText(field.getContent());
        }
        return view;
    }
}
