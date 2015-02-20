package ws1415.ps1415.task;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.UsergroupActivity;
import ws1415.ps1415.fragment.AllUsergroupsFragment;
import ws1415.ps1415.fragment.MyUsergroupsFragment;

/**
 *  * Klasse welche mit dem SkatenightBackend kommunizert um die E-Mail eines Benutzers
 * aus der Liste der Mitglieder von einer Gruppe auszutragen.
 *
 * Created by Bernd Eissing on 03.02.2015.
 */
public class LeaveUserGroupTask extends AsyncTask<String, Void, Void> {
    private UsergroupActivity usergroupActivity;

    public LeaveUserGroupTask(UsergroupActivity usergroupActivity){
        this.usergroupActivity = usergroupActivity;
    }


    /**
     * Ruft die leaveUserGroup Methode auf dem Server auf.
     *
     * @param params
     * @return
     */
    @Override
    protected Void doInBackground(String... params){
        try{
            ServiceProvider.getService().skatenightServerEndpoint().leaveUserGroup(params[0]).execute();
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
