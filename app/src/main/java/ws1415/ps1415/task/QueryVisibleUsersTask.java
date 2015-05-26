package ws1415.ps1415.task;

import android.content.Context;

import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserLocationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.ps1415.util.PrefManager;

/**
 * Klasse welche mit dem SkatenightBackend kommuniziert um die Mitglieder der eigenen sichtbaren
 * Gruppen vom Server abzurufen.
 *
 * @author Bernd Eissing
 */
public class QueryVisibleUsersTask extends ExtendedTask<Void, Void, HashMap<UserLocationInfo, String>> {
    private Context context;

    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public QueryVisibleUsersTask(ExtendedTaskDelegate<Void, HashMap<UserLocationInfo, String>> delegate, Context context) {
        super(delegate);
        this.context = context;
    }


    /**
     * Ruft die sichtbaren Mitglieder vom Server ab.
     *
     * @return Benutzerinformationen mit der dazugehörigen Bezeichnung der Gruppe
     */
    @Override
    protected HashMap<UserLocationInfo, String> doInBackground(Void... params) {
        List<UserGroup> tmpGroups = null;
        List<UserLocationInfo> groupMembers = new ArrayList<UserLocationInfo>();
        HashMap<UserLocationInfo, String> farbenMap = new HashMap<UserLocationInfo, String>();
        try {
            tmpGroups = (ArrayList) ServiceProvider.getService().groupEndpoint().fetchMyUserGroups().execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tmpGroups != null) {
            for (UserGroup userGroup : tmpGroups) {
                if (PrefManager.getGroupVisibility(context, userGroup.getName())) {
                    try {
                        //TODO auf UserLocationInfo umstellen
                        //groupMembers = ServiceProvider.getService().groupEndpoint().fetchGroupMembers(userGroup.getName()).execute().getItems();
                        for (UserLocationInfo u : groupMembers) {
                            farbenMap.put(u, userGroup.getName());
                        }
                    } finally {
                        // damit man übersetzen kann
                    }
                }
            }
        }
        return farbenMap;
    }
}
