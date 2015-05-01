package ws1415.common.task;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Klasse welche mit SkatenightBackend kommunizert um eine Gruppe zu löschen.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 30.01.2015.
 */
public class DeleteUserGroupTask extends ExtendedTask<UserGroup, Void, Boolean> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public DeleteUserGroupTask(ExtendedTaskDelegate<Void, Boolean> delegate) {
        super(delegate);
    }

    /**
     * Löscht die UserGroup vom Server.
     *
     * @param params Die UserGroup die gelöscht werden soll
     */
    @Override
    protected Boolean doInBackground(UserGroup... params) {
        try {
            ServiceProvider.getService().groupEndpoint().deleteUserGroup(
                    params[0].getName()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
