package ws1415.ps1415.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.Gallery;
import com.skatenight.skatenightAPI.model.GalleryContainerData;
import com.skatenight.skatenightAPI.model.GalleryMetaData;
import com.skatenight.skatenightAPI.model.PictureFilter;
import com.skatenight.skatenightAPI.model.PictureMetaData;
import com.skatenight.skatenightAPI.model.UserGalleryContainer;

import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.GalleryAdapter;
import ws1415.ps1415.adapter.PictureMetaDataAdapter;
import ws1415.ps1415.controller.GalleryController;
import ws1415.ps1415.controller.RoleController;
import ws1415.ps1415.fragment.PictureListFragment;
import ws1415.ps1415.model.ContextMenu;
import ws1415.ps1415.model.PictureVisibility;
import ws1415.ps1415.model.Privilege;
import ws1415.ps1415.model.SerializableGalleryMetaData;
import ws1415.ps1415.model.SerializablePictureFilter;
import ws1415.ps1415.model.SerializablePictureMetaData;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

public class ListPicturesActivity extends Activity implements PictureListFragment.OnPictureClickListener {
    public static final String EXTRA_MAIL = ListPicturesActivity.class.getSimpleName() + ".Mail";
    public static final String EXTRA_CONTAINER_CLASS = ListPicturesActivity.class.getSimpleName() + ".ContainerClass";
    public static final String EXTRA_CONTAINER_ID = ListPicturesActivity.class.getSimpleName() + ".ContainerId";
    public static final String EXTRA_TITLE = ListPicturesActivity.class.getSimpleName() + ".Title";

    private static final String MEMBER_MAIL = ListPicturesActivity.class.getSimpleName() + ".Mail";
    private static final String MEMBER_CONTAINER_CLASS = ListPicturesActivity.class.getSimpleName() + ".ContainerClass";
    private static final String MEMBER_CONTAINER_ID = ListPicturesActivity.class.getSimpleName() + ".ContainerId";
    private static final String MEMBER_TITLE = ListPicturesActivity.class.getSimpleName() + ".Title";
    private static final String MEMBER_ADMIN = ListPicturesActivity.class.getSimpleName() + ".IsAdmin";
    private static final String MEMBER_ADD_GALLERY = ListPicturesActivity.class.getSimpleName() + ".CanAddGallery";
    private static final String MEMBER_EDIT_GALLERY = ListPicturesActivity.class.getSimpleName() + ".CanEditGallery";
    private static final String MEMBER_REMOVE_GALLERY = ListPicturesActivity.class.getSimpleName() + ".CanRemoveGallery";
    private static final String MEMBER_ADD_PICTURES = ListPicturesActivity.class.getSimpleName() + ".CanAddPictures";
    private static final String MEMBER_REMOVE_PICTURES = ListPicturesActivity.class.getSimpleName() + ".CanRemovePictures";
    private static final String MEMBER_MIN_VISIBILITY = ListPicturesActivity.class.getSimpleName() + ".MinVisibility";
    private static final String MEMBER_GALLERIES = ListPicturesActivity.class.getSimpleName() + ".Galleries";
    private static final String MEMBER_GALLERY_POSITION = ListPicturesActivity.class.getSimpleName() + ".GalleryPosition";
    private static final String MEMBER_PICTURE_FILTER = ListPicturesActivity.class.getSimpleName() + ".PictureFilter";
    private static final String MEMBER_PICTURES = ListPicturesActivity.class.getSimpleName() + ".Pictures";

    private static final int UPLOAD_PICTURE_REQUEST_CODE = 1;
    private static final int EDIT_PICTURE_REQUEST_CODE = 2;

    /**
     * Bestimmt die Anzahl Bilder, die pro Aufruf an den Server herunter geladen werden.
     */
    private static final int PICTURES_PER_REQUEST = 15;

    private String userMail;
    private Long containerId;
    private String containerClass;
    private String title;

    private boolean isAdmin;
    private boolean canAddGallery;
    private boolean canEditGallery;
    private boolean canRemoveGallery;
    private boolean canAddPicturesToGallery;
    private boolean canRemovePicturesFromGallery;
    private PictureVisibility minPictureVisibility;

    private GalleryAdapter galleryAdapter;
    private PictureMetaDataAdapter pictureAdapter;

    private PictureListFragment pictureFragment;
    private Spinner galleries;
    private MenuItem menuItemAddPicture;

    /**
     * true, wenn die Activity gerade aus einem Bundle wiederhergestellt wurde. In diesem Fall werden
     * im ItemSelectedListener des Gallery-Spinner die Bilder nicht vom Server abgerufen, da sie aus
     * dem Bundle wiederhergestellt wurden.
     */
    private boolean firstFetchAfterRestoring;
    private long restoredGalleryPosition = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pictures);

        // "Zurück"-Button in der Actionbar anzeigen
        ActionBar mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setHomeButtonEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        pictureFragment = (PictureListFragment) getFragmentManager().findFragmentById(R.id.picturesFragment);
        galleries = (Spinner) findViewById(R.id.galleries);

        if (savedInstanceState != null) {
            title = savedInstanceState.getString(MEMBER_TITLE);

            isAdmin = savedInstanceState.getBoolean(MEMBER_ADMIN);
            canAddGallery = savedInstanceState.getBoolean(MEMBER_ADD_GALLERY);
            canEditGallery = savedInstanceState.getBoolean(MEMBER_EDIT_GALLERY);
            canRemoveGallery = savedInstanceState.getBoolean(MEMBER_REMOVE_GALLERY);
            canAddPicturesToGallery = savedInstanceState.getBoolean(MEMBER_ADD_PICTURES);
            canRemovePicturesFromGallery = savedInstanceState.getBoolean(MEMBER_REMOVE_PICTURES);
            minPictureVisibility = PictureVisibility.valueOf(savedInstanceState.getString(MEMBER_MIN_VISIBILITY));

            // GalleryAdapter wiederherstellen
            List<GalleryMetaData> galleryMetaDatas = new LinkedList<>();
            for (SerializableGalleryMetaData g : (LinkedList<SerializableGalleryMetaData>) savedInstanceState.getSerializable(MEMBER_GALLERIES)) {
                galleryMetaDatas.add(g.getGalleryMetaData());
            }
            galleryAdapter = new GalleryAdapter(galleryMetaDatas);
            galleries.setAdapter(galleryAdapter);
            firstFetchAfterRestoring = true;
            restoredGalleryPosition = savedInstanceState.getLong(MEMBER_GALLERY_POSITION);
            if (restoredGalleryPosition == 0) {
                // Bei 0 als Position wird der ItemSelectedListener nur einmal aufgerufen
                // Es muss daher nur die erste Auswahl des Items ignoriert werden, da es sich
                // dabei schon um den Eintrag handelt, der weitere selektiert bleibt
                restoredGalleryPosition = -2;
            }

            // PictureAdapter wiederherstellen
            PictureFilter filter = ((SerializablePictureFilter) savedInstanceState.getSerializable(MEMBER_PICTURE_FILTER)).getPictureFilter();
            List<PictureMetaData> pictureMetaDatas = new LinkedList<>();
            for (SerializablePictureMetaData p : (LinkedList<SerializablePictureMetaData>) savedInstanceState.getSerializable(MEMBER_PICTURES)) {
                pictureMetaDatas.add(p.getPictureMetaData());
            }
            pictureAdapter = new PictureMetaDataAdapter(this, pictureMetaDatas, filter);
            pictureFragment.setAdapter(pictureAdapter);

            if (savedInstanceState.containsKey(MEMBER_MAIL)) {
                userMail = savedInstanceState.getString(MEMBER_MAIL);
            }
            if (savedInstanceState.containsKey(MEMBER_CONTAINER_CLASS) && savedInstanceState.containsKey(MEMBER_CONTAINER_ID)) {
                containerId = savedInstanceState.getLong(MEMBER_CONTAINER_ID);
                containerClass = savedInstanceState.getString(MEMBER_CONTAINER_CLASS);
            } else {
                getGalleriesForUser();
            }
            setUpButtons();
        } else {
            RoleController.isAdmin(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                    isAdmin = aBoolean;
                }
            }, ServiceProvider.getEmail());

            title = getIntent().getStringExtra(EXTRA_TITLE);
            if (getIntent().hasExtra(EXTRA_MAIL)) {
                userMail = getIntent().getStringExtra(EXTRA_MAIL);
            } else if (getIntent().hasExtra(EXTRA_CONTAINER_CLASS) && getIntent().hasExtra(EXTRA_CONTAINER_ID)) {
                containerId = getIntent().getLongExtra(EXTRA_CONTAINER_ID, -1);
                containerClass = getIntent().getStringExtra(EXTRA_CONTAINER_CLASS);
            }

            startLoading();
            if (userMail != null) {
                getGalleriesForUser();
            } else if (containerClass != null && containerId != null) {
                galleryAdapter = new GalleryAdapter(ListPicturesActivity.this, containerClass, containerId, false);
                setUpGallerySpinner();
            }
        }

        setTitle(title);
        galleries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == restoredGalleryPosition) {
                    restoredGalleryPosition = -2;
                    return;
                }
                if (!firstFetchAfterRestoring) {
                    PictureFilter filter = new PictureFilter();
                    if (id == GalleryAdapter.EMPTY_ITEM_GALLERY_ID) {
                        filter.setUserId(userMail);
                    } else {
                        filter.setGalleryId(id);
                    }
                    filter.setLimit(PICTURES_PER_REQUEST);
                    pictureAdapter = new PictureMetaDataAdapter(ListPicturesActivity.this, filter);
                    pictureFragment.setAdapter(pictureAdapter);
                    setUpButtons();
                } else {
                    firstFetchAfterRestoring = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Sollte nicht vorkommen
            }
        });
    }

    private void getGalleriesForUser() {
        GalleryController.getGalleryContainerForMail(new ExtendedTaskDelegateAdapter<Void, UserGalleryContainer>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGalleryContainer userGalleryContainer) {
                containerId = userGalleryContainer.getId();
                containerClass = UserGalleryContainer.class.getSimpleName();
                galleryAdapter = new GalleryAdapter(ListPicturesActivity.this, containerClass, containerId, true);
                setUpGallerySpinner();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(ListPicturesActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }, userMail);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(MEMBER_TITLE, title);
        if (containerClass != null && containerId != null) {
            outState.putString(MEMBER_CONTAINER_CLASS, containerClass);
            outState.putLong(MEMBER_CONTAINER_ID, containerId);
        }
        outState.putString(MEMBER_MAIL, userMail);

        outState.putBoolean(MEMBER_ADMIN, isAdmin);
        outState.putBoolean(MEMBER_ADD_GALLERY, canAddGallery);
        outState.putBoolean(MEMBER_EDIT_GALLERY, canEditGallery);
        outState.putBoolean(MEMBER_REMOVE_GALLERY, canRemoveGallery);
        outState.putBoolean(MEMBER_ADD_PICTURES, canAddPicturesToGallery);
        outState.putBoolean(MEMBER_REMOVE_PICTURES, canRemovePicturesFromGallery);
        if (minPictureVisibility != null) {
            outState.putString(MEMBER_MIN_VISIBILITY, minPictureVisibility.name());
        }

        if (galleryAdapter != null) {
            LinkedList<SerializableGalleryMetaData> serializableGalleries = new LinkedList<>();
            for (GalleryMetaData g : galleryAdapter.getGalleries()) {
                serializableGalleries.add(new SerializableGalleryMetaData(g));
            }
            outState.putSerializable(MEMBER_GALLERIES, serializableGalleries);
            outState.putLong(MEMBER_GALLERY_POSITION, galleries.getSelectedItemPosition());
        }
        if (pictureAdapter != null) {
            outState.putSerializable(MEMBER_PICTURE_FILTER, new SerializablePictureFilter(pictureAdapter.getFilter()));
            LinkedList<SerializablePictureMetaData> serializablePictures = new LinkedList<>();
            for (PictureMetaData p : pictureAdapter.getPictures()) {
                serializablePictures.add(new SerializablePictureMetaData(p));
            }
            outState.putSerializable(MEMBER_PICTURES, serializablePictures);
        }
    }

    private void setUpGallerySpinner() {
        GalleryController.getGalleryContainer(new ExtendedTaskDelegateAdapter<Void, GalleryContainerData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, GalleryContainerData containerData) {
                if (containerData.getPrivileges() == null) {
                    canAddGallery = false;
                    canEditGallery = false;
                    canRemoveGallery = false;
                    canAddPicturesToGallery = false;
                    canRemovePicturesFromGallery = false;
                } else {
                    canAddGallery = containerData.getPrivileges().contains(Privilege.ADD_GALLERY.name());
                    canEditGallery = containerData.getPrivileges().contains(Privilege.EDIT_GALLERY.name());
                    canRemoveGallery = containerData.getPrivileges().contains(Privilege.REMOVE_GALLERY.name());
                    canAddPicturesToGallery = containerData.getPrivileges().contains(Privilege.ADD_PICTURE.name());
                    canRemovePicturesFromGallery = containerData.getPrivileges().contains(Privilege.REMOVE_PICTURES.name());
                }
                minPictureVisibility = PictureVisibility.valueOf(containerData.getMinPictureVisbility());
                setUpButtons();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(ListPicturesActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }, containerClass, containerId);

        galleryAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                finishLoading();
            }
        });
        galleries.setAdapter(galleryAdapter);
    }

    private void setUpButtons() {
        if (canAddGallery) {
            findViewById(R.id.addGallery).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.addGallery).setVisibility(View.GONE);
        }
        if (galleries.getSelectedItemId() == GalleryAdapter.EMPTY_ITEM_GALLERY_ID) {
            findViewById(R.id.removeGallery).setVisibility(View.GONE);
            findViewById(R.id.editGallery).setVisibility(View.GONE);
        } else {
            if (canEditGallery) {
                findViewById(R.id.editGallery).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.editGallery).setVisibility(View.GONE);
            }
            if (canRemoveGallery) {
                findViewById(R.id.removeGallery).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.removeGallery).setVisibility(View.GONE);
            }
        }
        if (menuItemAddPicture != null) {
            menuItemAddPicture.setVisible(canAddPicturesToGallery && galleryAdapter != null && galleryAdapter.getCount() > 0);
        }
        // Layout neu zeichnen, damit die Buttons richtig angezeigt werden
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.data).requestLayout();
            }
        });
    }

    private void startLoading() {
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
    }

    private void finishLoading() {
        findViewById(R.id.loading).setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_pictures, menu);
        menuItemAddPicture = menu.findItem(R.id.action_upload_picture);
        if (menuItemAddPicture != null) {
            menuItemAddPicture.setVisible(canAddPicturesToGallery && galleryAdapter != null && galleryAdapter.getCount() > 0);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_upload_picture) {
            Intent intent = new Intent(this, EditPictureActivity.class);
            if (galleries.getSelectedItemId() != GalleryAdapter.EMPTY_ITEM_GALLERY_ID) {
                intent.putExtra(EditPictureActivity.EXTRA_GALLERY_ID, galleries.getSelectedItemId());
            }
            intent.putExtra(EditPictureActivity.EXTRA_MIN_PICTURE_VISBILITY, minPictureVisibility.name());
            startActivityForResult(intent, UPLOAD_PICTURE_REQUEST_CODE);
            return true;
        } else if (id == R.id.action_refresh_pictures) {
            if (pictureAdapter != null) {
                pictureAdapter.refresh();
            }
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UPLOAD_PICTURE_REQUEST_CODE:
                if (pictureAdapter != null) {
                    pictureAdapter.refresh();
                }
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
            Intent intent = new Intent(ListPicturesActivity.this, ShowPictureActivity.class);
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
            List<ContextMenu.ContextMenuItem> menuItems = new LinkedList<>();
            // Anzeigen
            menuItems.add(new ContextMenu.ContextMenuItem() {
                @Override
                public String getText() {
                    return getResources().getString(R.string.action_show_picture);
                }
                @Override
                public void onClick() {
                    Intent intent = new Intent(ListPicturesActivity.this, ShowPictureActivity.class);
                    intent.putExtra(ShowPictureActivity.EXTRA_PICTURE_ID, picture.getId());
                    intent.putExtra(ShowPictureActivity.EXTRA_POSITION, position);
                    startActivityForResult(intent, EDIT_PICTURE_REQUEST_CODE);
                }
            });
            // Bearbeiten
            if (isAdmin || picture.getUploader().equals(ServiceProvider.getEmail())) {
                menuItems.add(new ContextMenu.ContextMenuItem() {
                    @Override
                    public String getText() {
                        return getResources().getString(R.string.action_edit_picture);
                    }
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(ListPicturesActivity.this, EditPictureActivity.class);
                        intent.putExtra(EditPictureActivity.EXTRA_PICTURE_ID, picture.getId());
                        intent.putExtra(EditPictureActivity.EXTRA_POSITION, position);
                        intent.putExtra(EditPictureActivity.EXTRA_MIN_PICTURE_VISBILITY, minPictureVisibility.name());
                        startActivityForResult(intent, EDIT_PICTURE_REQUEST_CODE);
                    }
                });
            }
            // Löschen
            if (isAdmin || picture.getUploader().equals(ServiceProvider.getEmail())) {
                menuItems.add(new ContextMenu.ContextMenuItem() {
                    @Override
                    public String getText() {
                        return getResources().getString(R.string.action_delete_picture);
                    }

                    @Override
                    public void onClick() {
                        deletePicture(picture);
                    }
                });
            }

            // Aus der Galerie entfernen
            if (galleries.getSelectedItemId() != GalleryAdapter.EMPTY_ITEM_GALLERY_ID && canRemovePicturesFromGallery) {
                menuItems.add(new ContextMenu.ContextMenuItem() {
                    @Override
                    public String getText() {
                        return getResources().getString(R.string.action_remove_from_gallery);
                    }
                    @Override
                    public void onClick() {
                        removePictureFromGallery(picture);
                    }
                });
            }

            ContextMenu menu = new ContextMenu(menuItems);
            builder.setTitle(picture.getTitle())
                    .setItems(menu.getItemStrings(), menu.getClickListener());
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
                                Toast.makeText(ListPicturesActivity.this, message, Toast.LENGTH_LONG).show();
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
                        startLoading();
                        GalleryController.deletePicture(new ExtendedTaskDelegateAdapter<Void, Void>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                pictureAdapter.removePicture(picture);
                                finishLoading();
                            }
                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(ListPicturesActivity.this, message, Toast.LENGTH_LONG).show();
                                finishLoading();
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
        input.setMaxLines(1);
        input.setSingleLine(true);
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
                    Toast.makeText(ListPicturesActivity.this, R.string.no_title_submitted, Toast.LENGTH_SHORT).show();
                } else {
                    Gallery gallery = new Gallery();
                    gallery.setTitle(title);
                    gallery.setContainerClass(containerClass);
                    gallery.setContainerId(containerId);
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
                                Toast.makeText(ListPicturesActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        }, gallery);
                        dialog.dismiss();
                    } catch(IllegalArgumentException ex) {
                        Toast.makeText(ListPicturesActivity.this, R.string.no_title_submitted, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(ListPicturesActivity.this, message, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(ListPicturesActivity.this, R.string.no_title_submitted, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(ListPicturesActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        }, editedGallery);
                        dialog.dismiss();
                    } catch(IllegalArgumentException ex) {
                        Toast.makeText(ListPicturesActivity.this, R.string.no_title_submitted, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
