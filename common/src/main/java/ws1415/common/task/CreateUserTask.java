package ws1415.common.task;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Created by Martin on 02.05.2015.
 */
public class CreateUserTask extends ExtendedTask<String, Void, Void> {

    public CreateUserTask(ExtendedTaskDelegate<Void, Void> delegate) {
        super(delegate);
    }

    @Override
    protected Void doInBackground(String... params) {
        String email = params[0];
        try {
            ServiceProvider.getService().userEndpoint().createUser(email).execute();
        } catch (IOException e){
            e.printStackTrace();
            publishError("Benutzer konnte nicht erstellt werden");
        }
        return null;
    }
}
