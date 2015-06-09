package ws1415.ps1415.model;

import android.content.DialogInterface;
import android.content.Intent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Datenmodell für ein Kontextmenü, das in AlertDialog-Objekten verwendet werden kann.
 * @author Richard Schulze
 */
public class ContextMenu {
    private List<ContextMenuItem> items;
    private String[] itemStrings;
    private DialogInterface.OnClickListener clickListener;

    public ContextMenu(List<ContextMenuItem> itemsParam) {
        this.items = new ArrayList<>(itemsParam);
        itemStrings = new String[itemsParam.size()];
        for (int i = 0; i < itemsParam.size(); i++) {
            itemStrings[i] = itemsParam.get(i).getText();
        }
        clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                items.get(which).onClick();
            }
        };
    }

    public DialogInterface.OnClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(DialogInterface.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public String[] getItemStrings() {
        return itemStrings;
    }

    public void setItemStrings(String[] itemStrings) {
        this.itemStrings = itemStrings;
    }

    /**
     * Interface für Einträge eines Kontextmenüs.
     * @author Richard Schulze
     */
    public interface ContextMenuItem {
        String getText();
        void onClick();
    }
}
