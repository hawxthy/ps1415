package ws1415.ps1415.task;

import android.os.AsyncTask;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.fragment.AllUsergroupsFragment;

/**
 * Created by Bernd Eissing on 30.01.2015.
 */
public class AddUserGroupTask extends AsyncTask<String, Void, Void> {
    AllUsergroupsFragment auf;

    public AddUserGroupTask(AllUsergroupsFragment auf){
        this.auf = auf;
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
        auf.refresh();
    }
}
