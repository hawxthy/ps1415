package ws1415.common.task;

import android.os.AsyncTask;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;


public class AddMemberTask extends ExtendedTask<String, Void, Void> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public AddMemberTask(ExtendedTaskDelegate<Void, Void> delegate) {
        super(delegate);
    }

    @Override
    protected Void doInBackground(String... params) {
        String email = params[0];

        try {
            if (email != null) {
                return ServiceProvider.getService().userEndpoint().createMember(email).execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
