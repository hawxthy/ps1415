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

        // Den Action Button holen
        addUserGroupButton = mActivity.getMenuItem();

        // Initialisiere die ListView
        listView = ((AllUsergroupsFragment) mActivity.getAdapter().getItem(0)).getListView();
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
        int positionMyGroup = -1;
        for (int i = 0; i < groupList.getAdapter().getCount(); i++) {
            if (((UserGroup) groupList.getAdapter().getItem(i)).getName().equals(userGroup)) {
                found = true;
                positionMyGroup = i;
            }
        }
        assertEquals("Gruppe wurde nicht angelegt", true, found);


        // Gruppe beitreten
        found = false;
        int position = -1;
        for (int i = 0; i < groupList.getAdapter().getCount(); i++) {
            if (!((UserGroup) groupList.getAdapter().getItem(i)).getMembers().contains(ServiceProvider.getEmail())) {
                position = i;
                found = true;
            }
        }
        assertTrue("Es existiert keine Gruppe, bei der man nicht der Ersteller/Member ist", found);

        final int pos = position;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupList.performItemClick(groupList.getAdapter().getView(pos, null, groupList), pos, groupList.getAdapter().getItemId(pos));
            }
        });
        Thread.sleep(3000);

        final Button okButton = ((AllUsergroupsFragment) mActivity.getAdapter().getItem(0)).getLastDialog().getButton(AlertDialog.BUTTON_POSITIVE);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                okButton.performClick();
            }
        });
        Thread.sleep(3000);

        found = false;
        for (int j = 0; j < ((UserGroup) groupList.getAdapter().getItem(position)).getMembers().size(); j++) {
            if (((UserGroup) groupList.getAdapter().getItem(position)).getMembers().get(j).equals(ServiceProvider.getEmail())) {
                found = true;
            }
        }
        assertTrue("Member wurde nicht gefunden", found);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupList.performItemClick(groupList.getAdapter().getView(pos, null, groupList), pos, groupList.getAdapter().getItemId(pos));
            }
        });
        Thread.sleep(3000);

        final Button okButtonLeave = ((AllUsergroupsFragment) mActivity.getAdapter().getItem(0)).getLastDialog().getButton(AlertDialog.BUTTON_POSITIVE);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                okButtonLeave.performClick();
            }
        });
        Thread.sleep(3000);

        found = true;
        if (!((UserGroup) groupList.getAdapter().getItem(position)).getMembers().contains(ServiceProvider.getEmail())) {
            found = false;
        }
        assertFalse("Benutzer hat die Gruppe nicht verlassen", found);


        // Da longClick nicht zu simmulieren ist, müssen wir hier den
        // Task direkt aufrufen
        new DeleteUserGroupTask(mFragment).execute(((UserGroup) groupList.getAdapter().getItem(positionMyGroup))).get();

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
