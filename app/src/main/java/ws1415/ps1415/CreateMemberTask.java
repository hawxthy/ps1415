package ws1415.ps1415;

import android.os.AsyncTask;
import android.util.Log;

import com.appspot.skatenight_ms.skatenightAPI.model.Member;
import java.io.IOException;

/**
 * Klasse welche mit SkatenightBackend kommuniziert um auf den Server zu zugreifen.
 *
 * Created by Tristan Rust on 28.10.2014.
 */
public class CreateMemberTask extends AsyncTask<Member, Void, Void> {
    /**
     * Schreibt die neuen Informationen in den Member.
     * @param params Der in der Activity erstellte Member
     */
    protected Void doInBackground(Member... params){
        try{
            ServiceProvider.getService().skatenightServerEndpoint().setMember(params[0]).execute();
            Log.i("XXXXXXXXXX", params[0].getUpdatedAt());
        }catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}




















