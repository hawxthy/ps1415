package ws1415.ps1415.task;

import android.os.AsyncTask;

import ws1415.ps1415.ServiceProvider;

/**
 * Created by Bernd Eissing on 03.02.2015.
 */
public class JoinUserGroupTask extends AsyncTask<String, Void, Void>{

    protected Void doInBackground(String... params){
        ServiceProvider.getService().skatenightServerEndpoint().joinUserGroup(params[0]).execute();
        return null;
    }
}
