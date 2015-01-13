package ws1415.veranstalterapp.task;

import android.os.AsyncTask;

import java.io.IOException;

import ws1415.veranstalterapp.ServiceProvider;
import ws1415.veranstalterapp.activity.PermissionManagementActivity;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um auf den Server zuzugreifen.
 *
 * Created by Martin on 06.01.2015.
 */
public class DeleteHostTask extends AsyncTask<String, Void, Void>{
    private PermissionManagementActivity pma;

    public DeleteHostTask(PermissionManagementActivity pma){
        this.pma = pma;
    }

    /**
     * Löscht den Veranstalter mit der übergebenen E-Mail-Adresse vom Server.
     *
     * @param strings E-Mail
     * @return NULL
     */
    @Override
    protected Void doInBackground(String... strings) {
        try{
            ServiceProvider.getService().skatenightServerEndpoint().removeHost(strings[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Aktualisiert die Liste der Veranstalter.
     */
    @Override
    protected void onPostExecute(Void result){
        pma.refresh();
    }
}
