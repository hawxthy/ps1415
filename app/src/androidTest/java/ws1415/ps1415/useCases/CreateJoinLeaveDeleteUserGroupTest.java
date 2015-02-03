package ws1415.ps1415.useCases;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.UserGroup;

import ws1415.ps1415.Constants;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.AddUserGroupActivity;
import ws1415.ps1415.activity.UsergroupActivity;
import ws1415.ps1415.fragment.AllUsergroupsFragment;
import ws1415.ps1415.task.DeleteUserGroupTask;

/**
 * Created by Bernd Eissing on 03.02.2015.
 */
public class CreateJoinLeaveDeleteUserGroupTest extends ActivityInstrumentationTestCase2<UsergroupActivity> {
    private UsergroupActivity mActivity;
    private AllUsergroupsFragment mFragment;
    private ActionBar mActionBar;
    private ListView listView;

    private MenuItem addUserGroupButton;

    // Wird zum Swipen nach link oder Rechts verwendet
    public enum Direction {
        Left, Right;
    }

    public CreateJoinLeaveDeleteUserGroupTest() {
        super(UsergroupActivity.class);
    }

    /**
     * Loggt den User ein und wechselt auf das UserGroupActivity mit dem AllUsergroupsFragment.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Nutzer verbinden, Nicht einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        //touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        // Die UsergroupActivity wird gestartet
        mActivity = getActivity();

        // Das Fragment wird initialisiert
        mFragment = (AllUsergroupsFragment) mActivity.getAdapter().getItem(0);

        // ActionBar der UsergroupActivity holen
        mActionBar = mActivity.getActionBar();

        // Initialisiere das MenuItem
        addUserGroupButton = (MenuItem) mActionBar.getCustomView().findViewById(R.id.action_add_user_group);

        // Initialisiere die ListView
        listView = ((AllUsergroupsFragment) mActivity.getAdapter().getItem(0)).getListView();
        mActivity.onOptionsItemSelected(addUserGroupButton);
    }

    /**
     * Prüft, ob diese Views wirklich in der Activity existieren.
     */
    @SmallTest
    public void testViews() {
        assertNotNull(mActionBar);
        assertNotNull(addUserGroupButton);
    }

    /**
     * Testet den UserCase, also wird eine UserGroup erstellt, dieser dann beigetreten
     * und nach dem beitreten diese dann wieder verlassen und die UserGroup dann
     * gelöscht
     *
     * @thorws Exception
     */
    public void testUserCase() throws Exception {
        Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(AddUserGroupActivity.class.getName(), null, false);
        // Initialisiere das MenuItem
        final MenuItem addUserGroupButton = (MenuItem) mActionBar.getCustomView().findViewById(R.id.action_add_user_group);


        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.onOptionsItemSelected(addUserGroupButton);
            }
        });
        Thread.sleep(3000);

        AddUserGroupActivity addUserGroupActivity = (AddUserGroupActivity) am.waitForActivityWithTimeout(20000);
        assertNotNull(addUserGroupActivity);

        // Die Test UserGroup
        final String userGroup = "TestGroup";

        // Initialisiere die View Elemente aus der AddUserGroupActivity
        final EditText editTextName = (EditText) addUserGroupActivity.findViewById(R.id.activity_add_user_group_edittext);
        final Button addButton = (Button) addUserGroupActivity.findViewById(R.id.activity_add_user_group_apply);

        addUserGroupActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextName.setText(userGroup);
                addButton.performClick();
            }
        });
        Thread.sleep(5000);

        final ListView groupList = (ListView) mActivity.findViewById(R.id.fragment_show_user_groups_list_view);
        boolean found = false;
        int position = -1;
        for (int i = 0; i < groupList.getAdapter().getCount(); i++) {
            if (((UserGroup) groupList.getAdapter().getItem(i)).getName().equals(userGroup)) {
                found = true;
                position = i;
            }
        }
        assertEquals("Gruppe wurde nicht angelegt", true, found);

        // Gruppe beitreten
        final int pos = position;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupList.performItemClick(groupList.getAdapter().getView(pos, null, groupList), pos, groupList.getAdapter().getItemId(pos));
            }
        });
        Thread.sleep(3000);

        final AlertDialog dialog = mFragment.getLastDialog();
        final Button posButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                posButton.performClick();
            }
        });
        Thread.sleep(3000);

        int posGruppe = 0;
        // Nach der Gruppe suchen und prüfen ob der Name bei den Membern der
        // Gruppe enthalten ist.
        for (int i = 0; i < groupList.getAdapter().getCount(); i++) {
            if (groupList.getAdapter().getItem(i).equals(userGroup)) {
                posGruppe = i;
            }
        }
        found = false;
        for (int j = 0; j < ((UserGroup) groupList.getAdapter().getItem(posGruppe)).getMembers().size(); j++) {
            if (((UserGroup) groupList.getAdapter().getItem(posGruppe)).getMembers().get(j).equals(ServiceProvider.getEmail())) {
                found = true;
            }
        }
        assertTrue("Member wurde nicht gefunden", found);

        // Da longClick nicht zu simmulieren ist, müssen wir hier den
        // Task direkt aufrufen
        new DeleteUserGroupTask(mFragment).execute(((UserGroup) groupList.getAdapter().getItem(posGruppe))).get();

        // Nach der Gruppe suchen und prüfen ob diese noch existiert
        found = false;
        for (int i = 0; i < groupList.getAdapter().getCount(); i++) {
            if (groupList.getAdapter().getItem(i).equals(userGroup)) {
                found = true;
            }
        }
        assertFalse("UserGroup wurde nicht entfernt", found);
    }
}
