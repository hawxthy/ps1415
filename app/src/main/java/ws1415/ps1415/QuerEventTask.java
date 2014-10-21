package ws1415.ps1415;

import android.os.AsyncTask;
import android.widget.TextView;

import com.appspot.myapplicationid.skatenightAPI.model.Event;

import java.io.IOException;

/**
 * Created by Richard on 21.10.2014.
 */
public class QuerEventTask extends AsyncTask<TextView, Void, Event> {
    private TextView view;

    @Override
    protected Event doInBackground(TextView... params) {
        view = params[0];
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().getEvent().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Event e) {
        if (e != null) {
            view.setText(e.getName());
        } else {
            view.setText("null");
        }
    }
}
