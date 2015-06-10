package ws1415.ps1415.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.GalleryMetaData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.GalleryController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Adapter zur Anzeige von Gallerien, denen ein gegebenes Bild hinzugefügt werden kann.
 * @author Richard Schulze
 */
public class ExpandableGalleryAdapter extends BaseAdapter {
    private List<GalleryMetaData> galleries = new LinkedList<>();
    private Map<GalleryMetaData, String> additionalTitles = new HashMap<>();

    /**
     * Erstellt einen Adapter mit allen Galleries, denen der eingeloggte Benutzer das angegebene Bild
     * hinzufügen kann. Der Adapter ruft dazu einen entsprechende Methode auf dem Backend auf.
     * @param context    Der Kontext, in dem der Adapter erstellt wird.
     * @param pictureId  Die ID des Bildes, das zu einer Galerie hinzugefügt werden soll.
     */
    public ExpandableGalleryAdapter(final Context context, long pictureId) {
        GalleryController.getExpandableGalleries(new ExtendedTaskDelegateAdapter<Void, Map<GalleryMetaData, String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Map<GalleryMetaData, String> galleryData) {
                if (galleryData != null) {
                    galleries.addAll(galleryData.keySet());
                    additionalTitles.putAll(galleryData);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }, pictureId);
    }

    @Override
    public int getCount() {
        return galleries.size();
    }

    @Override
    public GalleryMetaData getItem(int position) {
        return galleries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return galleries.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = View.inflate(parent.getContext(), R.layout.gallery_spinner_item, null);
        }
        GalleryMetaData gallery = getItem(position);
        ((TextView) view.findViewById(android.R.id.text1)).setText(gallery.getTitle());
        ((TextView) view.findViewById(android.R.id.text2)).setText(additionalTitles.get(gallery));
        return view;
    }
}
