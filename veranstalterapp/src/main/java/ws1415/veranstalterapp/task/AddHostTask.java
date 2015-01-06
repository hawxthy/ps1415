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
public class AddHostTask extends AsyncTask<String, Void, Void>{
    PermissionManagementActivity pma;

    public AddHostTask(PermissionManagementActivity pma){
        this.pma = pma;
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
            ServiceProvider.getService().skatenightServerEndpoint().addHost(params[0]).execute();
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
        pma.refresh();
    }
}
