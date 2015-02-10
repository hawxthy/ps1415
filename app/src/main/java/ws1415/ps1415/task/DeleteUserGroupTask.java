package ws1415.ps1415.task;

import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.UsergroupActivity;
import ws1415.ps1415.fragment.AllUsergroupsFragment;
import ws1415.ps1415.fragment.MyUsergroupsFragment;
import ws1415.ps1415.fragment.UsergroupsInterface;

/**
 * Created by Bernd Eissing, Martin Wrodarczyk on 30.01.2015.
 */
public class DeleteUserGroupTask extends AsyncTask<UserGroup, Void, Boolean> {
    private UsergroupActivity usergroupActivity;
    private UserGroup usergroup;

    public DeleteUserGroupTask(UsergroupActivity usergroupActivity) {
       this.usergroupActivity = usergroupActivity;
    }

    /**
     * Löscht die UserGroup vom Server
     *
     * @param params Die UserGroup die gelöscht werden soll
     */
    @Override
    protected Boolean doInBackground(UserGroup... params) {
        usergroup = params[0];
        try {
            ServiceProvider.getService().skatenightServerEndpoint().deleteUserGroup(
                    params[0].getName()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Löscht UserGroup aus der Liste.
     *
     * @param result true, bei erfolgreicher Löschung, false andernfalls
     */
    @Override
    protected void onPostExecute(Boolean result) {
        if (result == true) {
            AllUsergroupsFragment allUsergroupsFragment =
                    (AllUsergroupsFragment)usergroupActivity.getAdapter().getItem(0);
            allUsergroupsFragment.deleteUserGroupFromList(usergroup);
            MyUsergroupsFragment myUsergroupsFragment =
                    (MyUsergroupsFragment)usergroupActivity.getAdapter().getItem(1);
            myUsergroupsFragment.deleteUserGroupFromList(usergroup);
        }
    }
}
