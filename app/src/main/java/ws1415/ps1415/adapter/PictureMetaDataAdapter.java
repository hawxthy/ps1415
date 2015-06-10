package ws1415.ps1415.adapter;

import android.content.Context;
import android.graphics.Picture;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.EventMetaData;
import com.skatenight.skatenightAPI.model.PictureData;
import com.skatenight.skatenightAPI.model.PictureFilter;
import com.skatenight.skatenightAPI.model.PictureMetaData;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.GalleryController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.DiskCacheImageLoader;

/**
 * Adapter zur Anzeige von Bild-Metadaten. Durch den im Konstruktor angegebenen Filter wird bestimmt,
 * welche Bilder abgerufen werden. Es kann ebenfalls angegeben werden, wieviele Bilder pro Aufruf vom
 * Server angefordert werden.
 * @author Richard Schulze
 */
public class PictureMetaDataAdapter extends BaseAdapter {
    /**
     * View-Type für Picture-Views.
     */
    private static final int PICTURE_VIEW_TYPE = 0;
    /**
     * View-Type für die View zum Anzeigen des Lade-Icons.
     */
    private static final int LOAD_VIEW_TYPE = 1;

    private Context context;
    private PictureFilter filter;
    private List<PictureMetaData> pictures = new LinkedList<>();

    private int fetchDistance;
    private Boolean fetching = false;
    private boolean keepFetching = true;

    /**
     * Erstellt einen neuen PictureMetaDataAdapter, der die auf den Filter passenden Bilder abruft.
     * @param context    Der aufrufende Kontext.
     * @param filter     Der anzuwendende Filter.
     */
    public PictureMetaDataAdapter(Context context, PictureFilter filter) {
        if (filter == null) {
            throw new NullPointerException("no filter submitted");
        }
        if (context == null) {
            throw new NullPointerException("no context submitted");
        }
        this.context = context;
        this.filter = filter;
        fetchDistance = (int) (filter.getLimit() * 0.4);
        fetchData(true);
    }

    @Override
    public int getCount() {
        return pictures.size() + (fetching ? 1 : 0);
    }

    @Override
    public PictureMetaData getItem(int position) {
        if (fetching && position == pictures.size()) {
            return null;
        }
        return pictures.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position < pictures.size()) {
            return pictures.get(position).getId();
        } else {
            return -1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < pictures.size()) {
            return PICTURE_VIEW_TYPE;
        } else {
            return LOAD_VIEW_TYPE;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (position == pictures.size()) {
            // Ladeanzeige
            if (convertView != null && getItemViewType(position) == LOAD_VIEW_TYPE) {
                view = convertView;
            } else {
                view = View.inflate(parent.getContext(), R.layout.listitem_fetching, null);
            }
        } else {
            PictureMetaData picture = getItem(position);
            if (convertView != null && getItemViewType(position) == PICTURE_VIEW_TYPE) {
                view = convertView;
            } else {
                view = View.inflate(parent.getContext(), R.layout.listitem_picture_meta_data, null);
            }

            ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            TextView title = (TextView) view.findViewById(R.id.title);
            RatingBar rating = (RatingBar) view.findViewById(R.id.avgRating);
            TextView date = (TextView) view.findViewById(R.id.date);
            if (picture.getId() >= 0) {
                view.findViewById(R.id.notVisibleText).setVisibility(View.INVISIBLE);
                if (thumbnail.getHeight() > 0 && thumbnail.getWidth() > 0) {
                    // Bild nur abrufen, wenn der Thumbnail-View Größen zugeordnet wurden
                    DiskCacheImageLoader.getInstance().loadScaledImage(thumbnail, picture.getImageBlobKey(), Math.min(thumbnail.getHeight(), thumbnail.getWidth()));
                }
                title.setText(picture.getTitle());
                ((TextView) view.findViewById(R.id.avgSymbol)).setText(Html.fromHtml(view.getResources().getString(R.string.average)));
                if (picture.getAvgRating() != null) {
                    rating.setRating(picture.getAvgRating().floatValue());
                } else {
                    rating.setRating(0);
                }
                Date dateValue = new Date(picture.getDate().getValue());
                date.setText(DateFormat.getMediumDateFormat(context).format(dateValue));
            } else {
                view.findViewById(R.id.notVisibleText).setVisibility(View.VISIBLE);
                thumbnail.setImageBitmap(null);
                title.setText(parent.getContext().getResources().getString(R.string.picture_not_visible));
                rating.setRating(0);
                date.setText(null);
            }
        }

        if (pictures.size() - position < fetchDistance) {
            fetchData(false);
        }

        return view;
    }

    /**
     * Ruft weitere Events vom Server ab.
     * @param refresh    Falls true, so wird die Liste der Events aktualisiert, d.h. die Liste wird
     *                   neu vom Server abgerufen und nicht erweitert.
     */
    private void fetchData(boolean refresh) {
        if (!keepFetching || fetching) {
            return;
        }
        synchronized (fetching) {
            if (fetching) {
                return;
            } else {
                fetching = true;
            }
        }

        // Lade-Icon anzeigen lassen
        notifyDataSetChanged();

        if (refresh) {
            filter.setCursorString(null);
        }

        GalleryController.listPictures(new ExtendedTaskDelegateAdapter<Void, List<PictureMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<PictureMetaData> newPictures) {
                if (newPictures != null) {
                    pictures.addAll(newPictures);
                } else {
                    keepFetching = false;
                }
                finish();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                finish();
            }

            /**
             * Beendet das Abrufen der Daten unabhängig davon, ob das Abrufen erfolgreich war oder
             * fehlgeschlagen ist.
             */
            private void finish() {
                fetching = false;
                PictureMetaDataAdapter.this.notifyDataSetChanged();
            }
        }, filter);
    }

    /**
     * Veranlasst den Adapter die Eventliste neu herunterzuladen. Es wird dabei an den Anfang der
     * Liste gescrollt.
     */
    public void refresh() {
        pictures.clear();
        keepFetching = true;
        fetchData(true);
    }

    /**
     * Entfernt das angegebene Bild aus diesem Adapter.
     * @param picture    Das zu entfernende Bild.
     */
    public void removePicture(PictureMetaData picture) {
        pictures.remove(picture);
        notifyDataSetChanged();
    }

    /**
     * Lädt das Bild an der angegebenen Position neu herunter.
     * @param position    Die Position des Bildes, das neu geladen wird.
     */
    public void reloadPicture(int position) {
        final PictureMetaData picture = getItem(position);
        GalleryController.getPicture(new ExtendedTaskDelegateAdapter<Void, PictureData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, PictureData pictureData) {
                picture.setTitle(pictureData.getTitle());
                picture.setDate(pictureData.getDate());
                picture.setAvgRating(pictureData.getAvgRating());
                picture.setVisibility(pictureData.getVisibility());
                PictureMetaDataAdapter.this.notifyDataSetChanged();
            }
        }, picture.getId());
    }
}
