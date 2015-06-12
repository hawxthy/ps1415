package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.ProfileActivity;
import ws1415.ps1415.adapter.UserListAdapter;
import ws1415.ps1415.controller.RoleController;
import ws1415.ps1415.model.GlobalRole;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.UniversalUtil;


public class PermissionManagementActivity extends BaseActivity {
    private ListView mListViewPermission;
    private UserListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Prüft ob der Benutzer eingeloggt ist
        UniversalUtil.checkLogin(this);

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_permission_management);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        mListViewPermission = (ListView) findViewById(R.id.permission_management_list_view);
        mAdapter = new UserListAdapter(new ArrayList<String>(), PermissionManagementActivity.this);
        mListViewPermission.setAdapter(mAdapter);

        mListViewPermission.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String userMail = mAdapter.getItem(i).getEmail();
                Intent profile_intent = new Intent(PermissionManagementActivity.this, ProfileActivity.class);
                profile_intent.putExtra(ProfileActivity.EXTRA_MAIL, userMail);
                startActivity(profile_intent);
            }
        });
        mListViewPermission.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                takeRightsDialog(i);
                return true;
            }
        });

        if (ServiceProvider.getEmail() != null) getAdmins();
    }

    /**
     * Ruft die Administratoren ab und setzt diese in die Liste.
     */
    private void getAdmins() {
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        RoleController.listGlobalAdmins(new ExtendedTaskDelegateAdapter<Void, List<String>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<String> adminList) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                if (mListViewPermission != null && mAdapter != null) {
                    mAdapter.swapData(adminList);
                }
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(PermissionManagementActivity.this, message, Toast.LENGTH_LONG).show();
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
        });
    }

    /**
     * Erstellt einen Dialog für das Entziehen von administrativen Rechten.
     *
     * @param position Position des Administrators in der Liste
     */
    private void takeRightsDialog(final int position) {
        final String userMail = mAdapter.getItem(position).getEmail();
        new AlertDialog.Builder(PermissionManagementActivity.this)
                .setTitle(mAdapter.getItem(position).getEmail())
                .setMessage(getString(R.string.sure_take_right_dialog))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        takeRights(position, userMail);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    /**
     * Entzieht einem Benutzer Rechte.
     *
     * @param position Position des Administrators in der Liste
     * @param userMail E-Mail Adresse des Administrators
     */
    private void takeRights(final int position, String userMail) {
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        RoleController.assignGlobalRole(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                if (aBoolean) {
                    UniversalUtil.showToast(PermissionManagementActivity.this, getString(R.string.took_rights));
                    mAdapter.removeUser(position);
                } else {
                    UniversalUtil.showToast(PermissionManagementActivity.this, getString(R.string.cant_find_user));
                }
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                UniversalUtil.showToast(PermissionManagementActivity.this, message);
            }
        }, userMail, GlobalRole.USER);
    }

    /**
     * Erstellt einen Dialog mit einer E-Mail Eingabe zum Hinzufügen von Administratoren.
     */
    private void addAdmin() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        edittext.setHint("E-Mail");
        alert.setTitle(R.string.action_add_admin);
        alert.setView(edittext);
        alert.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String email = edittext.getText().toString();
                setProgressBarIndeterminateVisibility(Boolean.TRUE);
                assignAdminRole(email);
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    /**
     * Weist einem Benutzer administrative Rechte zu.
     *
     * @param email E-Mail Adresse des Benutzers
     */
    private void assignAdminRole(String email) {
        RoleController.assignGlobalRole(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                if (aBoolean) {
                    UniversalUtil.showToast(PermissionManagementActivity.this, getString(R.string.assigned_rights));
                    getAdmins();
                } else {
                    UniversalUtil.showToast(PermissionManagementActivity.this, getString(R.string.cant_find_user));
                }
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                UniversalUtil.showToast(PermissionManagementActivity.this, message);
            }
        }, email, GlobalRole.ADMIN);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_permission_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh_admins:
                getAdmins();
                break;
            case R.id.action_add_admin:
                addAdmin();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
