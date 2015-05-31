package ws1415.ps1415.model;

import android.view.View;
import android.widget.AdapterView;

/**
 * Model-Klasse für die Elemente in dem Navigation Drawer.
 *
 * @author Richard Schulze
 */
public interface NavDrawerItem {
    int getTitleId();
    int getIconId();
    void onClick(AdapterView<?> parent, View view, int position, long id);
}