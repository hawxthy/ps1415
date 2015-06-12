package ws1415.ps1415.task;

import android.content.Context;

import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupFilter;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;
import com.skatenight.skatenightAPI.model.UserLocationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ws1415.ps1415.ServiceProvider;
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
        List<UserGroupMetaData> tmpGroups = null;
        List<UserLocationInfo> groupMembers;
        HashMap<UserLocationInfo, String> farbenMap = new HashMap<UserLocationInfo, String>();
        UserGroupFilter filter = new UserGroupFilter();
        filter.setLimit(10);
        try {
            tmpGroups = (ArrayList) ServiceProvider.getService().groupEndpoint().fetchMyUserGroups(filter).execute().getMetaDatas();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tmpGroups != null) {
            for (UserGroupMetaData userGroup : tmpGroups) {
                if (PrefManager.getGroupVisibility(context, userGroup.getName())) {
                    try {
                        groupMembers = ServiceProvider.getService().groupEndpoint().listUserGroupVisibleMembersLocationInfo(userGroup.getName()).execute().getItems();
                        for (UserLocationInfo u : groupMembers) {
                            farbenMap.put(u, userGroup.getName());
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    finally {
                        // damit man übersetzen kann
                    }
                }
            }
        }
        return farbenMap;
    }
}
