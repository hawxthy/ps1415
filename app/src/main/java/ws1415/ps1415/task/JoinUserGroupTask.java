package ws1415.ps1415.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.UsergroupActivity;
import ws1415.ps1415.fragment.AllUsergroupsFragment;

/**
 * Klasse welche mit dem SkatenightBackend kommunizert um die E-Mail eines Benutzers
 * in die Liste der Mitglieder von einer Gruppe einzutragen.
 *
 * Created by Bernd Eissing on 03.02.2015.
 */
public class JoinUserGroupTask extends AsyncTask<String, Void, Void>{
    private UsergroupActivity usergroupActivity;

    public JoinUserGroupTask(UsergroupActivity usergroupActivity){
        this.usergroupActivity = usergroupActivity;
    }

    /**
     * Ruft die joinUserGroup Methode auf dem Server auf.
     *
     * @param params
     * @return
     */
    @Override
    protected Void doInBackground(String... params){
        try{
            ServiceProvider.getService().skatenightServerEndpoint().joinUserGroup(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Ruft die refresh() Methode in der UsergroupActivity auf, welche die Listen aller Gruppen
     * und eigener Gruppen aktualisiert.
     *
     * @param voids
     */
    @Override
    protected void onPostExecute(Void voids){
        usergroupActivity.refresh();
    }
}
