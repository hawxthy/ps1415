package ws1415.common.task;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Task zum erstellen und speichern einer neuen Gruppe.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 30.01.2015.
 */
public class AddUserGroupTask extends ExtendedTask<String, Void, Void> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public AddUserGroupTask(ExtendedTaskDelegate<Void, Void> delegate) {
        super(delegate);
    }

    /**
     * Erstellt eine neue Gruppe auf dem Server
     *
     * @param params E-Mail vom Veranstalter, der erstellt wird
     * @return
     */
    @Override
    protected Void doInBackground(String... params) {
        try{
            ServiceProvider.getService().groupEndpoint().createUserGroup(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

}
