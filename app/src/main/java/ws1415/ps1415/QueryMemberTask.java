package ws1415.ps1415;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

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
            // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view);
            // String email = prefs.getString("accountName", null);

            SharedPreferences prefs = view.getSharedPreferences("skatenight.app", Context.MODE_PRIVATE);
            String email = prefs.getString("accountName", null);

            return ServiceProvider.getService().skatenightServerEndpoint().getMember(email).execute();
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
