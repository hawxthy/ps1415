package ws1415.ps1415.task;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.skatenight.skatenightAPI.model.Member;

import java.io.IOException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.ShowRouteActivity;

/**
 * Klasse welche mit dem SkatenightBackend kommuniziert um Nutzerinformationen vom Server
 * abzurufen.
 *
 * @author Tristan Rust
 */
public class QueryMemberTask extends AsyncTask<String, Void, Member> {
     private ShowRouteActivity view;

    public QueryMemberTask(ShowRouteActivity view) {
        this.view = view;
    }

    /**
     * Ruft das aktuelle Member-Objekt vom Server ab.
     *
     * @param params Die zu befüllende Activity
     * @return Das Member-Objekt
     */
    @Override
    protected Member doInBackground(String... params) {
        String email = params[0];

        try {
            if (email != null) {
                return ServiceProvider.getService().skatenightServerEndpoint().getMember(email).execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Ruft den Befehl zum Zeichnen des Members auf die Karte auf.
     */
     @Override
     protected void onPostExecute(Member m) {
//         view.drawMembers(m);
     }
}
