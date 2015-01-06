package ws1415.veranstalterapp.task;

import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.Host;

import java.io.IOException;

import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.activity.PermissionManagementActivity;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um auf den Server zuzugreifen.
 *
 * Created by Martin on 06.01.2015.
 */
public class AddHostTask extends AsyncTask<Host, Void, Boolean>{
    PermissionManagementActivity pma;

    public AddHostTask(PermissionManagementActivity pma){
        this.pma = pma;
    }
    /**
     * Erstellt einen neuen Veranstalter auf dem Server.
     *
     * @param params Host, der erstellt wird
     * @return
     */
    @Override
    protected Boolean doInBackground(Host... params) {
        try{
            return ServiceProvider.getService().skatenightServerEndpoint().addHost(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Aktualisiert die Liste mit dem neuen Host.
     *
     * @param result true, bei erfolgreicher Löschung, false andernfalls
     */
    @Override
    protected void onPostExecute(Boolean result) {
        if (result == true) {
            pma.refresh();
        }
    }
}
