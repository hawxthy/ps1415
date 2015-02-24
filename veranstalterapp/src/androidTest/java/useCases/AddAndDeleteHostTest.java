package useCases;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Host;

import ws1415.veranstalterapp.Constants;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.dialog.AddHostDialog;
import ws1415.veranstalterapp.activity.PermissionManagementActivity;
import ws1415.veranstalterapp.task.DeleteHostTask;

/**
 * Testet das Hinzufügen und Löschen eines neuen Veranstalters.
 *
 * Created by Bernd Eissing & Martin Wrodarczyk on 06.01.2015.
 */
public class AddAndDeleteHostTest extends ActivityInstrumentationTestCase2<PermissionManagementActivity> {
    private PermissionManagementActivity mActivity;
    private ActionBar mActionBar;

    public AddAndDeleteHostTest(){
        super(PermissionManagementActivity.class);
    }

    /**
     * Loggt den Veranstalter ein.
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception{
        super.setUp();

        // Nutzer einloggen
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(getActivity(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(credential.getAllAccounts()[0].name);
        ServiceProvider.login(credential);

        // Touchmode ausschalten, damit auch die UI Elemente getestet werden können
        setActivityInitialTouchMode(false);

        // Die HoldTabsActivity wird gestartet
        mActivity = getActivity();
        // ActionBar der TabActivity holen
        mActionBar = mActivity.getActionBar();
    }

    /**
     * Testet ob alle View-Elemente nicht null sind.
     */
    @SmallTest
    public void testView(){
        assertNotNull(mActivity);

        // Initialisiere das View Element
        final ListView listView = (ListView) mActivity.findViewById(R.id.activtiy_permission_management_list_view);

        assertNotNull(listView);
    }

    /**
     * Testet ob alle View-Elemente auf dem Bildschirm sichtbar sind.
     *
     * @throws Exception
     */
    @SmallTest
    public void testViewVisible()throws Exception{
        // Initialisiere das View Element
        final ListView listView = (ListView) mActivity.findViewById(R.id.activtiy_permission_management_list_view);

        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), listView);
    }

    protected void tearDown()throws Exception{
        super.tearDown();
    }

    /**
     * Testet das Hinzufügen und Löschen eines neuen Veranstalters.
     *
     * @throws Exception
     */
    public void testUseCase() throws Exception{
        Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(AddHostDialog.class.getName(), null, false);
        final View addHost = mActivity.findViewById(R.id.action_add_host);
        assertNotNull(addHost);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addHost.performClick();
            }
        });

        AddHostDialog addHostDialog = (AddHostDialog) am.waitForActivityWithTimeout(20000);
        assertNotNull(addHostDialog);

        // Der Test Host
        final String email = "test@gmail.com";

        // Initialisiere die View Elemente aus dem AddHostDialog
        final EditText editTextEmail = (EditText) addHostDialog.findViewById(R.id.activity_add_host_hostName_edittext);
        final Button addButton = (Button) addHostDialog.findViewById(R.id.activity_add_host_dialog_apply);

        addHostDialog.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextEmail.setText(email);
                addButton.performClick();
            }
        });

        Thread.sleep(3000);
        final ListView hostList = (ListView) mActivity.findViewById(R.id.activtiy_permission_management_list_view);
        boolean found = false;
        int position = -1;
        for(int i = 0; i < hostList.getAdapter().getCount(); i++){
            if(((Host)(hostList.getAdapter().getItem(i))).getEmail().equals(email)){
                found = true;
                position= i;
            }
        }
        assertEquals(true, found);

        final int pos = position;
        addHostDialog.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hostList.performItemClick(hostList.getAdapter().getView(pos, null, hostList), pos, hostList.getAdapter().getItemId(pos));
            }
        });
        Thread.sleep(3000);

        final AlertDialog dialog = mActivity.getLastDialog();
        new DeleteHostTask(mActivity).execute(mActivity.getHostList().get(pos).getEmail());
        dialog.cancel();
        Thread.sleep(3000);
        found = false;

        for(int i = 0; i < hostList.getAdapter().getCount(); i++){
            if(((Host)(hostList.getAdapter().getItem(i))).getEmail().equals(email)){
                found = true;
            }
        }

        assertEquals(false, found);
    }
}
