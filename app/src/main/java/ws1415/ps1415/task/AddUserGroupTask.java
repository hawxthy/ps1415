package ws1415.ps1415.task;

import android.os.AsyncTask;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.UsergroupActivity;
import ws1415.ps1415.fragment.AllUsergroupsFragment;

/**
 * Created by Bernd Eissing, Martin Wrodarczyk on 30.01.2015.
 */
public class AddUserGroupTask extends AsyncTask<String, Void, Void> {
    private UsergroupActivity usergroupActivity;

    public AddUserGroupTask(UsergroupActivity usergroupActivity){
        this.usergroupActivity = usergroupActivity;
    }
    /**
     * Erstellt einen neuen Veranstalter auf dem Server.
     *
     * @param params E-Mail vom Veranstalter, der erstellt wird
     * @return
     */
    @Override
    protected Void doInBackground(String... params) {
        try{
            ServiceProvider.getService().skatenightServerEndpoint().createUserGroup(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Aktualisiert die Liste mit dem neuen Host.
     */
    @Override
    protected void onPostExecute(Void result) {
        usergroupActivity.refresh();
    }
}
