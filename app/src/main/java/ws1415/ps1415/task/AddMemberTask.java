package ws1415.ps1415.task;

import android.os.AsyncTask;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;


public class AddMemberTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
        String email = params[0];

        try {
            if (email != null) {
                return ServiceProvider.getService().skatenightServerEndpoint().createMember(email).execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
