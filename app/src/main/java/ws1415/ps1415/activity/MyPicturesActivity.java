package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.Gallery;
import com.skatenight.skatenightAPI.model.GalleryMetaData;
import com.skatenight.skatenightAPI.model.PictureFilter;
import com.skatenight.skatenightAPI.model.PictureMetaData;
import com.skatenight.skatenightAPI.model.UserGalleryContainer;

import java.util.ArrayList;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.GalleryAdapter;
import ws1415.ps1415.adapter.PictureMetaDataAdapter;
import ws1415.ps1415.controller.GalleryController;
import ws1415.ps1415.fragment.PictureListFragment;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

public class MyPicturesActivity extends BaseActivity implements PictureListFragment.OnPictureClickListener {
    private static final int UPLOAD_PICTURE_REQUEST_CODE = 1;
    private static final int EDIT_PICTURE_REQUEST_CODE = 2;

    /**
     * Bestimmt die Anzahl Bilder, die pro Aufruf an den Server herunter geladen werden.
     */
    private static final int PICTURES_PER_REQUEST = 15;

    private Long userGalleryContainerId;
    private Spinner galleries;
    private GalleryAdapter galleryAdapter;

    private PictureListFragment pictureFragment;
    private PictureMetaDataAdapter pictureAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pictures);

        pictureFragment = (PictureListFragment) getFragmentManager().findFragmentById(R.id.picturesFragment);
        galleries = (Spinner) findViewById(R.id.galleries);

        GalleryController.getGalleryContainerForMail(new ExtendedTaskDelegateAdapter<Void, UserGalleryContainer>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGalleryContainer userGalleryContainer) {
                userGalleryContainerId = userGalleryContainer.getId();
                galleryAdapter = new GalleryAdapter(MyPicturesActivity.this, UserGalleryContainer.class.getSimpleName(), userGalleryContainer.getId(), true);
                galleries = (Spinner) findViewById(R.id.galleries);
                galleries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        PictureFilter filter = new PictureFilter();
                        if (id == GalleryAdapter.EMPTY_ITEM_GALLERY_ID) {
                            filter.setUserId(ServiceProvider.getEmail());
                            findViewById(R.id.removeGallery).setVisibility(View.INVISIBLE);
                            findViewById(R.id.editGallery).setVisibility(View.INVISIBLE);
                        } else {
                            filter.setGalleryId(id);
                            findViewById(R.id.removeGallery).setVisibility(View.VISIBLE);
                            findViewById(R.id.editGallery).setVisibility(View.VISIBLE);
                        }
                        filter.setLimit(PICTURES_PER_REQUEST);
                        pictureAdapter = new PictureMetaDataAdapter(MyPicturesActivity.this, filter);
                        pictureFragment.setAdapter(pictureAdapter);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Sollte nicht vorkommen
                    }
                });
                galleries.setAdapter(galleryAdapter);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(MyPicturesActivity.this, R.string.error_loading_galleries, Toast.LENGTH_LONG).show();
            }
        }, ServiceProvider.getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_pictures, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_upload_picture) {
            Intent intent = new Intent(this, EditPictureActivity.class);
            startActivityForResult(intent, UPLOAD_PICTURE_REQUEST_CODE);
            return true;
        } else if (id == R.id.action_refresh_pictures) {
            pictureAdapter.refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UPLOAD_PICTURE_REQUEST_CODE:
                pictureAdapter.refresh();
                break;
            case EDIT_PICTURE_REQUEST_CODE:
                if (data != null && data.getIntExtra("position", -1) >= 0) {
                    pictureAdapter.reloadPicture(data.getIntExtra("position", -1));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPictureClick(PictureMetaData picture, int position) {
        if (picture.getId() >= 0) {
            // Nur Bilder mit gültiger ID anzeigen, da nur diese für den Benutzer einsehbar sind
            Intent intent = new Intent(MyPicturesActivity.this, ShowPictureActivity.class);
            intent.putExtra(ShowPictureActivity.EXTRA_PICTURE_ID, picture.getId());
            intent.putExtra(ShowPictureActivity.EXTRA_POSITION, position);
            startActivityForResult(intent, EDIT_PICTURE_REQUEST_CODE);
        }
    }

    @Override
    public boolean onPictureLongClick(final PictureMetaData picture, final int position) {
        if (picture.getId() >= 0) {
            // Nur Bilder mit gültiger ID anzeigen, da nur diese für den Benutzer einsehbar sind
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String[] items = getResources().getStringArray(R.array.picture_actions);
            if (galleries.getSelectedItemId() != GalleryAdapter.EMPTY_ITEM_GALLERY_ID) {
                // Falls nach einer Gallery gefiltert wurde, dann Option zum Entfernen des Bildes
                // aus der Gallery anbieten
                String[] tmp = new String[items.length + 1];
                for (int i = 0; i < items.length; i++) {
                    tmp[i] = items[i];
                }
                tmp[tmp.length - 1] = getResources().getString(R.string.action_remove_from_gallery);
                items = tmp;
            }
            builder.setTitle(picture.getTitle())
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent;
                            switch (which) {
                                case 0:
                                    // Anzeigen
                                    intent = new Intent(MyPicturesActivity.this, ShowPictureActivity.class);
                                    intent.putExtra(ShowPictureActivity.EXTRA_PICTURE_ID, picture.getId());
                                    intent.putExtra(ShowPictureActivity.EXTRA_POSITION, position);
                                    startActivityForResult(intent, EDIT_PICTURE_REQUEST_CODE);
                                    break;
                                case 1:
                                    // Bearbeiten
                                    intent = new Intent(MyPicturesActivity.this, EditPictureActivity.class);
                                    intent.putExtra(EditPictureActivity.EXTRA_PICTURE_ID, picture.getId());
                                    intent.putExtra(EditPictureActivity.EXTRA_POSITION, position);
                                    startActivityForResult(intent, EDIT_PICTURE_REQUEST_CODE);
                                    break;
                                case 2:
                                    // Löschen
                                    deletePicture(picture);
                                    break;
                                case 3:
                                    // Aus der Galerie entfernen
                                    removePictureFromGallery(picture);
                                    break;
                            }
                        }
                    });
            builder.create().show();
            return true;
        }
        return false;
    }

    private void removePictureFromGallery(final PictureMetaData picture) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remove_from_gallery)
                .setMessage(R.string.remove_from_gallery_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GalleryController.removePictureFromGallery(new ExtendedTaskDelegateAdapter<Void, Void>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                pictureAdapter.removePicture(picture);
                            }

                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(MyPicturesActivity.this, R.string.error_removing_from_gallery, Toast.LENGTH_LONG).show();
                            }
                        }, picture.getId(), galleries.getSelectedItemId());
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    private void deletePicture(final PictureMetaData picture) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_picture)
                .setMessage(R.string.delete_picture_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO R: Weitere Benutzereingaben während des löschens verhindern
                        GalleryController.deletePicture(new ExtendedTaskDelegateAdapter<Void, Void>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                pictureAdapter.removePicture(picture);
                            }
                        }, picture.getId());
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    public void onAddGalleryClick(View view) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_textinput, null);
        final EditText input = (EditText) dialogView.findViewById(android.R.id.text1);
        input.setHint(R.string.enter_gallery_title);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.create_gallery)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
        // Handler für den Ja-Button später setzen, damit das Schließen des Dialogs bei falschen Eingaben verhindert werden kann
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = input.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(MyPicturesActivity.this, R.string.no_title_submitted, Toast.LENGTH_SHORT).show();
                } else {
                    Gallery gallery = new Gallery();
                    gallery.setTitle(title);
                    gallery.setContainerClass(UserGalleryContainer.class.getSimpleName());
                    gallery.setContainerId(userGalleryContainerId);
                    try {
                        GalleryController.createGallery(new ExtendedTaskDelegateAdapter<Void, Gallery>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, Gallery gallery) {
                                galleryAdapter.addGallery(new GalleryMetaData()
                                        .setId(gallery.getId())
                                        .setTitle(gallery.getTitle()));
                                galleries.setSelection(galleryAdapter.getCount() - 1);
                            }
                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(MyPicturesActivity.this, R.string.error_creating_gallery, Toast.LENGTH_LONG).show();
                            }
                        }, gallery);
                        dialog.dismiss();
                    } catch(IllegalArgumentException ex) {
                        Toast.makeText(MyPicturesActivity.this, R.string.no_title_submitted, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void onDeleteGalleryClick(View view) {
        final long galleryId = galleries.getSelectedItemId();
        final int position = galleries.getSelectedItemPosition();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_gallery)
                .setMessage(R.string.delete_gallery_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GalleryController.deleteGallery(new ExtendedTaskDelegateAdapter<Void, Void>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                galleries.setSelection(0);
                                galleryAdapter.removeGallery(position);
                            }
                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(MyPicturesActivity.this, R.string.error_deleting_gallery, Toast.LENGTH_LONG).show();
                            }
                        }, galleryId);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    public void onEditGalleryClick(View view) {
        final GalleryMetaData gallery = galleryAdapter.getItem(galleries.getSelectedItemPosition());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_textinput, null);
        final EditText input = (EditText) dialogView.findViewById(android.R.id.text1);
        input.setText(gallery.getTitle());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.create_gallery)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
        // Handler für den Ja-Button später setzen, damit das Schließen des Dialogs bei falschen Eingaben verhindert werden kann
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String title = input.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(MyPicturesActivity.this, R.string.no_title_submitted, Toast.LENGTH_SHORT).show();
                } else {
                    Gallery editedGallery = new Gallery();
                    editedGallery.setId(gallery.getId());
                    editedGallery.setTitle(title);
                    editedGallery.setContainerClass(gallery.getContainerClass());
                    editedGallery.setContainerId(gallery.getContainerId());
                    gallery.setTitle(title);
                    try {
                        GalleryController.editGallery(new ExtendedTaskDelegateAdapter<Void, Gallery>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, Gallery gallery) {
                                gallery.setTitle(title);
                                galleryAdapter.notifyDataSetChanged();
                            }
                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(MyPicturesActivity.this, R.string.error_creating_gallery, Toast.LENGTH_LONG).show();
                            }
                        }, editedGallery);
                        dialog.dismiss();
                    } catch(IllegalArgumentException ex) {
                        Toast.makeText(MyPicturesActivity.this, R.string.no_title_submitted, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
