package ws1415.ps1415.task;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.fragment.AllUsergroupsFragment;
import ws1415.ps1415.fragment.MyUsergroupsFragment;
import ws1415.ps1415.fragment.UsergroupsInterface;

/**
 * Created by Bernd Eissing on 03.02.2015.
 */
public class LeaveUserGroupTask extends AsyncTask<String, Void, Void> {
    private UsergroupsInterface fragment;

    public LeaveUserGroupTask(UsergroupsInterface fragment){
        this.fragment = fragment;
    }

    @Override
    protected Void doInBackground(String... params){
        try{
            ServiceProvider.getService().skatenightServerEndpoint().leaveUserGroup(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void voids){
        fragment.refresh();
    }
}
