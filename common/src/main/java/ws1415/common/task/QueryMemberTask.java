package ws1415.common.task;

import com.skatenight.skatenightAPI.model.Member;

import java.io.IOException;

import ws1415.common.net.ServiceProvider;

/**
 * Klasse welche mit dem SkatenightBackend kommuniziert um Nutzerinformationen vom Server
 * abzurufen.
 *
 * @author Tristan Rust
 */
public class QueryMemberTask extends ExtendedTask<String, Void, Member> {

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public QueryMemberTask(ExtendedTaskDelegate<Void, Member> delegate) {
        super(delegate);
    }

    /**
     * Ruft das aktuelle Member-Objekt vom Server ab.
     *
     * @param params Die E-Mail des abzurufenden Benutzers
     * @return Das Member-Objekt des Benutzers
     */
    @Override
    protected Member doInBackground(String... params) {
        String email = params[0];

        try {
            if (email != null) {
                return ServiceProvider.getService().userEndpoint().getMember(email).execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
