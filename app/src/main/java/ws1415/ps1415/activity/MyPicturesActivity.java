package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.skatenight.skatenightAPI.model.PictureFilter;
import com.skatenight.skatenightAPI.model.PictureMetaData;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.PictureMetaDataAdapter;
import ws1415.ps1415.controller.GalleryController;
import ws1415.ps1415.fragment.PictureListFragment;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

public class MyPicturesActivity extends BaseActivity implements PictureListFragment.OnPictureClickListener {
    private static final int UPLOAD_PICTURE_REQUEST_CODE = 1;

    /**
     * Bestimmt die Anzahl Bilder, die pro Aufruf an den Server herunter geladen werden.
     */
    private static final int PICTURES_PER_REQUEST = 15;

    private PictureListFragment pictureFragment;
    private PictureMetaDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pictures);

        PictureFilter filter = new PictureFilter();
        filter.setUserId(ServiceProvider.getEmail());
        filter.setLimit(PICTURES_PER_REQUEST);
        adapter = new PictureMetaDataAdapter(this, filter);
        pictureFragment = (PictureListFragment) getFragmentManager().findFragmentById(R.id.picturesFragment);
        pictureFragment.setAdapter(adapter);
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
            adapter.refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UPLOAD_PICTURE_REQUEST_CODE:
                adapter.refresh();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPictureClick(PictureMetaData picture) {
        // TODO R
    }

    @Override
    public boolean onPictureLongClick(final PictureMetaData picture) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(picture.getTitle())
                .setItems(R.array.picture_actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        switch (which) {
                            case 0:
                                // Anzeigen
                                // TODO R
                                break;
                            case 1:
                                // Bearbeiten
                                intent = new Intent(MyPicturesActivity.this, EditPictureActivity.class);
                                intent.putExtra(EditPictureActivity.EXTRA_PICTURE_ID, picture.getId());
                                startActivityForResult(intent, UPLOAD_PICTURE_REQUEST_CODE);
                                // TODO R: ggf. nicht alle Bilder neu Abrufen sondern nur die geänderten Informationen übernehmen
                                break;
                            case 2:
                                // Löschen
                                deletePicture(picture);
                                break;
                        }
                    }
                });
        builder.create().show();
        return true;
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
                                adapter.removePicture(picture);
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
}
