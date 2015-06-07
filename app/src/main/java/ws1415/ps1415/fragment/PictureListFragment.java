package ws1415.ps1415.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.skatenight.skatenightAPI.model.PictureMetaData;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.PictureMetaDataAdapter;

/**
 * Zeigt ein Raster von Bildern an. Welche Bilder angezeigt werden, wird durch den Adapter bestimmt,
 * der mit der Methode {@code setAdapter(...)} angegeben wird.
 */
public class PictureListFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private OnPictureClickListener mListener;

    private GridView grid;
    private PictureMetaDataAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picturegrid, container, false);
        grid = (GridView) view.findViewById(R.id.pictureGrid);
        grid.setOnItemClickListener(this);
        grid.setOnItemLongClickListener(this);

        View emptyView = grid.getEmptyView();
        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(R.string.no_visible_pictures);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPictureClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPictureClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Setzt den Adapter für dieses Fragment.
     * @param adapter    Der Adapter, der zur Anzeige von Bildern verwendet wird.
     */
    public void setAdapter(PictureMetaDataAdapter adapter) {
        this.adapter = adapter;
        grid.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onPictureClick(adapter.getPictureMetaData(position), position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return mListener.onPictureLongClick(adapter.getPictureMetaData(position), position);
    }

    /**
     * Dieses Interface muss von Activities implementiert werden, die das Fragment verwenden.
     * Es definiert Callback-Methoden für Klicks auf die angezeigten Bilder.
     */
    public interface OnPictureClickListener {
        void onPictureClick(PictureMetaData picture, int position);
        boolean onPictureLongClick(PictureMetaData picture, int position);
    }

}
