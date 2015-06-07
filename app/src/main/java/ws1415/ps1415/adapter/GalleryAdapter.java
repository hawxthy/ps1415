package ws1415.ps1415.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.GalleryMetaData;

import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.GroupProfileActivity;
import ws1415.ps1415.activity.ShowRouteActivity;
import ws1415.ps1415.controller.GalleryController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Adapter zur Anzeige von Gallerien. Ist hauptsächlich für die Verwendung in Spinnern vorgesehen.
 * @author Richard Schulze
 */
public class GalleryAdapter extends BaseAdapter {
    public static final long EMPTY_ITEM_GALLERY_ID = -1;

    private List<GalleryMetaData> galleries = new LinkedList<>();

    /**
     * Erstellt einen neuen GalleryAdapter mit den angegebenen Daten. Der Adapter ruft die Gallerien
     * für den angegebenen Container selbstständig vom Server ab.
     * @param context          Der Context, der zum Auflösen der Strings für den leeren Eintrag
     *                         benötigt wird.
     * @param containerKind    Der Datastore-Kind des Containers.
     * @param containerId      Die ID des Containers.
     * @param addEmptyItem     true, wenn ein zusätzliches "leeres" Item am Beginn des Adapters ein-
     *                         gefügt werden soll. Dieser kann genutzt werden, um "keine Gallery aus-
     *                         gewählt" zu signalisieren. Die GalleryId dieses Eintrags ist in
     *                         {@code EMPTY_ITEM_GALLERY_ID} angegeben.
     */
    public GalleryAdapter(final Context context, String containerKind, long containerId, boolean addEmptyItem) {
        if (addEmptyItem) {
            GalleryMetaData emptyItem = new GalleryMetaData();
            emptyItem.setId(EMPTY_ITEM_GALLERY_ID);
            emptyItem.setTitle(context.getString(R.string.galleries_empty_item_title));
            galleries.add(emptyItem);
        }
        GalleryController.getGalleries(new ExtendedTaskDelegateAdapter<Void, List<GalleryMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<GalleryMetaData> galleryMetaDatas) {
                if (galleryMetaDatas != null) {
                    galleries.addAll(galleryMetaDatas);
                    GalleryAdapter.this.notifyDataSetChanged();
                }
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(context, R.string.error_loading_galleries, Toast.LENGTH_LONG).show();
            }
        }, containerKind, containerId);
    }

    /**
     * Erstellt einen Adapter mit allen Galleries, denen der eingeloggte Benutzer Bilder hinzufügen
     * kann. Der Adapter ruft dazu einen entsprechende Methode auf dem Backend auf.
     * @param context    Der Kontext, in dem der Adapter erstellt wird.
     */
    public GalleryAdapter(final Context context) {
        GalleryController.getExpandableGalleries(new ExtendedTaskDelegateAdapter<Void, List<GalleryMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<GalleryMetaData> galleryMetaDatas) {
                if (galleryMetaDatas != null) {
                    galleries.addAll(galleryMetaDatas);
                    GalleryAdapter.this.notifyDataSetChanged();
                }
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(context, R.string.error_loading_galleries, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getCount() {
        return galleries.size();
    }

    @Override
    public Object getItem(int position) {
        return galleries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return galleries.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO R: Layout der Spinneritems anpassen
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = parent.inflate(parent.getContext(), android.R.layout.simple_spinner_dropdown_item, null);
        }
        GalleryMetaData gallery = galleries.get(position);
        ((TextView) view.findViewById(android.R.id.text1)).setText(gallery.getTitle());
        return view;
    }

    /**
     * Fügt die angegebene Gallery zu diesem Adapter hinzu.
     */
    public void addGallery(GalleryMetaData gallery) {
        galleries.add(gallery);
        notifyDataSetChanged();
    }

    /**
     * Entfernt die Gallery an der angegebenen Position aus dem Adapter.
     * @param position    Die Position der zu entfernenden Gallery.
     */
    public void removeGallery(int position) {
        galleries.remove(position);
        notifyDataSetChanged();
    }
}
