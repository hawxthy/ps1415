package ws1415.common.task;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Klasse, welche mit SkatenightBackend kommunizert um einen neuen Veranstalter auf dem Server zu erstellen.
 *
 * Created by Martin on 06.01.2015.
 */
public class AddHostTask extends ExtendedTask<String, Void, Void>{

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public AddHostTask(ExtendedTaskDelegate<Void, Void> delegate) {
        super(delegate);
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

            ServiceProvider.getService().hostEndpoint().addHost(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
