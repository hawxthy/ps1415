package ws1415.ps1415.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.Member;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.Activities.ShowRouteActivity;

/**
 * Created by Tristan Rust on 21.10.2014.
 * Dient zum Abrufen der Nutzerinformationen.
 *
 */
public class QueryMemberTask extends AsyncTask<ShowRouteActivity, Void, Member> {
     private ShowRouteActivity view;

    /**
     * Ruft das aktuelle Member-Objekt vom Server ab.
     * @param params Die zu befüllende Activity
     * @return Das Member-Objekt
     */
    @Override
    protected Member doInBackground(ShowRouteActivity... params) {
        view = params[0];

        try {
            SharedPreferences prefs = view.getSharedPreferences("skatenight.app", Context.MODE_PRIVATE);
            String email = prefs.getString("accountName", null);
            return ServiceProvider.getService().skatenightServerEndpoint().getMember(email).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

     @Override
     /**
      * Ruft den Befehl zum zeichnen des Members auf die Karte auf
      */
     protected void onPostExecute(Member m) {
         view.drawMembers(m);
     }
}
