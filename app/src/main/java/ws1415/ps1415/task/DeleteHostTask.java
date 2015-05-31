package ws1415.ps1415.task;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um einen Veranstalter vom Server zu löschen.
 *
 * Created by Martin on 06.01.2015.
 */
public class DeleteHostTask extends ExtendedTask<String, Void, Void> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public DeleteHostTask(ExtendedTaskDelegate<Void, Void> delegate) {
        super(delegate);
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
            ServiceProvider.getService().hostEndpoint().removeHost(strings[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
