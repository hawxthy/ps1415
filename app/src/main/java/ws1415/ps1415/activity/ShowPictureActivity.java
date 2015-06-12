package ws1415.ps1415.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import ws1415.ps1415.R;
import ws1415.ps1415.fragment.PictureFragment;

public class ShowPictureActivity extends Activity {
    public static final String EXTRA_PICTURE_ID = ShowPictureActivity.class.getName() + ".PictureId";
    /**
     * Die Position des Bildes im Adapter. Falls die Activity Änderungen an dem Bild vornimmt, wird
     * dieses Extra wieder an die aufrufende Activity zurück gegeben. Dort kann dann ggf. das einzelne
     * Bild neu geladen werden.
     */
    public static final String EXTRA_POSITION = ShowPictureActivity.class.getName() + ".Position";

    private static final String MEMBER_PICTURE_ID = ShowPictureActivity.class.getName() + ".PictureId";

    private long pictureId;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);

        // "Zurück"-Button in der Actionbar anzeigen
        ActionBar mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setHomeButtonEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        position = getIntent().getIntExtra(EXTRA_POSITION, -1);
        if (savedInstanceState != null && savedInstanceState.containsKey(MEMBER_PICTURE_ID)) {
            pictureId = savedInstanceState.getLong(MEMBER_PICTURE_ID);
        } else if (getIntent().hasExtra(EXTRA_PICTURE_ID)) {
            pictureId = getIntent().getLongExtra(EXTRA_PICTURE_ID, -1);
        } else {
            throw new RuntimeException("intent has to have extra " + EXTRA_PICTURE_ID);
        }
        PictureFragment fragment = (PictureFragment) getFragmentManager().findFragmentById(R.id.pictureFragment);
        fragment.loadPicture(pictureId, position);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.getLong(MEMBER_PICTURE_ID, pictureId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
