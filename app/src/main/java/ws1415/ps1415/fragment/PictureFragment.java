package ws1415.ps1415.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.Gallery;
import com.skatenight.skatenightAPI.model.GalleryMetaData;
import com.skatenight.skatenightAPI.model.PictureData;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.GalleryAdapter;
import ws1415.ps1415.controller.GalleryController;
import ws1415.ps1415.model.PictureVisibility;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.DiskCacheImageLoader;

/**
 * Fragment zur vollständigen Anzeige eines Bildes inkl. Kommentaren.
 */
public class PictureFragment extends Fragment implements RatingBar.OnRatingBarChangeListener {
    private MenuItem addToGalleryItem;

    private ProgressBar pictureLoading;

    private ImageView picture;
    private TextView title;
    private TextView date;
    private TextView uploader;
    private TextView description;
    private RatingBar rating;

    private Long pictureId;
    private boolean initializing = true;
    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picture, container, false);
        pictureLoading = (ProgressBar) view.findViewById(R.id.pictureLoading);
        picture = (ImageView) view.findViewById(R.id.picture);
        title = (TextView) view.findViewById(R.id.title);
        date = (TextView) view.findViewById(R.id.date);
        uploader = (TextView) view.findViewById(R.id.uploader);
        description = (TextView) view.findViewById(R.id.description);
        rating = (RatingBar) view.findViewById(R.id.rating);
        rating.setOnRatingBarChangeListener(this);
        rating.setStepSize(1);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_picture_fragment, menu);
        addToGalleryItem = menu.findItem(R.id.action_add_to_gallery);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_to_gallery:
                addPictureToGallery();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Lädt das Bild mit der angegebenen ID in diesem Fragment.
     * @param pictureId    Die ID des zu ladenden Bildes.
     * @param position     Die Position des Bildes im Adapter. Wird über den Intent zurückgegeben,
     *                     falls Änderungen am Bild gemacht wurden.
     */
    public void loadPicture(long pictureId, int position) {
        this.pictureId = pictureId;
        this.position = position;
        startLoading();
        GalleryController.getPicture(new ExtendedTaskDelegateAdapter<Void, PictureData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, PictureData pictureData) {
                DiskCacheImageLoader.getInstance().loadScaledImage(picture, pictureData.getImageBlobKey(), picture.getWidth());
                title.setText(pictureData.getTitle());

                Date dateValue = new Date(pictureData.getDate().getValue());
                date.setText(DateFormat.getMediumDateFormat(PictureFragment.this.getActivity()).format(dateValue)
                        + " " + DateFormat.getTimeFormat(PictureFragment.this.getActivity()).format(dateValue));

                // TODO R: Nicht die Mail-Adresse des Uploaders anzeigen
                uploader.setText(pictureData.getUploader());
                description.setText(pictureData.getDescription());

                // TODO R: Eigene und durchschnittliche Bewertung getrennt anzeigen
                if (pictureData.getMyRating() != null) {
                    rating.setRating(pictureData.getMyRating());
                } else if (pictureData.getAvgRating() != null) {
                    rating.setRating(pictureData.getAvgRating().floatValue());
                } else {
                    rating.setRating(0);
                }

                if (PictureVisibility.valueOf(pictureData.getVisibility()) == PictureVisibility.PRIVATE) {
                    addToGalleryItem.setVisible(false);
                } else {
                    addToGalleryItem.setVisible(true);
                }

                finishLoading();
                initializing = false;
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(PictureFragment.this.getActivity(), R.string.error_loading_picture, Toast.LENGTH_LONG).show();
                finishLoading();
            }
        }, pictureId);
    }

    /**
     * Startet die Ladeanimation.
     */
    private void startLoading() {
        pictureLoading.setVisibility(View.VISIBLE);
    }

    /**
     * Beendet die Ladeanimation.
     */
    private void finishLoading() {
        pictureLoading.setVisibility(View.GONE);
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (pictureId != null && !initializing) {
            GalleryController.ratePicture(new ExtendedTaskDelegateAdapter<Void, Void>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Void aVoid) {
                    Toast.makeText(PictureFragment.this.getActivity(), R.string.rating_saved, Toast.LENGTH_SHORT).show();
                    getActivity().setResult(0, new Intent().putExtra("position", position));
                }
                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    Toast.makeText(PictureFragment.this.getActivity(), R.string.error_rating_picture, Toast.LENGTH_LONG).show();
                }
            }, pictureId, (int) rating);
        }
    }

    private void addPictureToGallery() {
        final GalleryAdapter galleryAdapter = new GalleryAdapter(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.action_add_to_gallery)
                .setAdapter(galleryAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GalleryController.addPictureToGallery(new ExtendedTaskDelegateAdapter<Void, Void>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                Toast.makeText(getActivity(), R.string.adding_to_gallery_finished, Toast.LENGTH_LONG).show();
                            }
                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(getActivity(), R.string.error_adding_to_gallery, Toast.LENGTH_LONG).show();
                            }
                        }, pictureId, galleryAdapter.getItemId(which));
                    }
                });
        builder.create().show();
    }
}
