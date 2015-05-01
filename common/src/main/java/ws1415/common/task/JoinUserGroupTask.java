package ws1415.common.task;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Klasse welche mit dem SkatenightBackend kommunizert um die E-Mail eines Benutzers
 * in die Liste der Mitglieder von einer Gruppe einzutragen.
 *
 * Created by Bernd Eissing on 03.02.2015.
 */
public class JoinUserGroupTask extends ExtendedTask<String, Void, Void>{

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public JoinUserGroupTask(ExtendedTaskDelegate<Void, Void> delegate) {
        super(delegate);
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
            ServiceProvider.getService().groupEndpoint().joinUserGroup(params[0]).execute();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
