package ws1415.ps1415;

import android.os.AsyncTask;

import com.appspot.skatenight_ms.skatenightAPI.model.Member;

import java.io.IOException;

/**
 * Created by Tristan Rust on 21.10.2014.
 * Dient zum Abrufen der Nutzerinformationen.
 *
 */
public class QueryMemberTask extends AsyncTask<ShowRouteActivity, Void, Member> {
     private ShowRouteActivity view;

    /**
     * Ruft das aktuelle Member-Objekt vom Server ab.
     * @param params Die zu befüllende Views
     * @return Das Member-Objekt
     */
    @Override
    protected Member doInBackground(ShowRouteActivity... params) {
        view = params[0];

        try {
            return ServiceProvider.getService().skatenightServerEndpoint().getMember().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Member m) {
        view.setMemberInformation(m);
    }
}
