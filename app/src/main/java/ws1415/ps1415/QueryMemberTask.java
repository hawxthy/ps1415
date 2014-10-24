package ws1415.ps1415;

import android.os.AsyncTask;
import android.widget.TextView;

import com.appspot.skatenight_ms.skatenightAPI.model.Member;

import java.io.IOException;

/**
 * Created by Tristan on 21.10.2014.
 */
public class QueryMemberTask extends AsyncTask<ShowInformationActivity, Void, Member> {
    private ShowInformationActivity view;

    /**
     * Ruft das aktuelle Member-Objekt vom Server ab und schreibt die Informationen in die
     * übergebenen Views.
     * @TODO Diese Methode ist noch temporär auf die ShowInformationAcitivy gesetzt...
     * @param params Die zu befüllenden Views in der Reihenfolge: Datum, Ort, Gebühr, Beschreibung
     * @return Das abgerufene Member-Objekt
     */
    @Override
    protected Member doInBackground(ShowInformationActivity... params) {
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

    }
}
